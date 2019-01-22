package no.nav.fo.veilarbperson.consumer.tps;

import io.vavr.control.Try;
import no.nav.fo.veilarbperson.consumer.tps.mappers.PersonDataMapper;
import no.nav.fo.veilarbperson.domain.person.GeografiskTilknytning;
import no.nav.fo.veilarbperson.domain.person.PersonData;
import no.nav.fo.veilarbperson.domain.person.Sikkerhetstiltak;
import no.nav.tjeneste.virksomhet.person.v3.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.HentSikkerhetstiltakPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.WSHentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.WSHentSikkerhetstiltakRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.WSHentSikkerhetstiltakResponse;
import org.springframework.cache.annotation.Cacheable;

import java.util.Optional;

import static no.nav.fo.veilarbperson.config.CacheConfig.GEOGRAFISK_TILKNYTNING;
import static no.nav.fo.veilarbperson.config.CacheConfig.PERSON;

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

    private WSHentPersonRequest lagHentPersonRequest(String ident) {
        return new WSHentPersonRequest().withAktoer(new WSPersonIdent().withIdent(new WSNorskIdent().withIdent(ident)))
                .withInformasjonsbehov(WSInformasjonsbehov.ADRESSE, WSInformasjonsbehov.BANKKONTO,
                        WSInformasjonsbehov.FAMILIERELASJONER, WSInformasjonsbehov.KOMMUNIKASJON);
    }

    public Sikkerhetstiltak hentSikkerhetstiltak(String ident) throws HentSikkerhetstiltakPersonIkkeFunnet {
        WSHentSikkerhetstiltakResponse wsSikkerhetstiltak = personV3.hentSikkerhetstiltak(lagHentSikkerhetstiltakRequest(ident));
        String beskrivelse = Optional.ofNullable(wsSikkerhetstiltak.getSikkerhetstiltak())
                .map(WSSikkerhetstiltak::getSikkerhetstiltaksbeskrivelse)
                .orElse(null);
        return new Sikkerhetstiltak(beskrivelse);
    }

    private WSHentSikkerhetstiltakRequest lagHentSikkerhetstiltakRequest(String ident) {
        WSPersonIdent personIdent = new WSPersonIdent();
        personIdent.setIdent(
                new WSNorskIdent()
                        .withIdent(ident)
                        .withType(new WSPersonidenter()
                                .withValue("fnr")));
        return new WSHentSikkerhetstiltakRequest().withAktoer(personIdent);
    }

}