package no.nav.veilarbperson.client.tps;

import io.vavr.control.Try;
import no.nav.veilarbperson.client.tps.mappers.PersonDataMapper;
import no.nav.veilarbperson.domain.person.GeografiskTilknytning;
import no.nav.veilarbperson.domain.person.PersonData;
import no.nav.veilarbperson.domain.person.Sikkerhetstiltak;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentSikkerhetstiltakPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Informasjonsbehov;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personidenter;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentSikkerhetstiltakRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentSikkerhetstiltakResponse;
import org.springframework.cache.annotation.Cacheable;

import java.util.Optional;

import static no.nav.veilarbperson.config.CacheConfig.GEOGRAFISK_TILKNYTNING;
import static no.nav.veilarbperson.config.CacheConfig.PERSON;

public class PersonService {

    private final PersonV3 personV3;
    private final PersonDataMapper personDataMapper;

    public PersonService(PersonV3 personV3) {
        this.personV3 = personV3;
        this.personDataMapper = new PersonDataMapper();
    }

    @Cacheable(PERSON)
    public PersonData hentPerson(String ident) throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        return Try.of(() -> personV3.hentPerson(lagHentPersonRequest(ident)))
                .map(wsPerson -> personDataMapper.tilPersonData(wsPerson.getPerson()))
                .get();
    }

    @Cacheable(GEOGRAFISK_TILKNYTNING)
    public GeografiskTilknytning hentGeografiskTilknytning(String ident) throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        return Try.of( ()-> personV3.hentPerson(lagHentPersonRequest(ident)))
                .map(wsPerson -> PersonDataMapper.geografiskTilknytning(wsPerson.getPerson()))
                .map(GeografiskTilknytning::new)
                .get();
    }

    private HentPersonRequest lagHentPersonRequest(String ident) {
        return new HentPersonRequest().withAktoer(new PersonIdent().withIdent(new NorskIdent().withIdent(ident)))
                .withInformasjonsbehov(Informasjonsbehov.ADRESSE, Informasjonsbehov.BANKKONTO,
                        Informasjonsbehov.FAMILIERELASJONER, Informasjonsbehov.KOMMUNIKASJON);
    }

    public Sikkerhetstiltak hentSikkerhetstiltak(String ident) throws HentSikkerhetstiltakPersonIkkeFunnet {
        HentSikkerhetstiltakResponse wsSikkerhetstiltak = personV3.hentSikkerhetstiltak(lagHentSikkerhetstiltakRequest(ident));
        String beskrivelse = Optional.ofNullable(wsSikkerhetstiltak.getSikkerhetstiltak())
                .map(no.nav.tjeneste.virksomhet.person.v3.informasjon.Sikkerhetstiltak::getSikkerhetstiltaksbeskrivelse)
                .orElse(null);
        return new Sikkerhetstiltak(beskrivelse);
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