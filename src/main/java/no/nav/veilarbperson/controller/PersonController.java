package no.nav.veilarbperson.controller;

import io.swagger.annotations.ApiOperation;
import no.nav.veilarbperson.client.person.domain.TpsPerson;
import no.nav.veilarbperson.domain.*;
import no.nav.veilarbperson.service.AuthService;
import no.nav.veilarbperson.service.PersonService;
import no.nav.veilarbperson.utils.PersonDataMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/person")
public class PersonController {

    private final PersonService personService;

    private final AuthService authService;

    public PersonController(PersonService personService, AuthService authService) {
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
        return personService.hentFlettetPerson(fnr);
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

        TpsPerson person = personService.hentPerson(fodselsnummer);
        return PersonDataMapper.hentNavn(person);
    }

    @GetMapping("/{fodselsnummer}/malform")
    @ApiOperation(value = "Henter målform til en person")
    public Malform malform(@PathVariable("fodselsnummer") String fnr) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fnr);

        TpsPerson person = personService.hentPerson(fnr);
        return new Malform(person.getMalform());
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
        return personService.hentGeografisktilknytning(fnr);
    }

}
