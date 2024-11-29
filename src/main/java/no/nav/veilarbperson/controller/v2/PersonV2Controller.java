package no.nav.veilarbperson.controller.v2;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.regoppslag.RegoppslagClient;
import no.nav.veilarbperson.client.regoppslag.RegoppslagResponseDTO;
import no.nav.veilarbperson.domain.*;
import no.nav.veilarbperson.service.AuthService;
import no.nav.veilarbperson.service.PersonV2Service;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/person")
public class PersonV2Controller {

    private final PersonV2Service personV2Service;
    private final AuthService authService;
    private final RegoppslagClient regoppslagClient;

    @Deprecated
    @GetMapping
    @Operation(summary = "Henter informasjon om en person fra PDL")
    public PersonV2Data hentPerson(@RequestParam("fnr") Fnr fnr) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);
        return personV2Service.hentFlettetPerson(new PersonFraPdlRequest(fnr, null));
    }

    @Deprecated
    @GetMapping("/malform")
    @Operation(summary = "Henter malform fra DIGDIR tjeneste")
    public Malform malform(@RequestParam("fnr") Fnr fnr) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);

        String malform = personV2Service.hentMalform(fnr);
        return new Malform(malform);
    }

    @Deprecated
    @GetMapping("/vergeOgFullmakt")
    @Operation(summary = "Henter informasjon om verge og fullmakt for en person fra PDL")
    public VergeData hentVergemaal(@RequestParam("fnr") Fnr fnr) throws IOException {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);
        return personV2Service.hentVerge(new PersonFraPdlRequest(fnr, null));
    }

    @Deprecated
    @GetMapping("/tolk")
    @Operation(summary = "Henter tolk informajon til en person fra PDL")
    public TilrettelagtKommunikasjonData hentSpraakTolk(@RequestParam("fnr") Fnr fnr) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);
        return personV2Service.hentSpraakTolkInfo(new PersonFraPdlRequest(fnr, null));
    }

    @Deprecated
    @GetMapping("/navn")
    @Operation(summary = "Henter navn til en person fra PDL")
    public PersonNavnV2 hentNavn(@RequestParam("fnr") Fnr fnr) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);
        return personV2Service.hentNavn(new PersonFraPdlRequest(fnr, null));
    }

    @Deprecated
    @GetMapping("/postadresse")
    @Operation(summary = "Henter postadresse til en person fra regoppslag")
    public RegoppslagResponseDTO hentPostadresse(@RequestParam("fnr") Fnr fnr) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);
        return regoppslagClient.hentPostadresse(fnr);
    }
}
