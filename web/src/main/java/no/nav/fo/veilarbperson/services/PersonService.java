package no.nav.fo.veilarbperson.services;

import no.nav.tjeneste.virksomhet.person.v2.*;
import no.nav.tjeneste.virksomhet.person.v2.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v2.meldinger.HentKjerneinformasjonRequest;
import no.nav.tjeneste.virksomhet.person.v2.meldinger.HentKjerneinformasjonResponse;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import static org.apache.commons.lang3.text.WordUtils.capitalize;
import static org.slf4j.LoggerFactory.getLogger;

public class PersonService{

    private static final Logger logger = getLogger(PersonService.class);

    @Autowired
    private PersonV2 personV2;

    public String  hentNavn(String ident) {
        final HentKjerneinformasjonRequest request = new HentKjerneinformasjonRequest().withIdent(ident);

        try {
            HentKjerneinformasjonResponse wsPerson = personV2.hentKjerneinformasjon(request);
            return tilNavn(wsPerson.getPerson());

        } catch (HentKjerneinformasjonSikkerhetsbegrensning ikkeTilgang) {
            logger.error("Ikke tilgang til " + ident);
            ikkeTilgang.printStackTrace();
            return "Du har ikke tilgang til å se navnet.";
        } catch (HentKjerneinformasjonPersonIkkeFunnet ikkeFunnet) {
            logger.error("Finner ikke " + ident);
            ikkeFunnet.printStackTrace();
            return "Vi finner ikke navnet";
        }
    }

    private String tilNavn(Person person) {
        final String navnFraTps = person.getPersonnavn().getFornavn() + " " + person.getPersonnavn().getEtternavn();

        return capitalize(navnFraTps.toLowerCase(), '-', ' ');
    }

}