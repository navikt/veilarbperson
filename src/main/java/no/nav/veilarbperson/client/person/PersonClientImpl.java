package no.nav.veilarbperson.client.person;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.cxf.CXFClient;
import no.nav.common.health.HealthCheckResult;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Informasjonsbehov;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personidenter;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentSikkerhetstiltakRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentSikkerhetstiltakResponse;
import no.nav.veilarbperson.client.person.domain.PersonData;
import no.nav.veilarbperson.config.CacheConfig;
import no.nav.veilarbperson.utils.MapExceptionUtil;
import no.nav.veilarbperson.utils.PersonDataMapper;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.springframework.cache.annotation.Cacheable;

import java.util.Optional;

@Slf4j
public class PersonClientImpl implements PersonClient {

    private final PersonV3 personV3;

    private final PersonV3 personV3Ping;

    public PersonClientImpl(String personV3Endpoint) {
        personV3 = new CXFClient<>(PersonV3.class)
                .address(personV3Endpoint)
                .withOutInterceptor(new LoggingOutInterceptor())
                .configureStsForSubject()
                .build();

        personV3Ping = new CXFClient<>(PersonV3.class)
                .address(personV3Endpoint)
                .configureStsForSystemUser()
                .build();
    }

    public PersonClientImpl(PersonV3 personV3, PersonV3 personV3Ping) {
        this.personV3 = personV3;
        this.personV3Ping = personV3Ping;
    }

    @Cacheable(CacheConfig.TPS_PERSON_CACHE_NAME)
    @Override
    public PersonData hentPersonData(String ident) {
        try {
            HentPersonResponse response = personV3.hentPerson(lagHentPersonRequest(ident));
            return PersonDataMapper.tilPersonData(response.getPerson());
        } catch (Exception e) {
            log.error("Henting av person feilet", e);
            throw MapExceptionUtil.map(e);
        }
    }

    @Cacheable(CacheConfig.SIKKERHETSTILTAK_CACHE_NAME)
    @Override
    public String hentSikkerhetstiltak(String ident) {
        try {
            HentSikkerhetstiltakResponse wsSikkerhetstiltak = personV3.hentSikkerhetstiltak(lagHentSikkerhetstiltakRequest(ident));
            return Optional.ofNullable(wsSikkerhetstiltak.getSikkerhetstiltak())
                    .map(no.nav.tjeneste.virksomhet.person.v3.informasjon.Sikkerhetstiltak::getSikkerhetstiltaksbeskrivelse)
                    .orElse(null);
        } catch (Exception e) {
            log.error("Henting av sikkerhetstiltak feilet", e);
            throw MapExceptionUtil.map(e);
        }
    }

    @Override
    public HealthCheckResult checkHealth() {
        try {
            personV3Ping.ping();
            return HealthCheckResult.healthy();
        } catch (Exception e) {
            return HealthCheckResult.unhealthy("Failed to ping personV3", e);
        }
    }

    private HentPersonRequest lagHentPersonRequest(String ident) {
        return new HentPersonRequest()
                .withAktoer(new PersonIdent().withIdent(new NorskIdent().withIdent(ident)))
                .withInformasjonsbehov(
                        Informasjonsbehov.ADRESSE, Informasjonsbehov.BANKKONTO,
                        Informasjonsbehov.FAMILIERELASJONER, Informasjonsbehov.KOMMUNIKASJON
                );
    }

    private HentSikkerhetstiltakRequest lagHentSikkerhetstiltakRequest(String ident) {
        PersonIdent personIdent = new PersonIdent();
        personIdent.setIdent(
                new NorskIdent()
                        .withIdent(ident)
                        .withType(new Personidenter()
                                .withValue("fnr")));
        return new HentSikkerhetstiltakRequest().withAktoer(personIdent);
    }

}
