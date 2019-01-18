package no.nav.fo.veilarbperson.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.vavr.control.Try;
import no.nav.apiapp.feil.IngenTilgang;
import no.nav.apiapp.security.PepClient;
import no.nav.common.auth.SubjectHandler;
import no.nav.fo.veilarbperson.PersonFletter;
import no.nav.fo.veilarbperson.consumer.digitalkontaktinformasjon.DigitalKontaktinformasjonService;
import no.nav.fo.veilarbperson.consumer.kodeverk.KodeverkService;
import no.nav.fo.veilarbperson.consumer.organisasjonenhet.EnhetService;
import no.nav.fo.veilarbperson.consumer.portefolje.PortefoljeService;
import no.nav.fo.veilarbperson.consumer.tps.EgenAnsattService;
import no.nav.fo.veilarbperson.consumer.tps.PersonService;
import no.nav.fo.veilarbperson.domain.person.Bostedsadresse;
import no.nav.fo.veilarbperson.domain.person.PersonData;
import no.nav.fo.veilarbperson.domain.person.PersonNavn;
import no.nav.fo.veilarbperson.utils.AutentiseringHjelper;
import no.nav.fo.veilarbperson.utils.FeilmeldingResponsHjelper;
import no.nav.tjeneste.virksomhet.person.v3.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.HentPersonSikkerhetsbegrensning;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.slf4j.LoggerFactory.getLogger;

@Component
@Api(value = "person")
@Path("/person/{fodselsnummer}")
public class PersonRessurs {

    private static final Logger logger = getLogger(PersonRessurs.class);

    private final PersonFletter personFletter;
    private final PepClient pepClient;
    private final PersonService personService;

    public PersonRessurs(EnhetService enhetService,
                         DigitalKontaktinformasjonService digitalKontaktinformasjonService,
                         PersonService personService,
                         EgenAnsattService egenAnsattService,
                         KodeverkService kodeverkService,
                         PortefoljeService portefoljeService,
                         PepClient pepClient
    ) {

        this.pepClient = pepClient;
        this.personService = personService;

        personFletter = new PersonFletter(
                enhetService,
                digitalKontaktinformasjonService,
                personService,
                egenAnsattService,
                kodeverkService,
                portefoljeService
        );
    }

    @GET
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Henter informasjon om en person",
            notes = "Denne tjenesten gjør kall mot flere baktjenester: " +
                    "Kodeverk, organisasjonenhet_v2, Digitalkontaktinformasjon_v1, Person_v3, Egenansatt_v1")
    public Response person(@PathParam("fodselsnummer") String fodselsnummer, @Context HttpServletRequest request) {

        logger.info("Henter persondata med fodselsnummer: " + fodselsnummer);

        if(AutentiseringHjelper.erEksternBruker()) {
            return FeilmeldingResponsHjelper.lagResponsForSluttbrukerIkkeTilgangFeilmelding(fodselsnummer);
        }

        pepClient.sjekkLeseTilgangTilFnr(fodselsnummer);

        try {
            String cookie = request.getHeader(HttpHeaders.COOKIE);
            PersonData person = personFletter.hentPerson(fodselsnummer, cookie);

            return Response.ok().entity(person).build();
        } catch (HentPersonPersonIkkeFunnet hentPersonPersonIkkeFunnet) {
            return FeilmeldingResponsHjelper.lagResponsForIkkeFunnetFeilmelding(
                    hentPersonPersonIkkeFunnet, fodselsnummer
            );
        } catch (HentPersonSikkerhetsbegrensning hentPersonSikkerhetsbegrensning) {
            return FeilmeldingResponsHjelper.lagResponsForIkkeTilgangFeilmelding(
                    hentPersonSikkerhetsbegrensning, fodselsnummer
            );
        }
    }


    @GET
    @Path("/navn")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Henter navnet til en person")
    public Response navn(@PathParam("fodselsnummer") String fodselsnummer) {

        logger.info("Henter brukerens navn med fodselsnummer: " + fodselsnummer);

        pepClient.sjekkLeseTilgangTilFnr(fodselsnummer);

        try {
            PersonData personData = personService.hentPerson(fodselsnummer);
            PersonNavn personNavn = PersonNavn.fraPerson(personData);

            return Response.ok().entity(personNavn).build();
        } catch (HentPersonSikkerhetsbegrensning hentPersonSikkerhetsbegrensning) {
            return FeilmeldingResponsHjelper.lagResponsForIkkeTilgangFeilmelding(
                    hentPersonSikkerhetsbegrensning, fodselsnummer
            );
        } catch (HentPersonPersonIkkeFunnet hentPersonPersonIkkeFunnet) {
            return FeilmeldingResponsHjelper.lagResponsForIkkeFunnetFeilmelding(
                    hentPersonPersonIkkeFunnet, fodselsnummer
            );
        }

    }

    @GET
    @Path("/tilgangTilBruker")
    public boolean tilgangTilBruker(@PathParam("fodselsnummer") String fodselsnummer) {
        logger.info("Sjekker om veileder har tilgang til bruker med fodselsnummer: " + fodselsnummer);

        try {
            pepClient.sjekkLeseTilgangTilFnr(fodselsnummer);
            return true;
        } catch (RuntimeException e) {
            logger.info("Veileder har ikke tilgang til bruker med fodselsnummer: " + fodselsnummer);
            return false;
        }
    }

    @GET
    @Path("/bostedsadresse")
    @Produces(APPLICATION_JSON)
    public Bostedsadresse bostedsadresse(@PathParam("fodselsnummer") String fodselsnummer) {
        pepClient.sjekkLeseTilgangTilFnr(fodselsnummer);
        return Try.of(() -> personFletter.hentBostedsadresse(fodselsnummer)).get();
    }

}
