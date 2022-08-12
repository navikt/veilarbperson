package no.nav.veilarbperson.client.nom;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.health.HealthCheckUtils;
import no.nav.common.json.JsonUtils;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.config.CacheConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;

import java.util.function.Supplier;

import static no.nav.common.rest.client.RestUtils.MEDIA_TYPE_JSON;
import static no.nav.common.rest.client.RestUtils.parseJsonResponseOrThrow;
import static no.nav.common.utils.UrlUtils.joinPaths;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class SkjermetClientImpl implements SkjermetClient {

    private final String skjermedUrl;

    private final OkHttpClient client;

    private final Supplier<String> serviceTokenSupplier;

    public SkjermetClientImpl(String skjermedUrl, Supplier<String> serviceTokenSupplier) {
        this.skjermedUrl = skjermedUrl;
        this.serviceTokenSupplier = serviceTokenSupplier;
        this.client = RestClient.baseClient();
    }

    @Override
    @SneakyThrows
    @Cacheable(CacheConfig.NOM_SKJERMEDE_PERSONER_CACHE_NAME)
    public Boolean hentSkjermet(Fnr fodselsnummer) {

        Request request = new Request.Builder()
                .post(RequestBody.create(
                        JsonUtils.toJson(new Personident(fodselsnummer)),
                        MEDIA_TYPE_JSON
                ))
                .url(joinPaths(skjermedUrl, "/skjermet"))
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceTokenSupplier.get())
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            return parseJsonResponseOrThrow(response, Boolean.class);
        }
    }

    @Override
    public HealthCheckResult checkHealth() {
        return HealthCheckUtils.pingUrl(joinPaths(skjermedUrl, "/internal/isAlive"), client);
    }

    @AllArgsConstructor
    private static class Personident {
        Fnr personident;
    }
}
