package no.nav.veilarbperson.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CvJobbprofilService {

    private final static String EMPTY_JSON_OBJ = "{}";

    private final AuthService authService;

    private final VeilarboppfolgingClient veilarboppfolgingClient;

    private final PamClient pamClient;

    @SneakyThrows
    public ResponseEntity<String> hentCvJobbprofilJson(Fnr fnr) {
        boolean erInnloggetBrukerEkstern = !authService.erInternBruker();
        boolean harIkkeLesetilgangTilBruker = !authService.harLesetilgang(fnr);

        if (erInnloggetBrukerEkstern || harIkkeLesetilgangTilBruker) {
            if(erInnloggetBrukerEkstern) {
                log.warn("Kunne ikke hente CV- og jobbprofil for bruker. Årsak: innlogget bruker er ekstern.");
            }

            if(harIkkeLesetilgangTilBruker) {
                log.warn("Kunne ikke hente CV- og jobbprofil for bruker. Årsak: innlogget bruker har ikke lesetilgang til brukeren som det ble forsøkt hentet CV- og jobbprofil for.");
            }

            return ikkeTilgangResponse(CvIkkeTilgang.IKKE_TILGANG_TIL_BRUKER);
        }

        UnderOppfolging underOppfolging = veilarboppfolgingClient.hentUnderOppfolgingStatus(fnr);

        if (!underOppfolging.isUnderOppfolging()) {
            log.warn("Kunne ikke hente CV- og jobbprofil for bruker. Årsak: brukeren er ikke under oppfølging.");
            return ikkeTilgangResponse(CvIkkeTilgang.BRUKER_IKKE_UNDER_OPPFOLGING);
        }

        try (Response cvJobbprofilResponse = pamClient.hentCvOgJobbprofil(fnr, underOppfolging.isErManuell())) {
            if (cvJobbprofilResponse.code() == HttpStatus.NOT_FOUND.value()) {
                log.warn("Kunne ikke hente CV- og jobbprofil for bruker. Årsak: baksystem returnerte NOT_FOUND.");
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            } else if (cvJobbprofilResponse.code() == HttpStatus.FORBIDDEN.value()) {
                log.warn("Kunne ikke hente CV- og jobbprofil for bruker. Årsak: baksystem returnerte FORBIDDEN.");
                return ikkeTilgangResponse(CvIkkeTilgang.BRUKER_IKKE_GODKJENT_SAMTYKKE);
            }

            String cvJobbprofilJson = RestUtils.getBodyStr(cvJobbprofilResponse)
                    .orElseThrow(() -> {
                        log.warn("Kunne ikke hente CV- og jobbprofil for bruker. Årsak: respons fra baksystem var tom.");
                        return new ResponseStatusException(HttpStatus.NOT_FOUND);
                    });

            if (cvJobbprofilJson.trim().equals(EMPTY_JSON_OBJ)) {
                log.warn("Kunne ikke hente CV- og jobbprofil for bruker. Årsak: respons fra baksystem var tom.");
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }

            return ResponseEntity.ok(cvJobbprofilJson);
        }
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
