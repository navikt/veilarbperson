package no.nav.veilarbperson.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.ArbeidssokerperiodeResponse;
import no.nav.veilarbperson.client.pdl.HentPerson;
import no.nav.veilarbperson.client.regoppslag.RegoppslagClient;
import no.nav.veilarbperson.client.regoppslag.RegoppslagResponseDTO;
import no.nav.veilarbperson.domain.*;
import no.nav.veilarbperson.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v3")
public class PersonController {

    private final PersonService personService;
    private final AuthService authService;
    private final RegoppslagClient regoppslagClient;

    private final CvJobbprofilService cvJobbprofilService;

    private final OppslagArbeidssoekerregisteretService oppslagArbeidssoekerregisteretService;

    @PostMapping("/hent-person-tilgangsstyrt")
    @Operation(summary = "Henter informasjon om en person fra PDL")
    public PersonData hentPersonTilgangsstyrt(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        return personService.hentFlettetPersonTilgangsstyrt(personRequest);
    }

    @PostMapping("/person/hent-aktorid")
    @Operation(summary = "Henter aktørId til person")
    public AktoerId aktorid(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        return new AktoerId(authService.getAktorId(personRequest.getFnr()));
    }

    @PostMapping("/person/hent-tilgangTilBruker")
    @Operation(summary = "Sjekk om innlogget person har tilgang til person")
    public boolean tilgangTilBruker(@RequestBody PersonRequest personRequest) {
        return authService.harLesetilgang(personRequest.getFnr());
    }

    @PostMapping("/person/hent-geografisktilknytning")
    @Operation(summary = "Henter persons geografisk tilknytning")
    public GeografiskTilknytning geografisktilknytning(@RequestBody PersonRequest personRequest) {
        Fnr fodselsnummer = hentIdentForEksternEllerIntern(personRequest.getFnr());
        authService.sjekkLesetilgang(fodselsnummer);
        return personService.hentGeografiskTilknytning(personRequest);
    }

    @PostMapping("/person/hent-cv_jobbprofil")
    @Operation(summary = "Henter persons cv og jobbprofil")
    public ResponseEntity<String> cvOgJobbprofil(@RequestBody PersonRequest personRequest) {
        return cvJobbprofilService.hentCvJobbprofilJson(personRequest.getFnr());
    }

    @PostMapping("/person/hent-malform")
    @Operation(summary = "Henter malform fra DIGDIR tjeneste")
    public Malform malform(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        String malform = personService.hentMalform(personRequest.getFnr());
        return new Malform(malform);
    }

    @PostMapping("/person/hent-vergeOgFullmakt")
    @Operation(summary = "Henter informasjon om verge og fullmakt for en person fra PDL")
    public VergeData hentVergemaal(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        return personService.hentVerge(personRequest);
    }

    @PostMapping("/person/hent-fullmakt")
    @Operation(summary = "Henter informasjon for fullmakt fra representasjon")
    public FullmaktDTO hentFullmakt(@RequestBody PersonRequest personRequest) throws IOException {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        return personService.hentFullmakt(personRequest);
    }

    @PostMapping("/person/hent-tolk")
    @Operation(summary = "Henter tolk informajon til en person fra PDL")
    public TilrettelagtKommunikasjonData hentSpraakTolk(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        return personService.hentSpraakTolkInfo(personRequest);
    }

    @PostMapping("/person/hent-navn")
    @Operation(summary = "Henter navn til en person fra PDL")
    public PersonNavn hentNavn(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        return personService.hentNavn(personRequest);
    }

    @PostMapping("/person/hent-adressebeskyttelse")
    @Operation(summary = "Henter gradering på adressebeskyttelse til en person fra PDL")
    public HentPerson.Adressebeskyttelse hentAdressebeskyttelse(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        return personService.hentAdressebeskyttelse(personRequest);
    }

    @PostMapping("/person/hent-postadresse")
    @Operation(summary = "Henter postadresse til en person fra regoppslag")
    public RegoppslagResponseDTO hentPostadresse(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        return regoppslagClient.hentPostadresse(personRequest.getFnr());
    }

    @PostMapping("/person/hent-siste-opplysninger-om-arbeidssoeker-med-profilering")
    @Operation(summary = "Henter svarene fra den siste arbeidssøkerregistrering til en person i en aktiv arbeidssøkerperiode")
    public OpplysningerOmArbeidssoekerMedProfilering hentSisteOpplysningerOmArbeidssoerkerOgProfilering(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        return oppslagArbeidssoekerregisteretService.hentSisteOpplysningerOmArbeidssoekerMedProfilering(personRequest.getFnr());
    }

    @PostMapping("/person/hent-siste-aktiv-arbeidssoekerperiode")
    @Operation(summary = "Henter siste aktiv arbeidssøkerperiode på en person")
    public ArbeidssokerperiodeResponse hentSisteArbeidssoekerperiode(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        ArbeidssokerperiodeResponse sisteArbeidssoekerPeriode =  oppslagArbeidssoekerregisteretService.hentSisteArbeidssoekerPeriode(personRequest.getFnr());
        if(sisteArbeidssoekerPeriode == null) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "Person har ingen aktive arbeidssøkerperioder");
        }
        return sisteArbeidssoekerPeriode;
    }

    @PostMapping("/person/hent-foedselsdato")
    @Operation(summary = "Henter fødselsdato til en person fra PDL")
    public Foedselsdato hentFoedselsdato(@RequestBody PersonRequest personRequest) {
        authService.stoppHvisEksternBruker();
        authService.sjekkLesetilgang(personRequest.getFnr());
        return personService.hentFoedselsdato(personRequest);
    }

    private Fnr hentIdentForEksternEllerIntern(Fnr queryParamFnr) {
        Fnr fnr;

        if (authService.erInternBruker()) {
            if (queryParamFnr == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mangler fnr");
            }
            fnr = queryParamFnr;
        } else if (authService.erEksternBruker()) {
            fnr = Fnr.of(authService.getInnloggerBrukerUid());
        } else {
            // Systembruker har ikke tilgang
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return fnr;
    }

}
