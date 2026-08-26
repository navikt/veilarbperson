package no.nav.veilarbperson.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.domain.*;
import no.nav.veilarbperson.service.AuthService;
import no.nav.veilarbperson.service.CvJobbprofilService;
import no.nav.veilarbperson.service.PersonV2Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/person")
@AllArgsConstructor
public class PersonController {
    private final PersonV2Service personV2Service;

    private final AuthService authService;

    private final CvJobbprofilService cvJobbprofilService;

    /**
     * @deprecated Bruk POST /api/v3/person/hent-aktorid
     */
    @Deprecated
    @GetMapping("/aktorid")
    public AktoerId aktorid(@RequestParam("fnr") Fnr fnr) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);
        return new AktoerId(authService.getAktorId(fnr));
    }

    /**
     * @deprecated Bruk POST /api/v3/person/hent-navn
     */
    @Deprecated
    @GetMapping("/navn")
    @Operation(summary = "Henter navnet til en person")
    public ResponseEntity<?> navn(@RequestParam(value = "fnr", required = false) Fnr fnr) {
        throw new ResponseStatusException(HttpStatus.GONE, "Bytt til v2 endepunkt");
    }

    /**
     * @deprecated Bruk POST /api/v3/person/hent-malform
     */
    @Deprecated
    @GetMapping("/{fodselsnummer}/malform")
    @Operation(summary = "Henter målform til en person")
    public Malform malform(@PathVariable("fodselsnummer") Fnr fnr) {
        throw new ResponseStatusException(HttpStatus.GONE,
                "Bytt til v2 endepunkt");
    }
    /**
     * @deprecated Bruk POST /api/v3/person/hent-tilgangTilBruker
     */
    @Deprecated
    @GetMapping("/{fodselsnummer}/tilgangTilBruker")
    public boolean tilgangTilBruker(@PathVariable("fodselsnummer") Fnr fodselsnummer) {
        return authService.harLesetilgang(fodselsnummer);
    }

    /**
     * @deprecated Bruk POST /api/v3/person/hent-geografisktilknytning
     */
    @Deprecated
    @GetMapping("/geografisktilknytning")
    public GeografiskTilknytning geografisktilknytning(@RequestParam(value = "fnr", required = false) Fnr fnr) {
        Fnr fodselsnummer = hentIdentForEksternEllerIntern(fnr);
        authService.sjekkLesetilgang(fodselsnummer);
        return personV2Service.hentGeografiskTilknytning(new PersonFraPdlRequest(fodselsnummer, null));
    }

    /**
     * @deprecated Bruk POST /api/v3/person/hent-cv_jobbprofil
     */
    @Deprecated
    @GetMapping("/cv_jobbprofil")
    public ResponseEntity<String> cvOgJobbprofil(@RequestParam(value = "fnr", required = false) Fnr fnr) {
        return cvJobbprofilService.hentCvJobbprofilJson(fnr);
    }

    // TODO: Det er hårete å måtte skille på ekstern og intern
    //  Lag istedenfor en egen controller for interne operasjoner og en annen for eksterne
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
