package no.nav.fo.veilarbperson.consumer.tps;

import no.nav.fo.veilarbperson.consumer.tps.mappers.PersonDataMapper;
import no.nav.fo.veilarbperson.domain.person.PersonData;
import no.nav.fo.veilarbperson.domain.person.Sikkerhetstiltak;
import no.nav.tjeneste.virksomhet.person.v3.*;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.*;
import org.slf4j.Logger;

import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class PersonService {

    private static final Logger logger = getLogger(PersonService.class);

    private final PersonV3 personV3;
    private final PersonDataMapper personDataMapper;

    public PersonService(PersonV3 personV3) {
        this.personV3 = personV3;
        this.personDataMapper = new PersonDataMapper();
    }

    public PersonData hentPerson(String ident) throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        try {
            WSHentPersonResponse wsPerson = personV3.hentPerson(lagHentPersonRequest(ident));
            return personDataMapper.tilPersonData(wsPerson.getPerson());
        } catch (HentPersonPersonIkkeFunnet hentPersonPersonIkkeFunnet) {
            logger.info("Person ikke funnet: " + ident);
            throw hentPersonPersonIkkeFunnet;
        } catch (HentPersonSikkerhetsbegrensning hentPersonSikkerhetsbegrensning) {
            logger.info("Saksbehandler har ikke tilgang til: " + ident);
            throw hentPersonSikkerhetsbegrensning;
        }
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