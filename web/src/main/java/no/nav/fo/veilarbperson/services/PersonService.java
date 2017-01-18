package no.nav.fo.veilarbperson.services;

import no.nav.tjeneste.virksomhet.organisasjonenhet.v1.OrganisasjonEnhetV1;
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

    public PersonData hentPerson(String ident) {
        final HentKjerneinformasjonRequest request = new HentKjerneinformasjonRequest().withIdent(ident);

        try {
            HentKjerneinformasjonResponse wsPerson = personV2.hentKjerneinformasjon(request);
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

    private String tilNavn(Person person) {
        final String navnFraTps = person.getPersonnavn().getFornavn() + " " + person.getPersonnavn().getEtternavn();

        return capitalize(navnFraTps.toLowerCase(), '-', ' ');
    }

}