package no.nav.veilarbperson.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.difi.HarLoggetInnRespons;
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

    @Deprecated
    @GetMapping("/aktorid")
    public AktoerId aktorid(@RequestParam("fnr") Fnr fnr) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);
        return new AktoerId(authService.getAktorId(fnr));
    }

    @Deprecated
    @GetMapping("/navn")
    @Operation(summary = "Henter navnet til en person")
    public PersonNavn navn(@RequestParam(value = "fnr", required = false) Fnr fnr) {
        throw new ResponseStatusException(HttpStatus.GONE, "Bytt til v2 endepunkt");
    }

    @Deprecated
    @GetMapping("/{fodselsnummer}/malform")
    @Operation(summary = "Henter målform til en person")
    public Malform malform(@PathVariable("fodselsnummer") Fnr fnr) {
        throw new ResponseStatusException(HttpStatus.GONE,
                "Bytt til v2 endepunkt");
    }
    @Deprecated
    @GetMapping("/{fodselsnummer}/tilgangTilBruker")
    public boolean tilgangTilBruker(@PathVariable("fodselsnummer") Fnr fodselsnummer) {
        return authService.harLesetilgang(fodselsnummer);
    }

    // TODO: 21/08/2023 denne skal slettes etter vi har ryddet opp i kode i de andre appene da dkif slutter å tilby tjenesten
    @GetMapping("/{fodselsnummer}/harNivaa4")
    public HarLoggetInnRespons harNivaa4(@PathVariable("fodselsnummer") Fnr fodselsnummer) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fodselsnummer);
        return new HarLoggetInnRespons()
                .setErRegistrertIdPorten(true)
                .setHarbruktnivaa4(true)
                .setPersonidentifikator(fodselsnummer);
    }

    @Deprecated
    @GetMapping("/geografisktilknytning")
    public GeografiskTilknytning geografisktilknytning(@RequestParam(value = "fnr", required = false) Fnr fnr) {
        Fnr fodselsnummer = hentIdentForEksternEllerIntern(fnr);
        authService.sjekkLesetilgang(fodselsnummer);
        return personV2Service.hentGeografiskTilknytning(new PersonFraPdlRequest(fodselsnummer, null));
    }

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