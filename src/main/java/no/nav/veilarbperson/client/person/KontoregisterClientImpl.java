package no.nav.veilarbperson.client.person;

import lombok.SneakyThrows;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.domain.KontoregisterResponseDTO;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Optional.empty;
import static no.nav.common.utils.UrlUtils.joinPaths;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


public class KontoregisterClientImpl implements KontoregisterClient {

    private final String kontoregisterUrl;
    private static final String KONTOREGISTER_API_URL = "/api/system/v1/hent-aktiv-konto";

    private final Supplier<String> systemUserTokenProvider;

    private final OkHttpClient client;

    public KontoregisterClientImpl(String kontoregisterUrl, Supplier<String> systemUserTokenProvider) {
        this.kontoregisterUrl = kontoregisterUrl;
        this.systemUserTokenProvider = systemUserTokenProvider;
        this.client = RestClient.baseClient();
    }
    @SneakyThrows
    @Override
    public Optional<KontoregisterResponseDTO> hentKontonummer(Fnr kontohaver) {
        Request request = new Request.Builder()
                .url(joinPaths(kontoregisterUrl, KONTOREGISTER_API_URL))
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION, "Bearer " + getToken())
                .build();
        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            return RestUtils.parseJsonResponse(response, KontoregisterResponseDTO.class);
        } catch (Exception e) {
    //        log.error("Feil under henting av data fra Kontoregister", e);
            return empty();
        }
    }

    @Override
    public HealthCheckResult checkHealth() {
        return null;
    }

    private String getToken() {
        return systemUserTokenProvider.get();
    }
}

