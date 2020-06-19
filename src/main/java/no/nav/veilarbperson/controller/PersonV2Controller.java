package no.nav.veilarbperson.controller;

import no.nav.veilarbperson.client.pdl.HentPersonData;
import no.nav.veilarbperson.service.PdlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/person")
public class PersonV2Controller {

    private final PdlService pdlService;

    @Autowired
    public PersonV2Controller(PdlService pdlService) {
        this.pdlService = pdlService;
    }

    @GetMapping
    public HentPersonData.PdlPerson hentPerson(@RequestParam("fnr") String fnr) {
        return pdlService.hentPerson(fnr);
    }

}
