package no.nav.fo.veilarbperson.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.vavr.control.Try;
import no.nav.apiapp.feil.Feil;
import no.nav.apiapp.feil.FeilType;
import no.nav.apiapp.security.veilarbabac.Bruker;
import no.nav.apiapp.security.veilarbabac.VeilarbAbacPepClient;
import no.nav.common.auth.SubjectHandler;
import no.nav.dialogarena.aktor.AktorService;
import no.nav.fo.veilarbperson.PersonFletter;
import no.nav.fo.veilarbperson.consumer.digitalkontaktinformasjon.DigitalKontaktinformasjonService;
import no.nav.fo.veilarbperson.consumer.kodeverk.KodeverkService;
import no.nav.fo.veilarbperson.consumer.organisasjonenhet.EnhetService;
import no.nav.fo.veilarbperson.consumer.portefolje.PortefoljeService;
import no.nav.fo.veilarbperson.consumer.tps.EgenAnsattService;
import no.nav.fo.veilarbperson.consumer.tps.PersonService;
import no.nav.fo.veilarbperson.domain.person.*;
import no.nav.fo.veilarbperson.utils.AutentiseringHjelper;
import no.nav.fo.veilarbperson.utils.MapExceptionUtil;
import no.nav.fo.veilarbperson.utils.MapKrrExceptionUtil;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.apiapp.feil.FeilType.FINNES_IKKE;

@Component
@Api(value = "person")
@Path("/person")
public class PersonRessurs {

    private final PersonFletter personFletter;
    private final DigitalKontaktinformasjonService digitalKontaktinformasjonService;
    private final VeilarbAbacPepClient pepClient;
    private final PersonService personService;

    @Inject
    private AktorService aktorService;

    public PersonRessurs(EnhetService enhetService,
                         DigitalKontaktinformasjonService digitalKontaktinformasjonService,
                         PersonService personService,
                         EgenAnsattService egenAnsattService,
                         KodeverkService kodeverkService,
                         PortefoljeService portefoljeService,
                         VeilarbAbacPepClient pepClient
    ) {
        this.digitalKontaktinformasjonService = digitalKontaktinformasjonService;
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
    @Path("/{fodselsnummer}")
    @ApiOperation(value = "Henter informasjon om en person",
            notes = "Denne tjenesten gjør kall mot flere baktjenester: " +
                    "Kodeverk, organisasjonenhet_v2, Digitalkontaktinformasjon_v1, Person_v3, Egenansatt_v1")
    public PersonData person(@PathParam("fodselsnummer") String fodselsnummer, @Context HttpServletRequest request) {


        if(AutentiseringHjelper.erEksternBruker()) {
            throw new Feil(FeilType.INGEN_TILGANG);
        }

        pepClient.sjekkLesetilgangTilBruker(lagBruker(fodselsnummer));
        String cookie = request.getHeader(HttpHeaders.COOKIE);

        return Try.of(() -> personFletter.hentPerson(fodselsnummer, cookie))
                .getOrElseThrow(MapExceptionUtil::map);
    }

    @GET
    @Path("/aktorid")
    @Produces(APPLICATION_JSON)
    public AktoerId aktorid(@QueryParam("fnr") String fodselsnummer){

        if(AutentiseringHjelper.erInternBruker()) {
            Bruker bruker = lagBruker(fodselsnummer);
            pepClient.sjekkLesetilgangTilBruker(bruker);

            return new AktoerId(bruker.getAktoerId());
        }

        throw new Feil(FeilType.INGEN_TILGANG);
    }

    @GET
    @Path("/navn")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Henter navnet til en person")
    public PersonNavn navn(@QueryParam("fnr") String fnr) {

        // Fnr fra query param kan kun brukes av interne brukere, eksterne må bruke token
        final String fodselsnummer = (fnr != null && AutentiseringHjelper.erInternBruker()) ?
                fnr : SubjectHandler.getIdent().orElseThrow(() -> new Feil(FeilType.UGYLDIG_REQUEST));

        pepClient.sjekkLesetilgangTilBruker(lagBruker(fodselsnummer));

        return Try.of(() -> personService.hentPerson(fodselsnummer))
                .map(PersonNavn::fraPerson)
                .getOrElseThrow(MapExceptionUtil::map);

    }

    @GET
    @Path("/kontaktinfo")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Henter kontaktinfo til en person")
    public Kontaktinfo kontaktinfo(@QueryParam("fnr") String fnr) {

        // Fnr fra query param kan kun brukes av interne brukere, eksterne må bruke token
        final String fodselsnummer = (fnr != null && AutentiseringHjelper.erInternBruker()) ?
                fnr : SubjectHandler.getIdent().orElseThrow(() -> new Feil(FeilType.UGYLDIG_REQUEST));

        pepClient.sjekkLesetilgangTilBruker(lagBruker(fodselsnummer));

        return Try.of(() -> digitalKontaktinformasjonService.hentDigitalKontaktinformasjon(fodselsnummer))
                .map(Kontaktinfo::fraKrr)
                .getOrElseThrow(MapKrrExceptionUtil::map);
    }

    @GET
    @Path("/{fodselsnummer}/malform")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Henter målform til en person")
    public Malform malform(@PathParam("fodselsnummer") String fnr) {

        if(AutentiseringHjelper.erEksternBruker()) {
            throw new Feil(FeilType.INGEN_TILGANG);
        }

        pepClient.sjekkLesetilgangTilBruker(lagBruker(fnr));

        return Try.of(() -> personService.hentPerson(fnr))
                .map(PersonData::getMalform)
                .map(Malform::new)
                .getOrElseThrow(MapExceptionUtil::map);
    }

    @GET
    @Path("/{fodselsnummer}/tilgangTilBruker")
    public boolean tilgangTilBruker(@PathParam("fodselsnummer") String fodselsnummer) {
        return Try.runRunnable(() -> pepClient.sjekkLesetilgangTilBruker(lagBruker(fodselsnummer))).isSuccess();
    }

    @GET
    @Path("/geografisktilknytning")
    @Produces(APPLICATION_JSON)
    public GeografiskTilknytning geografisktilknytning(@QueryParam("fnr") String fodselsnummer) {
        if(AutentiseringHjelper.erEksternBruker()) {
            String fnr = SubjectHandler.getIdent().orElseThrow(RuntimeException::new);
            return Try.of(() -> personFletter.hentGeografisktilknytning(fnr))
                    .getOrElseThrow(MapExceptionUtil::map);
        }
        else if(AutentiseringHjelper.erInternBruker()) {
            pepClient.sjekkLesetilgangTilBruker(lagBruker(fodselsnummer));
            return Try.of(() -> personFletter.hentGeografisktilknytning(fodselsnummer))
                    .getOrElseThrow(MapExceptionUtil::map);
        }

        throw new Feil(FeilType.INGEN_TILGANG);
    }

    private Bruker lagBruker(String fnr) {
        return Bruker.fraFnr(fnr)
                .medAktoerIdSupplier(()->aktorService.getAktorId(fnr).orElseThrow(()->new Feil(FINNES_IKKE, "Finner ikke aktørId for gitt Fnr")));
    }

}
