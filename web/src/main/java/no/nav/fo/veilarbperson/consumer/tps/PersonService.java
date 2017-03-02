package no.nav.fo.veilarbperson.consumer.tps;

import no.nav.fo.veilarbperson.consumer.tps.mappers.PersonDataMapper;
import no.nav.fo.veilarbperson.domain.PersonData;
import no.nav.fo.veilarbperson.domain.Sikkerhetstiltak;
import no.nav.tjeneste.virksomhet.person.v2.*;
import no.nav.tjeneste.virksomhet.person.v2.informasjon.WSNorskIdent;
import no.nav.tjeneste.virksomhet.person.v2.informasjon.WSPersonidenter;
import no.nav.tjeneste.virksomhet.person.v2.meldinger.*;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class PersonService{

    private static final Logger logger = getLogger(PersonService.class);

    private final PersonV2 personV2;

    private final PersonDataMapper personDataMapper;


    public PersonService(PersonV2 personV2) {
        this.personV2 = personV2;
        this.personDataMapper = new PersonDataMapper();
    }

    public PersonData hentPerson(String ident) {
        final WSHentKjerneinformasjonRequest request = new WSHentKjerneinformasjonRequest().withIdent(ident);

        try {
            WSHentKjerneinformasjonResponse wsPerson = personV2.hentKjerneinformasjon(request);
            PersonData personData = personDataMapper.tilPersonData(wsPerson.getPerson());
            return personData;
        } catch (HentKjerneinformasjonSikkerhetsbegrensning ikkeTilgang) {
            logger.error("Ikke tilgang til " + ident);
            ikkeTilgang.printStackTrace();
            return new PersonData();
        } catch (HentKjerneinformasjonPersonIkkeFunnet ikkeFunnet) {
            logger.error("Finner ikke " + ident);
            ikkeFunnet.printStackTrace();
            return new PersonData();
        }
    }

    public Sikkerhetstiltak hentSikkerhetstiltak(String ident) throws HentSikkerhetstiltakPersonIkkeFunnet {
        final WSHentSikkerhetstiltakRequest request = lagRequest(ident);

        WSHentSikkerhetstiltakResponse wsSikkerhetstiltak = personV2.hentSikkerhetstiltak(request);

        String sikkerhetstiltaksbeskrivelse = wsSikkerhetstiltak
                .getSikkerhetstiltak()
                .getSikkerhetstiltaksbeskrivelse();
        return new Sikkerhetstiltak().medSikkerhetstiltaksbeskrivelse(sikkerhetstiltaksbeskrivelse);
    }

    private WSHentSikkerhetstiltakRequest lagRequest(String ident) {
        WSNorskIdent norskIdent = new WSNorskIdent()
                .withIdent(ident)
                .withType(new WSPersonidenter()
                        .withValue("fnr"));
        return new WSHentSikkerhetstiltakRequest().withIdent(norskIdent);
    }

}