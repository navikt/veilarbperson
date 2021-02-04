package no.nav.veilarbperson.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import no.nav.common.json.JsonUtils;
import no.nav.common.rest.client.RestUtils;
import no.nav.common.types.identer.Fnr;
import no.nav.common.utils.StringUtils;
import no.nav.veilarbperson.client.pam.CvIkkeTilgang;
import no.nav.veilarbperson.client.pam.PamClient;
import no.nav.veilarbperson.client.person.domain.TpsPerson;
import okhttp3.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
public class CvJobbprofilService {

    private final static String EMPTY_JSON_OBJ = "{}";

    private final AuthService authService;

    private final PersonService personService;

    private final PamClient pamClient;

    public String hentCvJobbprofilJson(Fnr fnr) {
        return pamClient.hentCvOgJobbprofilJson(fnr);
    }

    @SneakyThrows
    public ResponseEntity<String> hentCvJobbprofilJsonV2(Fnr fnr) {
        if (!authService.erInternBruker() || !authService.harLesetilgang(fnr)) {
            return ikkeTilgangResponse(CvIkkeTilgang.IKKE_TILGANG_TIL_BRUKER);
        }

        TpsPerson person = personService.hentPerson(fnr);

        if (StringUtils.notNullOrEmpty(person.getDodsdato())) {
            return ikkeTilgangResponse(CvIkkeTilgang.BRUKER_ER_DOED);
        }

        // TODO: Sjekk at bruker er under oppfølging
        // TODO: Sjekk at bruker er manuell (Gjør avklaring med PAM om hvordan sjekk på manuell skal gjøres)
        //   Ikke tilgang hvis bruker ikke har satt hjemmel og ikke er manuell.

        Response cvJobbprofilResponse = pamClient.hentCvOgJobbprofilJsonV2(fnr);

        if (cvJobbprofilResponse.code() == HttpStatus.NOT_FOUND.value()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else if (cvJobbprofilResponse.code() == HttpStatus.NOT_ACCEPTABLE.value()) {
            return ikkeTilgangResponse(CvIkkeTilgang.BRUKER_IKKE_GODKJENT_SAMTYKKE);
        }

        String cvJobbprofilJson = RestUtils.getBodyStr(cvJobbprofilResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (cvJobbprofilJson.trim().equals(EMPTY_JSON_OBJ)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(cvJobbprofilJson);
    }

    private static ResponseEntity<String> ikkeTilgangResponse(CvIkkeTilgang cvIkkeTilgang) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(JsonUtils.toJson(new CvIkkeTilgangResponse(cvIkkeTilgang)));
    }

    @Value
    private static class CvIkkeTilgangResponse {
        CvIkkeTilgang ikkeTilgangStatus;
    }

}
