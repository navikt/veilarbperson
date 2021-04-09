package no.nav.veilarbperson.controller;

import io.swagger.annotations.ApiOperation;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.domain.PersonV2Data;
import no.nav.veilarbperson.domain.PersonNavnV2;
import no.nav.veilarbperson.domain.TilrettelagtKommunikasjonData;
import no.nav.veilarbperson.domain.VergeOgFullmaktData;
import no.nav.veilarbperson.domain.Malform;
import no.nav.veilarbperson.service.AuthService;
import no.nav.veilarbperson.service.PersonV2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/person")
public class PersonV2Controller {

    private final PersonV2Service personV2Service;
    private final AuthService authService;

    @Autowired
    public PersonV2Controller(PersonV2Service personV2Service, AuthService authService) {
        this.personV2Service = personV2Service;
        this.authService = authService;
    }

    @GetMapping
    @ApiOperation(value = "Henter informasjon om en person fra PDL")
    public PersonV2Data hentPerson(@RequestParam("fnr") Fnr fnr) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);
        return personV2Service.hentFlettetPerson(fnr, authService.getInnloggetBrukerToken());
    }

    @GetMapping("/malform")
    @ApiOperation(value = "Henter malform fra DKIF tjeneste")
    public Malform malform(@RequestParam("fnr") Fnr fnr) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);

        String malform = personV2Service.hentMalform(fnr);
        return new Malform(malform);
    }

    @GetMapping("/vergeOgFullmakt")
    @ApiOperation(value = "Henter informasjon om verge og fullmakt for en person fra PDL")
    public VergeOgFullmaktData hentVergemaalOgFullmakt(@RequestParam("fnr") Fnr fnr) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);
        return personV2Service.hentVergeEllerFullmakt(fnr, authService.getInnloggetBrukerToken());
    }

    @GetMapping("/tolk")
    @ApiOperation(value = "Henter tolk informajon til en person fra PDL")
    public TilrettelagtKommunikasjonData hentSpraakTolk(@RequestParam("fnr") Fnr fnr) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);
        return personV2Service.hentSpraakTolkInfo(fnr, authService.getInnloggetBrukerToken());
    }

    @GetMapping("/navn")
    @ApiOperation(value = "Henter navn til en person fra PDL")
    public PersonNavnV2 hentNavn(@RequestParam("fnr") Fnr fnr) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);
        return personV2Service.hentNavn(fnr, authService.getInnloggetBrukerToken());
    }
}
