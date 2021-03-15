package no.nav.veilarbperson.controller;

import io.swagger.annotations.ApiOperation;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.pdl.PersonV2Data;
import no.nav.veilarbperson.domain.Malform;
import no.nav.veilarbperson.service.AuthService;
import no.nav.veilarbperson.service.PersonV2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{fodselsnummer}")
    @ApiOperation(value = "Henter informasjon om en person fra PDL")
    public PersonV2Data hentPerson(@PathVariable("fodselsnummer") String fnr) throws Exception {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(Fnr.of(fnr));
        return personV2Service.hentFlettetPerson(fnr, authService.getInnloggetBrukerToken());
    }

    @GetMapping("/{fodselsnummer}/malform")
    @ApiOperation(value = "Henter malform fra DKIF tjeneste")
    public Malform malform(@PathVariable("fodselsnummer") Fnr fnr) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);

        String malform = personV2Service.hentMalform(fnr);
        return new Malform(malform);
    }
}
