package no.nav.veilarbperson.controller;

import lombok.RequiredArgsConstructor;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.nom.SkjermetClient;
import no.nav.veilarbperson.client.pdl.HentPerson;
import no.nav.veilarbperson.client.pdl.PdlClient;
import no.nav.veilarbperson.client.pdl.domain.PdlRequest;
import no.nav.veilarbperson.domain.PersonVisittkortData;
import no.nav.veilarbperson.service.AuthService;
import no.nav.veilarbperson.utils.PersonVisittkortDataMapper;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequiredArgsConstructor
public class PersonGraphQLController {

    private final PdlClient pdlClient;
    private final SkjermetClient skjermetClient;
    private final AuthService authService;

    @QueryMapping
    public PersonVisittkortData person(
            @Argument String fnr,
            @Argument String behandlingsnummer) {

        // 🔴 RØD SONE — implementer og forstå selv før du merger:
        //
        // TODO 1: Valider fnr-input. Kast GraphQL-feil med kode BAD_USER_INPUT ved ugyldig/manglende input.
        //         Bruk: throw new GraphQlException("Ugyldig fnr") eller tilsvarende Spring GraphQL-mekanisme.
        //         Ikke la Fnr.of() kaste NPE/IllegalArgumentException ukontrollert (gir INTERNAL_ERROR til klient).
        //
        // TODO 2: Kall authService.stoppHvisEksternBruker() for å blokkere eksterne brukere.
        //         Se PersonV3Controller for referanseimplementasjon. Forstå hva dette kallet faktisk gjør.
        //
        // TODO 3: Kall authService.sjekkLesetilgang(validertFnr) for å sjekke at innlogget veileder
        //         har lesetilgang til personen. Dette er tilgangskontroll mot poao-tilgang + skjerming.
        //         Se PersonV3Controller for referanseimplementasjon. Forstå hva dette kallet faktisk gjør.

        Fnr validertFnr = Fnr.of(fnr);

        HentPerson.Person person = pdlClient.hentPerson(new PdlRequest(validertFnr, behandlingsnummer));
        if (person == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        // Merk: Kaller kun PDL og Skjermet — ikke Kodeverk, Digdir, Norg2 eller Representasjon.
        // Dette er tilstrekkelig for de 10 feltene veilarbvisittkortfs bruker.
        boolean erSkjermet = Boolean.TRUE.equals(skjermetClient.hentSkjermet(validertFnr));

        return PersonVisittkortDataMapper.tilPersonVisittkortData(person, erSkjermet);
    }
}
