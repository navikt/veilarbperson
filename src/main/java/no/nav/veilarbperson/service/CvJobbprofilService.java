package no.nav.veilarbperson.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import no.nav.common.json.JsonUtils;
import no.nav.common.rest.client.RestUtils;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.pam.CvIkkeTilgang;
import no.nav.veilarbperson.client.pam.PamClient;
import no.nav.veilarbperson.client.veilarboppfolging.UnderOppfolging;
import no.nav.veilarbperson.client.veilarboppfolging.VeilarboppfolgingClient;
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

    private final VeilarboppfolgingClient veilarboppfolgingClient;

    private final PamClient pamClient;

    public String hentCvJobbprofilJson(Fnr fnr) {
        return pamClient.hentCvOgJobbprofilJson(fnr);
    }

    @SneakyThrows
    public ResponseEntity<String> hentCvJobbprofilJsonV2(Fnr fnr) {
        if (!authService.erInternBruker() || !authService.harLesetilgang(fnr)) {
            return ikkeTilgangResponse(CvIkkeTilgang.IKKE_TILGANG_TIL_BRUKER);
        }

        UnderOppfolging underOppfolging = veilarboppfolgingClient.hentUnderOppfolgingStatus(fnr);

        if (!underOppfolging.isUnderOppfolging()) {
            return ikkeTilgangResponse(CvIkkeTilgang.BRUKER_IKKE_UNDER_OPPFOLGING);
        }

        Response cvJobbprofilResponse = pamClient.hentCvOgJobbprofilJsonV2(fnr, underOppfolging.isErManuell());

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
    static class CvIkkeTilgangResponse {
        CvIkkeTilgang ikkeTilgangStatus;
    }

}
