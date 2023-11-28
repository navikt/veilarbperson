package no.nav.veilarbperson.controller.v2;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.kontoregister.HentKontoRequestDTO;
import no.nav.veilarbperson.client.kontoregister.KontoregisterClient;
import no.nav.veilarbperson.client.kontoregister.HentKontoResponseDTO;
import no.nav.veilarbperson.client.regoppslag.RegoppslagClient;
import no.nav.veilarbperson.client.regoppslag.RegoppslagResponseDTO;
import no.nav.veilarbperson.domain.*;
import no.nav.veilarbperson.service.AuthService;
import no.nav.veilarbperson.service.PersonV2Service;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/person")
public class PersonV2Controller {

    private final PersonV2Service personV2Service;
    private final AuthService authService;
    private final RegoppslagClient regoppslagClient;

    private final KontoregisterClient kontoregisterClient;

    @Deprecated
    @GetMapping
    @Operation(summary = "Henter informasjon om en person fra PDL")
    public PersonV2Data hentPerson(@RequestParam("fnr") Fnr fnr) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);
        PdlRequest pdlRequest = new PdlRequest(fnr, null);
        return personV2Service.hentFlettetPerson(pdlRequest);
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
    public VergeOgFullmaktData hentVergemaalOgFullmakt(@RequestParam("fnr") Fnr fnr) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);
        PdlRequest pdlRequestVergeEllerFullmakt = new PdlRequest(fnr, null);
        return personV2Service.hentVergeEllerFullmakt(pdlRequestVergeEllerFullmakt);
    }

    @Deprecated
    @GetMapping("/tolk")
    @Operation(summary = "Henter tolk informajon til en person fra PDL")
    public TilrettelagtKommunikasjonData hentSpraakTolk(@RequestParam("fnr") Fnr fnr) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);
        return personV2Service.hentSpraakTolkInfo(new PdlRequest(fnr, null));
    }

    @Deprecated
    @GetMapping("/navn")
    @Operation(summary = "Henter navn til en person fra PDL")
    public PersonNavnV2 hentNavn(@RequestParam("fnr") Fnr fnr) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);
        return personV2Service.hentNavn(new PdlRequest(fnr, null));
    }

    @Deprecated
    @GetMapping("/postadresse")
    @Operation(summary = "Henter postadresse til en person fra regoppslag")
    public RegoppslagResponseDTO hentPostadresse(@RequestParam("fnr") Fnr fnr) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);
        return regoppslagClient.hentPostadresse(fnr);
    }
    @PostMapping
    @Operation(summary = "Henter kontonummer fra Kontoregister")
    public HentKontoResponseDTO hentKontonummerFraKontoregister(@RequestParam("kontohaver") HentKontoRequestDTO kontohaver) {
        Fnr fnr = new Fnr(kontohaver.getKontohaver());
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);
        return kontoregisterClient.hentKontonummer(kontohaver);
    }

}
