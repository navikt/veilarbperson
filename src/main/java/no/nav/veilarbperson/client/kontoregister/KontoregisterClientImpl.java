package no.nav.veilarbperson.client.kontoregister;


import lombok.extern.slf4j.Slf4j;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.health.HealthCheckUtils;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import no.nav.veilarbperson.config.CacheConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.util.function.Supplier;

import no.nav.common.utils.UrlUtils;
import okhttp3.Response;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;

import static no.nav.common.utils.UrlUtils.joinPaths;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
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
    @Cacheable(CacheConfig.KONTOREGISTER_CACHE_NAME)
    @Override
    public HentKontoResponseDTO hentKontonummer(HentKontoRequestDTO kontohaver) {
        log.info("I hentKontonummerImpl Url={}, kontohaver.getKontohaver()={}, token={}", UrlUtils.joinPaths(kontoregisterUrl, KONTOREGISTER_API_URL), kontohaver.getKontohaver(), systemUserTokenProvider.toString());
        Request request = new Request.Builder()
                .url(UrlUtils.joinPaths(kontoregisterUrl, KONTOREGISTER_API_URL))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + systemUserTokenProvider.get())
                .post(RestUtils.toJsonRequestBody(kontohaver))
                .build();
        log.info("Request til kontoreg url = {},  auth = {}", request.url(), systemUserTokenProvider.get());

        try (Response response = client.newCall(request).execute()) {
            log.info("svar fra kontoreg: message = {}, challenges = {}, TokenProvider = {}", response.message(), response.challenges(), systemUserTokenProvider.get());
            RestUtils.throwIfNotSuccessful(response);
            return RestUtils.parseJsonResponse(response, HentKontoResponseDTO.class)
                    .orElseThrow(() -> new IllegalStateException("HentKontonummer body is missing"));


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
}

    @Override
    public HealthCheckResult checkHealth() {
        Request request = new Request.Builder()
                .url(joinPaths(kontoregisterUrl, "/rest/ping"))
                .header(AUTHORIZATION, "Bearer " + systemUserTokenProvider.get())
                .build();

        return HealthCheckUtils.pingUrl(request, client);
    }
}


