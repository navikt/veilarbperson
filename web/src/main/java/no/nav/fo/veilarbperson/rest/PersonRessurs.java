package no.nav.fo.veilarbperson.rest;

import no.nav.fo.veilarbperson.PersonFletter;
import no.nav.fo.veilarbperson.consumer.digitalkontaktinformasjon.DigitalKontaktinformasjonService;
import no.nav.fo.veilarbperson.consumer.kodeverk.KodeverkService;
import no.nav.fo.veilarbperson.consumer.organisasjonenhet.EnhetService;
import no.nav.fo.veilarbperson.consumer.tps.EgenAnsattService;
import no.nav.fo.veilarbperson.consumer.tps.PersonService;
import no.nav.fo.veilarbperson.domain.Feilmelding;
import no.nav.fo.veilarbperson.domain.person.PersonData;
import no.nav.tjeneste.virksomhet.person.v2.HentKjerneinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v2.HentKjerneinformasjonSikkerhetsbegrensning;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.slf4j.LoggerFactory.getLogger;
@Component
@Path("/person/{fodselsnummer}")
public class PersonRessurs {

    private static final Logger logger = getLogger(PersonRessurs.class);

    final PersonFletter personFletter;

    public PersonRessurs(EnhetService enhetService,
                         DigitalKontaktinformasjonService digitalKontaktinformasjonService,
                         PersonService personService,
                         EgenAnsattService egenAnsattService,
                         KodeverkService kodeverkService) {

        personFletter = new PersonFletter(
                enhetService,
                digitalKontaktinformasjonService,
                personService,
                egenAnsattService,
                kodeverkService);
    }

    @GET
    @Produces(APPLICATION_JSON)
    public Response person(@PathParam("fodselsnummer") String fodselsnummer) {

        logger.info("Henter persondata med fodselsnummer: " + fodselsnummer);

        try {
            PersonData person = personFletter.hentPerson(fodselsnummer);
            return Response.ok().entity(person).build();
        } catch (HentKjerneinformasjonPersonIkkeFunnet hentKjerneinformasjonPersonIkkeFunnet) {
            Feilmelding feilmelding = new Feilmelding("Fant ikke person med fodselsnummer: " + fodselsnummer,
                    hentKjerneinformasjonPersonIkkeFunnet.toString());
            return Response
                    .status(Status.NOT_FOUND)
                    .entity(feilmelding)
                    .build();
        } catch (HentKjerneinformasjonSikkerhetsbegrensning hentKjerneinformasjonSikkerhetsbegrensning) {
            Feilmelding feilmelding = new Feilmelding("Saksbehandler har ikke tilgang til fodselsnummer: " + fodselsnummer,
                    hentKjerneinformasjonSikkerhetsbegrensning.toString());
            return Response
                    .status(Status.UNAUTHORIZED)
                    .entity(feilmelding)
                    .build();
        }
    }

}

