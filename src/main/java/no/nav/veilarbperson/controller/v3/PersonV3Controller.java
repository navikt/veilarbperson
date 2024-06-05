package no.nav.veilarbperson.controller.v3;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.ArbeidssokerperiodeResponse;
import no.nav.veilarbperson.client.regoppslag.RegoppslagClient;
import no.nav.veilarbperson.client.regoppslag.RegoppslagResponseDTO;
import no.nav.veilarbperson.domain.*;
import no.nav.veilarbperson.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v3")
public class PersonV3Controller {

    private final PersonV2Service personV2Service;
    private final AuthService authService;
    private final RegoppslagClient regoppslagClient;

    private final CvJobbprofilService cvJobbprofilService;

    private final RegistreringService registreringService;

    private final OppslagArbeidssoekerregisteretService oppslagArbeidssoekerregisteretService;

    @PostMapping("/hent-person")
    @Operation(summary = "Henter informasjon om en person fra PDL")
    public PersonV2Data hentPerson(@RequestBody PersonFraPdlRequest personFraPdlRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personFraPdlRequest.getFnr());
        return personV2Service.hentFlettetPerson(personFraPdlRequest);
    }

    @PostMapping("/person/hent-aktorid")
    @Operation(summary = "Henter aktørId til person")
    public AktoerId aktorid(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        return new AktoerId(authService.getAktorId(personRequest.getFnr()));
    }

    @PostMapping("/person/hent-tilgangTilBruker")
    @Operation(summary = "Sjekk om innlogget person har tilgang til person")
    public boolean tilgangTilBruker(@RequestBody PersonRequest personRequest) {
        return authService.harLesetilgang(personRequest.getFnr());
    }

    @PostMapping("/person/hent-geografisktilknytning")
    @Operation(summary = "Henter persons geografisk tilknytning")
    public GeografiskTilknytning geografisktilknytning(@RequestBody PersonFraPdlRequest personFraPdlRequest) {
        Fnr fodselsnummer = hentIdentForEksternEllerIntern(personFraPdlRequest.getFnr());
        authService.sjekkLesetilgang(fodselsnummer);
        return personV2Service.hentGeografiskTilknytning(personFraPdlRequest);
    }

    @PostMapping("/person/hent-cv_jobbprofil")
    @Operation(summary = "Henter persons cv og jobbprofil")
    public ResponseEntity<String> cvOgJobbprofil(@RequestBody PersonRequest personRequest) {
        return cvJobbprofilService.hentCvJobbprofilJson(personRequest.getFnr());
    }

    @PostMapping("/person/hent-registrering")
    @Operation(summary = "Henter registreringen til person")
    public ResponseEntity<String> registrering(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        return registreringService.hentRegistrering(personRequest.getFnr());
    }

    @PostMapping("/person/registrering/hent-endringer")
    @Operation(summary = "Henter endringer på registreringen til person")
    public ResponseEntity<String> endringIRegistreringdata(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        return registreringService.hentEndringIRegistreringsdata(personRequest.getFnr());
    }

    @PostMapping("/person/hent-malform")
    @Operation(summary = "Henter malform fra DIGDIR tjeneste")
    public Malform malform(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        String malform = personV2Service.hentMalform(personRequest.getFnr());
        return new Malform(malform);
    }

    @PostMapping("/person/hent-vergeOgFullmakt")
    @Operation(summary = "Henter informasjon om verge og fullmakt for en person fra PDL")
    public VergeOgFullmaktData hentVergemaalOgFullmakt(@RequestBody PersonFraPdlRequest personFraPdlRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personFraPdlRequest.getFnr());
        return personV2Service.hentVergeEllerFullmakt(personFraPdlRequest);
    }

    @PostMapping("/person/hent-tolk")
    @Operation(summary = "Henter tolk informajon til en person fra PDL")
    public TilrettelagtKommunikasjonData hentSpraakTolk(@RequestBody PersonFraPdlRequest personFraPdlRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personFraPdlRequest.getFnr());
        return personV2Service.hentSpraakTolkInfo(personFraPdlRequest);
    }

    @PostMapping("/person/hent-navn")
    @Operation(summary = "Henter navn til en person fra PDL")
    public PersonNavnV2 hentNavn(@RequestBody PersonFraPdlRequest personFraPdlRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personFraPdlRequest.getFnr());
        return personV2Service.hentNavn(personFraPdlRequest);
    }

    @PostMapping("/person/hent-postadresse")
    @Operation(summary = "Henter postadresse til en person fra regoppslag")
    public RegoppslagResponseDTO hentPostadresse(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        return regoppslagClient.hentPostadresse(personRequest.getFnr());
    }

    @PostMapping("/person/hent-siste-opplysninger-om-arbeidssoeker-med-profilering")
    @Operation(summary = "Henter svarene fra den siste arbeidssøkerregistrering til en person i en aktiv arbeidssøkerperiode")
    public OpplysningerOmArbeidssoekerMedProfilering hentSisteOpplysningerOmArbeidssoerkerOgProfilering(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        return oppslagArbeidssoekerregisteretService.hentSisteOpplysningerOmArbeidssoekerMedProfilering(personRequest.getFnr());
    }

    @PostMapping("/person/hent-siste-aktiv-arbeidssoekerperiode")
    @Operation(summary = "Henter siste aktiv arbeidssøkerperiode på en person")
    public ArbeidssokerperiodeResponse hentSisteArbeidssoekerperiode(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        ArbeidssokerperiodeResponse sisteArbeidssoekerPeriode =  oppslagArbeidssoekerregisteretService.hentSisteArbeidssoekerPeriode(personRequest.getFnr());
        if(sisteArbeidssoekerPeriode == null) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "Person har ingen aktive arbeidssøkerperioder");
        }
        return sisteArbeidssoekerPeriode;
    }

    private Fnr hentIdentForEksternEllerIntern(Fnr queryParamFnr) {
        Fnr fnr;

        if (authService.erInternBruker()) {
            if (queryParamFnr == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mangler fnr");
            }
            fnr = queryParamFnr;
        } else if (authService.erEksternBruker()) {
            fnr = Fnr.of(authService.getInnloggerBrukerUid());
        } else {
            // Systembruker har ikke tilgang
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return fnr;
    }

}
