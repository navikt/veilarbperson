package no.nav.fo.veilarbperson.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.vavr.control.Try;
import no.nav.apiapp.feil.IngenTilgang;
import no.nav.apiapp.security.PepClient;
import no.nav.fo.veilarbperson.PersonFletter;
import no.nav.fo.veilarbperson.consumer.digitalkontaktinformasjon.DigitalKontaktinformasjonService;
import no.nav.fo.veilarbperson.consumer.kodeverk.KodeverkService;
import no.nav.fo.veilarbperson.consumer.organisasjonenhet.EnhetService;
import no.nav.fo.veilarbperson.consumer.portefolje.PortefoljeService;
import no.nav.fo.veilarbperson.consumer.tps.EgenAnsattService;
import no.nav.fo.veilarbperson.consumer.tps.PersonService;
import no.nav.fo.veilarbperson.domain.person.BoligInformasjon;
import no.nav.fo.veilarbperson.domain.person.PersonData;
import no.nav.fo.veilarbperson.domain.person.PersonNavn;
import no.nav.fo.veilarbperson.utils.AutentiseringHjelper;
import no.nav.fo.veilarbperson.utils.FeilmeldingResponsHjelper;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Component
@Api(value = "person")
@Path("/person/{fodselsnummer}")
public class PersonRessurs {

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
    public PersonData person(@PathParam("fodselsnummer") String fodselsnummer, @Context HttpServletRequest request) {

        if(AutentiseringHjelper.erEksternBruker()) {
            throw new IngenTilgang("Bruker har ikke tilgang til hente persondata");
        }

        pepClient.sjekkLeseTilgangTilFnr(fodselsnummer);

        Try<PersonData> tryPersonData = Try.of(() -> {
            String cookie = request.getHeader(HttpHeaders.COOKIE);
            return personFletter.hentPerson(fodselsnummer, cookie);
        });

        return tryPersonData.getOrElseThrow(FeilmeldingResponsHjelper::feilHanteringHjelper);
    }

    @GET
    @Path("/navn")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Henter navnet til en person")
    public PersonNavn navn(@PathParam("fodselsnummer") String fodselsnummer) {

        pepClient.sjekkLeseTilgangTilFnr(fodselsnummer);

        return Try.of(()-> personService.hentPerson(fodselsnummer))
                .map(PersonNavn::fraPerson)
                .getOrElseThrow(FeilmeldingResponsHjelper::feilHanteringHjelper);
    }

    @GET
    @Path("/tilgangTilBruker")
    public boolean tilgangTilBruker(@PathParam("fodselsnummer") String fodselsnummer) {
        return Try.of(() -> pepClient.sjekkLeseTilgangTilFnr(fodselsnummer))
                .map(fnr -> true)
                .getOrElseGet(exception -> false);
    }

    @GET
    @Path("/boliginformasjon")
    @Produces(APPLICATION_JSON)
    public BoligInformasjon bostedsadresse(@PathParam("fodselsnummer") String fodselsnummer) {
        pepClient.sjekkLeseTilgangTilFnr(fodselsnummer);
        return Try.of(() -> personFletter.hentBostedsadresse(fodselsnummer))
                .getOrElseThrow(FeilmeldingResponsHjelper::feilHanteringHjelper);
    }

}
