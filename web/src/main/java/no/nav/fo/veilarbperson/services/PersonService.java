package no.nav.fo.veilarbperson.services;

import no.nav.tjeneste.virksomhet.person.v2.*;
import no.nav.tjeneste.virksomhet.person.v2.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v2.meldinger.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import static org.apache.commons.lang3.text.WordUtils.capitalize;
import static org.slf4j.LoggerFactory.getLogger;

//TODO: Sende feilmeldinger tilbake til frontend
public class PersonService{

    private static final Logger logger = getLogger(PersonService.class);

    @Autowired
    private PersonV2 personV2;

    public PersonData hentPerson(String ident) {
        final WSHentKjerneinformasjonRequest request = new WSHentKjerneinformasjonRequest().withIdent(ident);

        try {
            WSHentKjerneinformasjonResponse wsPerson = personV2.hentKjerneinformasjon(request);
            PersonData personData = PersonDataMapper.tilPersonData(wsPerson.getPerson());
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

    public Boolean hentSikkerhetstiltak(String ident){
        WSNorskIdent norskIdent = new WSNorskIdent()
                .withIdent(ident)
                .withType(new WSPersonidenter()
                        .withValue("fnr"));
        final WSHentSikkerhetstiltakRequest request = new WSHentSikkerhetstiltakRequest().withIdent(norskIdent);

        try{
            WSHentSikkerhetstiltakResponse wsSikkerhetstiltak = personV2.hentSikkerhetstiltak(request);
            wsSikkerhetstiltak.getSikkerhetstiltak();
            return true;
        } catch (HentSikkerhetstiltakPersonIkkeFunnet hentSikkerhetstiltakPersonIkkeFunnet) {
            logger.error("Finner ikke " + ident);
            hentSikkerhetstiltakPersonIkkeFunnet.printStackTrace();
            return false;
        }
    }

    private String tilNavn(WSPerson person) {
        final String navnFraTps = person.getPersonnavn().getFornavn() + " " + person.getPersonnavn().getEtternavn();

        return capitalize(navnFraTps.toLowerCase(), '-', ' ');
    }

}