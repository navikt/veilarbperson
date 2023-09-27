package no.nav.veilarbperson.controller.v3;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.difi.HarLoggetInnRespons;
import no.nav.veilarbperson.client.kontoregister.HentKontoRequestDTO;
import no.nav.veilarbperson.client.kontoregister.KontoregisterClient;
import no.nav.veilarbperson.client.kontoregister.HentKontoResponseDTO;
import no.nav.veilarbperson.client.regoppslag.RegoppslagClient;
import no.nav.veilarbperson.client.regoppslag.RegoppslagResponseDTO;
import no.nav.veilarbperson.domain.*;
import no.nav.veilarbperson.service.AuthService;
import no.nav.veilarbperson.service.CvJobbprofilService;
import no.nav.veilarbperson.service.PersonV2Service;
import no.nav.veilarbperson.service.RegistreringService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v3/person")
public class PersonV3Controller {

    private final PersonV2Service personV2Service;
    private final AuthService authService;
    private final RegoppslagClient regoppslagClient;

    private final CvJobbprofilService cvJobbprofilService;

    private final RegistreringService registreringService;

    @PostMapping
    @Operation(summary = "Henter informasjon om en person fra PDL")
    public PersonV2Data hentPerson(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        return personV2Service.hentFlettetPerson(personRequest.getFnr());
    }
    @PostMapping("/aktorid")
    public AktoerId aktorid(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        return new AktoerId(authService.getAktorId(personRequest.getFnr()));
    }

    @PostMapping("/tilgangTilBruker")
    public boolean tilgangTilBruker(@RequestBody PersonRequest personRequest) {
        return authService.harLesetilgang(personRequest.getFnr());
    }
    @GetMapping("/geografisktilknytning")
    public GeografiskTilknytning geografisktilknytning(@RequestBody PersonRequest personRequest) {
        Fnr fodselsnummer = hentIdentForEksternEllerIntern(personRequest.getFnr());
        authService.sjekkLesetilgang(fodselsnummer);
        return personV2Service.hentGeografiskTilknytning(fodselsnummer);
    }

    @GetMapping("/cv_jobbprofil")
    public ResponseEntity<String> cvOgJobbprofil(@RequestBody PersonRequest personRequest) {
        return cvJobbprofilService.hentCvJobbprofilJson(personRequest.getFnr());
    }

    @GetMapping("/registrering")
    public ResponseEntity<String> registrering(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        return registreringService.hentRegistrering(personRequest.getFnr());
    }

    @PostMapping("/registrering/endringer")
    public ResponseEntity<String> endringIRegistreringdata(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        return registreringService.hentEndringIRegistreringsdata(personRequest.getFnr());
    }

    @PostMapping("/malform")
    @Operation(summary = "Henter malform fra DIGDIR tjeneste")
    public Malform malform(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());

        String malform = personV2Service.hentMalform(personRequest.getFnr());
        return new Malform(malform);
    }

    @PostMapping("/vergeOgFullmakt")
    @Operation(summary = "Henter informasjon om verge og fullmakt for en person fra PDL")
    public VergeOgFullmaktData hentVergemaalOgFullmakt(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        return personV2Service.hentVergeEllerFullmakt(personRequest.getFnr());
    }

    @PostMapping("/tolk")
    @Operation(summary = "Henter tolk informajon til en person fra PDL")
    public TilrettelagtKommunikasjonData hentSpraakTolk(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        return personV2Service.hentSpraakTolkInfo(personRequest.getFnr());
    }

    @PostMapping("/navn")
    @Operation(summary = "Henter navn til en person fra PDL")
    public PersonNavnV2 hentNavn(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        return personV2Service.hentNavn(personRequest.getFnr());
    }

    @PostMapping("/postadresse")
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
