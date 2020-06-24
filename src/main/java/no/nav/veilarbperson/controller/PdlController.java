package no.nav.veilarbperson.controller;

import no.nav.veilarbperson.client.pdl.HentPersonData;
import no.nav.veilarbperson.service.PdlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pdl")
public class PdlController {

    private final PdlService pdlService;

    @Autowired
    public PdlController(PdlService pdlService) {
        this.pdlService = pdlService;
    }

    @GetMapping
    public HentPersonData.PdlPerson hentPerson(@RequestParam("fnr") String fnr) {
        return pdlService.hentPerson(fnr);
    }

    @PostMapping("/graphql")
    public String gqlRequest(@RequestBody String gqlRequestJson) {
        return pdlService.executeGqlRequest(gqlRequestJson);
    }

}
