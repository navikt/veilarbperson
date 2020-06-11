package no.nav.veilarbperson.controller;

import io.swagger.annotations.ApiOperation;
import io.vavr.control.Try;
import no.nav.veilarbperson.service.AuthService;
import no.nav.veilarbperson.service.PersonFletterService;
import no.nav.veilarbperson.client.tps.PersonService;
import no.nav.veilarbperson.utils.MapExceptionUtil;
import no.nav.veilarbperson.domain.person.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/person")
public class PersonController {

    private final PersonFletterService personFletterService;

    private final PersonService personService;

    private final AuthService authService;

    public PersonController(PersonFletterService personFletterService, PersonService personService, AuthService authService) {
        this.personFletterService = personFletterService;
        this.personService = personService;
        this.authService = authService;
    }

    @GetMapping("/{fodselsnummer}")
    @ApiOperation(value = "Henter informasjon om en person",
            notes = "Denne tjenesten gjør kall mot flere baktjenester: " +
                    "Kodeverk, organisasjonenhet_v2, Digitalkontaktinformasjon_v1, Person_v3, Egenansatt_v1")
    public PersonData person(@PathVariable("fodselsnummer") String fnr) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);

        return Try.of(() -> personFletterService.hentPerson(fnr)).getOrElseThrow(MapExceptionUtil::map);
    }

    @GetMapping("/aktorid")
    public AktoerId aktorid(@RequestParam("fnr") String fnr){
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);
        return new AktoerId(authService.getAktorId(fnr));
    }

    @GetMapping("/navn")
    @ApiOperation(value = "Henter navnet til en person")
    public PersonNavn navn(@RequestParam("fnr") String fnr) {
        // Fnr fra query param kan kun brukes av interne brukere, eksterne må bruke token
        final String fodselsnummer = (fnr != null && authService.erInternBruker())
                ? fnr
                : authService.getInnloggerBrukerIdent();

        authService.sjekkLesetilgang(fnr);

        return Try.of(() -> personService.hentPerson(fodselsnummer))
                .map(PersonNavn::fraPerson)
                .getOrElseThrow(MapExceptionUtil::map);
    }

    @GetMapping("/{fodselsnummer}/malform")
    @ApiOperation(value = "Henter målform til en person")
    public Malform malform(@PathVariable("fodselsnummer") String fnr) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);

        return Try.of(() -> personService.hentPerson(fnr))
                .map(PersonData::getMalform)
                .map(Malform::new)
                .getOrElseThrow(MapExceptionUtil::map);
    }

    @GetMapping("/{fodselsnummer}/tilgangTilBruker")
    public boolean tilgangTilBruker(@PathVariable("fodselsnummer") String fodselsnummer) {
        return authService.harLesetilgang(fodselsnummer);
    }

    @GetMapping("/geografisktilknytning")
    public GeografiskTilknytning geografisktilknytning(@RequestParam("fnr") String fodselsnummer) {
        String fnr;

        if (authService.erEksternBruker()) {
            fnr = authService.getInnloggerBrukerIdent();
        } else if (authService.erInternBruker()) {
            fnr = fodselsnummer;
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        authService.sjekkLesetilgang(fnr);

        return Try.of(() -> personFletterService.hentGeografisktilknytning(fnr))
                .getOrElseThrow(MapExceptionUtil::map);
    }


}
