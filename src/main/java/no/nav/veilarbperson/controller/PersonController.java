package no.nav.veilarbperson.controller;

import io.swagger.annotations.ApiOperation;
import no.finn.unleash.UnleashContext;
import no.nav.common.featuretoggle.UnleashService;
import no.nav.veilarbperson.client.difi.HarLoggetInnRespons;
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

    private final UnleashService unleashService;

    public PersonController(PersonService personService, AuthService authService, UnleashService unleashService) {
        this.unleashService = unleashService;
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
    public PersonNavn navn(@RequestParam(value = "fnr", required = false) String fnr) {
        String fodselsnummer = hentIdentForEksternEllerIntern(fnr);

        authService.sjekkLesetilgang(fodselsnummer);

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

    @GetMapping("/{fodselsnummer}/harNivaa4")
    public HarLoggetInnRespons harNivaa4(@PathVariable("fodselsnummer") String fodselsnummer) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(fodselsnummer);



        if(unleashService.isEnabled("veilarb.sjekk.nivaa4")) {
            return personService.hentHarNivaa4(fodselsnummer);
        }

        HarLoggetInnRespons harLoggetInnRespons = new HarLoggetInnRespons();
        harLoggetInnRespons.setPersonidentifikator(fodselsnummer);
        harLoggetInnRespons.setHarbruktnivaa4(true);
        return harLoggetInnRespons;
    }

    @GetMapping("/geografisktilknytning")
    public GeografiskTilknytning geografisktilknytning(@RequestParam(value = "fnr", required = false) String fnr) {
        String fodselsnummer = hentIdentForEksternEllerIntern(fnr);
        authService.sjekkLesetilgang(fodselsnummer);
        return personService.hentGeografisktilknytning(fodselsnummer);
    }

    // TODO: Det er hårete å måtte skille på ekstern og intern
    //  Lag istedenfor en egen controller for interne operasjoner og en annen for eksterne
    private String hentIdentForEksternEllerIntern(String queryParamFnr) {
        String fnr;

        if (authService.erInternBruker()) {
            fnr = queryParamFnr;
        } else if (authService.erEksternBruker()) {
            fnr = authService.getInnloggerBrukerIdent();
        } else {
            // Systembruker har ikke tilgang
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (fnr == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mangler fnr");
        }

        return fnr;
    }

}
