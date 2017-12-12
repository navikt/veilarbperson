package no.nav.fo.veilarbperson.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import no.nav.fo.veilarbperson.PersonFletter;
import no.nav.fo.veilarbperson.consumer.digitalkontaktinformasjon.DigitalKontaktinformasjonService;
import no.nav.fo.veilarbperson.consumer.kodeverk.KodeverkFetcher;
import no.nav.fo.veilarbperson.consumer.organisasjonenhet.EnhetService;
import no.nav.fo.veilarbperson.consumer.tps.EgenAnsattService;
import no.nav.fo.veilarbperson.consumer.tps.PersonService;
import no.nav.fo.veilarbperson.domain.Feilmelding;
import no.nav.fo.veilarbperson.domain.person.PersonData;
import no.nav.fo.veilarbperson.services.PepClient;
import no.nav.tjeneste.virksomhet.person.v3.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.HentPersonSikkerhetsbegrensning;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.slf4j.LoggerFactory.getLogger;

@Component
@Api(value = "person")
@Path("/person/{fodselsnummer}")
public class PersonRessurs {

    private static final Logger logger = getLogger(PersonRessurs.class);

    private final PersonFletter personFletter;
    private final PepClient pepClient;

    public PersonRessurs(EnhetService enhetService,
                         DigitalKontaktinformasjonService digitalKontaktinformasjonService,
                         PersonService personService,
                         EgenAnsattService egenAnsattService,
                         KodeverkFetcher kodeverkFetcher,
                         PepClient pepClient) {

        this.pepClient = pepClient;

        personFletter = new PersonFletter(
                enhetService,
                digitalKontaktinformasjonService,
                personService,
                egenAnsattService,
                kodeverkFetcher);
    }

    @GET
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Henter informasjon om en person",
            notes = "Denne tjenesten gjør kall mot flere baktjenester: " +
                    "Kodeverk, organisasjonenhet_v2, Digitalkontaktinformasjon_v1, Person_v3, Egenansatt_v1")
    public Response person(@PathParam("fodselsnummer") String fodselsnummer, @Context HttpServletRequest request) {

        logger.info("Henter persondata med fodselsnummer: " + fodselsnummer);

        pepClient.isServiceCallAllowed(fodselsnummer);

        try {
            String cookie = request.getHeader(HttpHeaders.COOKIE);
            PersonData person = personFletter.hentPerson(fodselsnummer, cookie);

            return Response.ok().entity(person).build();
        } catch (HentPersonPersonIkkeFunnet hentPersonPersonIkkeFunnet) {
            Feilmelding feilmelding = new Feilmelding("Fant ikke person med fnr: " + fodselsnummer,
                    hentPersonPersonIkkeFunnet.toString());
            return Response
                    .status(Status.NOT_FOUND)
                    .entity(feilmelding)
                    .build();
        } catch (HentPersonSikkerhetsbegrensning hentPersonSikkerhetsbegrensning) {
            Feilmelding feilmelding = new Feilmelding("Saksbehandler har ikke tilgang til fnr: " + fodselsnummer,
                    hentPersonSikkerhetsbegrensning.toString());
            return Response
                    .status(Status.UNAUTHORIZED)
                    .entity(feilmelding)
                    .build();
        }
    }

}
