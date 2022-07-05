package no.nav.veilarbperson.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.difi.HarLoggetInnRespons;
import no.nav.veilarbperson.domain.*;
import no.nav.veilarbperson.service.AuthService;
import no.nav.veilarbperson.service.CvJobbprofilService;
import no.nav.veilarbperson.service.PersonV2Service;
import no.nav.veilarbperson.service.RegistreringService;
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

    private final RegistreringService registreringService;

    @GetMapping("/{fodselsnummer}")
    @Operation(summary = "Henter informasjon om en person",
            description = "Denne tjenesten gjør kall mot flere baktjenester: " +
                    "Kodeverk, organisasjonenhet_v2, Digitalkontaktinformasjon_v1, Person_v3, Egenansatt_v1")
    public PersonDataTPS person(@PathVariable("fodselsnummer") Fnr fnr) {
        throw new ResponseStatusException(HttpStatus.GONE, "Bytt til v2 endepunkt");
    }

    @GetMapping("/aktorid")
    public AktoerId aktorid(@RequestParam("fnr") Fnr fnr) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);
        return new AktoerId(authService.getAktorId(fnr));
    }

    @GetMapping("/navn")
    @Operation(summary = "Henter navnet til en person")
    public PersonNavn navn(@RequestParam(value = "fnr", required = false) Fnr fnr) {
        throw new ResponseStatusException(HttpStatus.GONE, "Bytt til v2 endepunkt");
    }

    @GetMapping("/{fodselsnummer}/malform")
    @Operation(summary = "Henter målform til en person")
    public Malform malform(@PathVariable("fodselsnummer") Fnr fnr) {
        throw new ResponseStatusException(HttpStatus.GONE,
                "Bytt til v2 endepunkt");
    }

    @GetMapping("/{fodselsnummer}/tilgangTilBruker")
    public boolean tilgangTilBruker(@PathVariable("fodselsnummer") Fnr fodselsnummer) {
        return authService.harLesetilgang(fodselsnummer);
    }

    @GetMapping("/{fodselsnummer}/harNivaa4")
    public HarLoggetInnRespons harNivaa4(@PathVariable("fodselsnummer") Fnr fodselsnummer) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fodselsnummer);
        return personV2Service.hentHarNivaa4(fodselsnummer);
    }

    @GetMapping("/geografisktilknytning")
    public GeografiskTilknytning geografisktilknytning(@RequestParam(value = "fnr", required = false) Fnr fnr) {
        throw new ResponseStatusException(HttpStatus.GONE, "Bytt til v2 endepunkt");
    }

    @GetMapping("/cv_jobbprofil")
    public ResponseEntity<String> cvOgJobbprofil(@RequestParam(value = "fnr", required = false) Fnr fnr) {
        return cvJobbprofilService.hentCvJobbprofilJson(fnr);
    }

    @GetMapping("/registrering")
    public ResponseEntity<String> registrering(@RequestParam(value = "fnr") Fnr fnr) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);
        return registreringService.hentRegistrering(fnr);
    }

}
