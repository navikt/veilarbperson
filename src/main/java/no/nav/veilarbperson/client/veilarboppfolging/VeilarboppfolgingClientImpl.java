package no.nav.veilarbperson.client.veilarboppfolging;

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

public class VeilarboppfolgingClientImpl implements VeilarboppfolgingClient {

    private final String veilarboppfolgingUrl;

    private final Supplier<String> userTokenProvider;

    private final OkHttpClient client;

    public VeilarboppfolgingClientImpl(String veilarboppfolgingUrl, Supplier<String> userTokenProvider) {
        this.veilarboppfolgingUrl = veilarboppfolgingUrl;
        this.userTokenProvider = userTokenProvider;
        this.client = RestClient.baseClient();
    }

    @Cacheable(CacheConfig.VEILARBOPPFOLGING_UNDER_OPPFOLGING_CACHE_NAME)
    @SneakyThrows
    @Override
    public UnderOppfolging hentUnderOppfolgingStatus(Fnr fnr) {
        Request request = new Request.Builder()
                .url(joinPaths(veilarboppfolgingUrl, "/api/underoppfolging?fnr=" + fnr.get()))
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION, "Bearer " + userTokenProvider.get())
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            return RestUtils.parseJsonResponseOrThrow(response, UnderOppfolging.class);
        }
    }

    @Override
    public HealthCheckResult checkHealth() {
        return HealthCheckUtils.pingUrl(joinPaths(veilarboppfolgingUrl, "/internal/isAlive"), client);
    }

}
