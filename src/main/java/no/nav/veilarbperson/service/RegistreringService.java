package no.nav.veilarbperson.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.aiabackend.AiaBackendClient;
import no.nav.veilarbperson.client.aiabackend.EndringIRegistreringsdataRequestDTO;
import no.nav.veilarbperson.client.veilarbregistrering.VeilarbregistreringClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RegistreringService {

    private final VeilarbregistreringClient client;
    private final AiaBackendClient aiaBackendClient;

    @SneakyThrows
    public ResponseEntity<String> hentRegistrering(Fnr fnr) {
        try (Response response = client.hentRegistrering(fnr);
             ResponseBody responseBody = response.body()) {

            String bodyString = null;

            if (responseBody != null) {
                bodyString = responseBody.string();
            }

            return ResponseEntity
                    .status(response.code())
                    .body(bodyString);
        }
    }

    @SneakyThrows
    public ResponseEntity<String> hentEndringIRegistreringsdata(Fnr fnr) {
        try (Response response = aiaBackendClient.hentEndringIRegistreringsdata(new EndringIRegistreringsdataRequestDTO(fnr.get()));
             ResponseBody responseBody = response.body()) {

            String bodyString = null;

            if (responseBody != null) {
                bodyString = responseBody.string();
            }

            return ResponseEntity
                    .status(response.code())
                    .body(bodyString);
        }
    }
}
