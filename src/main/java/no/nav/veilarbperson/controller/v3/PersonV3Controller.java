package no.nav.veilarbperson.controller.v3;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.regoppslag.RegoppslagClient;
import no.nav.veilarbperson.client.regoppslag.RegoppslagResponseDTO;
import no.nav.veilarbperson.domain.*;
import no.nav.veilarbperson.service.AuthService;
import no.nav.veilarbperson.service.CvJobbprofilService;
import no.nav.veilarbperson.service.PersonV2Service;
import no.nav.veilarbperson.service.RegistreringService;
import no.nav.veilarbperson.utils.SecureLog;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static no.nav.veilarbperson.utils.SecureLog.secureLog;

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

    @PostMapping("/hent-person")
    @Operation(summary = "Henter informasjon om en person fra PDL")
    public PersonV2Data hentPerson(@RequestBody PdlRequest pdlRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(pdlRequest.getFnr());
        if (pdlRequest.getBehandlingsnummer() == null) {
            secureLog.info("Mottok request mot hent-person med behandlingsnummer null");
        }else {
            secureLog.info("Mottok request mot hent-person med behandlingsnummer : " + pdlRequest.getBehandlingsnummer());
        }
            //TODO: Remove log before merging/prodsetting!
        return personV2Service.hentFlettetPerson(pdlRequest);
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
    public GeografiskTilknytning geografisktilknytning(@RequestBody PdlRequest pdlRequest) {
        Fnr fodselsnummer = hentIdentForEksternEllerIntern(pdlRequest.getFnr());
        authService.sjekkLesetilgang(fodselsnummer);
        if (pdlRequest.getBehandlingsnummer() == null) {
            secureLog.info("Mottok request mot hent-geografisktilknytning med behandlingsnummer null");
        } else {
            secureLog.info("Mottok request mot hent-geografisktilknytning med behandlingsnummer : " + pdlRequest.getBehandlingsnummer());
        }
        return personV2Service.hentGeografiskTilknytning(pdlRequest);
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
    public VergeOgFullmaktData hentVergemaalOgFullmakt(@RequestBody PdlRequest pdlRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(pdlRequest.getFnr());
        if (pdlRequest.getBehandlingsnummer() == null) {
            secureLog.info("Mottok request mot hent-vergeOgFullmakt med behandlingsnummer null");
        } else {
            secureLog.info("Mottok request mot hent-vergeOgFullmakt med behandlingsnummer : " + pdlRequest.getBehandlingsnummer());
        }
        return personV2Service.hentVergeEllerFullmakt(pdlRequest);
    }

    @PostMapping("/person/hent-tolk")
    @Operation(summary = "Henter tolk informajon til en person fra PDL")
    public TilrettelagtKommunikasjonData hentSpraakTolk(@RequestBody PdlRequest pdlRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(pdlRequest.getFnr());
        if (pdlRequest.getBehandlingsnummer() == null) {
            secureLog.info("Mottok request mot hent-tolk med behandlingsnummer null");
        } else {
            secureLog.info("Mottok request mot hent-tolk med behandlingsnummer : " + pdlRequest.getBehandlingsnummer());
        }
        return personV2Service.hentSpraakTolkInfo(pdlRequest);
    }

    @PostMapping("/person/hent-navn")
    @Operation(summary = "Henter navn til en person fra PDL")
    public PersonNavnV2 hentNavn(@RequestBody PdlRequest pdlRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(pdlRequest.getFnr());
        return personV2Service.hentNavn(pdlRequest);
    }

    @PostMapping("/person/hent-postadresse")
    @Operation(summary = "Henter postadresse til en person fra regoppslag")
    public RegoppslagResponseDTO hentPostadresse(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        return regoppslagClient.hentPostadresse(personRequest.getFnr());
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