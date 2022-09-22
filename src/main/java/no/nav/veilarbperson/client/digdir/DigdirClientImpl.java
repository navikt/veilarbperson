package no.nav.veilarbperson.client.digdir;

import lombok.SneakyThrows;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.health.HealthCheckUtils;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.config.CacheConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.cache.annotation.Cacheable;

import java.util.function.Supplier;

import static no.nav.common.utils.UrlUtils.joinPaths;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class DigdirClientImpl implements DigdirClient {

    private final String digdirUrl;
    private final Supplier<String> systemUserTokenProvider;
    private final OkHttpClient client;

    public DigdirClientImpl(String digdirUrl, Supplier<String> systemUserTokenProvider) {
        this.digdirUrl = digdirUrl;
        this.systemUserTokenProvider = systemUserTokenProvider;
        this.client = RestClient.baseClient();
    }

    @Cacheable(CacheConfig.DIGDIR_KONTAKTINFO_CACHE_NAME)
    @SneakyThrows
    @Override
    public DigdirKontaktinfo hentKontaktInfo(Fnr fnr) {
        Request request = new Request.Builder()
                .url(joinPaths(digdirUrl, "/rest/v1/person?inkluderSikkerDigitalPost=false"))
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION, "Bearer " + systemUserTokenProvider.get())
                .header("Nav-Personident", fnr.get())
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            return RestUtils.parseJsonResponse(response, DigdirKontaktinfo.class)
                    .orElseThrow(() -> new IllegalStateException("Digdir body is missing"));
        }
    }

    @Override
    public HealthCheckResult checkHealth() {
        return HealthCheckUtils.pingUrl(joinPaths(digdirUrl, "/rest/ping"), client);
    }

}
