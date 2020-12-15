package no.nav.veilarbperson.controller;

import io.swagger.annotations.ApiOperation;
import no.nav.common.featuretoggle.UnleashService;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.pdl.HentPdlPerson;
import no.nav.veilarbperson.client.pdl.PdlPersonData;
import no.nav.veilarbperson.client.person.domain.TpsPerson;
import no.nav.veilarbperson.domain.AktoerId;
import no.nav.veilarbperson.domain.GeografiskTilknytning;
import no.nav.veilarbperson.domain.Malform;
import no.nav.veilarbperson.service.AuthService;
import no.nav.veilarbperson.service.PdlService;
import no.nav.veilarbperson.utils.PdlPersonDataMappper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/pdl")
public class PdlController {

    private final PdlService pdlService;
    private final AuthService authService;

    @Autowired
    public PdlController(PdlService pdlService, AuthService authService) {
        this.pdlService = pdlService;
        this.authService = authService;
    }

    @GetMapping("/{fodselsnummer}")
    @ApiOperation(value = "Henter informasjon om en person fra PDL")
    public PdlPersonData hentPerson(@RequestParam("fnr") String fnr) throws Exception {
        // throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Midlertidig skrudd av");
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(Fnr.of(fnr));
        return pdlService.hentFlettetPerson(fnr, authService.getInnloggetBrukerToken());
    }

    @GetMapping("/navn")
    @ApiOperation(value = "Henter navnet til en person")
    public HentPdlPerson.Navn navn(@RequestParam(value = "fnr", required = false) Fnr fnr) {
        Fnr fodselsnummer = hentIdentForEksternEllerIntern(fnr);

        authService.sjekkLesetilgang(fodselsnummer);

        HentPdlPerson.PdlPerson person = pdlService.hentPerson(fodselsnummer.get());
        return PdlPersonDataMappper.hentNavn(person.getNavn());
    }

    @GetMapping("/{fodselsnummer}/malform")
    @ApiOperation(value = "Henter målform til en person fra KRR")
    public Malform malform(@PathVariable("fodselsnummer") Fnr fnr) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);

        String malform = pdlService.hentMalform(fnr);
        return new Malform(malform);
    }

    @GetMapping("/geografisktilknytning")
    public GeografiskTilknytning geografisktilknytning(@RequestParam(value = "fnr", required = false) Fnr fnr) {
        Fnr fodselsnummer = hentIdentForEksternEllerIntern(fnr);
        authService.sjekkLesetilgang(fodselsnummer);

        HentPdlPerson.PdlPerson personDataFraPdl = pdlService.hentPerson(fodselsnummer.get());

        return new GeografiskTilknytning(personDataFraPdl.getGeografiskTilknytning().getGtKommune());
    }

    private Fnr hentIdentForEksternEllerIntern(Fnr queryParamFnr) {
        Fnr fnr;

        if (authService.erInternBruker()) {
            if (queryParamFnr == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mangler fnr");
            }
            fnr = queryParamFnr;
        } else if (authService.erEksternBruker()) {
            fnr = Fnr.of(authService.getInnloggerBrukerSubject());
        } else {
            // Systembruker har ikke tilgang
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return fnr;
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
