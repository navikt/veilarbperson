package no.nav.veilarbperson.controller;

import no.nav.veilarbperson.client.pdl.HentPersonData;
import no.nav.veilarbperson.service.PdlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Midlertidig skrudd av");
//        return pdlService.hentPerson(fnr);
    }

    @PostMapping("/graphql")
    public String gqlRequest(@RequestBody String gqlRequestJson) {
        // TODO: Hvis vi skal over på AAD så må vi kalle PDL med 2 systembrukertokens og gjøre tilgangskontroll selv.
        //  Dette endepunktet kan ikke bli brukt sikkert med et slikt oppsett siden det blir for usikkert å prøve å parse GQL requestet for å finne bruker å gjøre tilgangskontroll på
        //  Gjør istedenfor slik som i hentPerson hvor vi kan kontrollere brukeren som det skal hentes data fra
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Permanent skrudd av");
//        return pdlService.executeGqlRequest(gqlRequestJson);
    }

}
