package no.nav.veilarbperson.client.veilarbregistrering;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.health.HealthCheckUtils;
import no.nav.common.rest.client.RestClient;
import no.nav.common.types.identer.Fnr;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.http.HttpHeaders;

import java.util.function.Supplier;

import static no.nav.common.utils.UrlUtils.joinPaths;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
public class VeilarbregistreringClientImpl implements VeilarbregistreringClient {

    private final OkHttpClient client;

    private final String veilarbregistreringUrl;

    private final Supplier<String> serviceTokenSupplier;

    @Override
    @SneakyThrows
    public Response hentRegistrering(Fnr fnr) {
        Request request = new Request.Builder()
                .url(joinPaths(veilarbregistreringUrl, "/veilarbregistrering/api/registrering?fnr=" + fnr))
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceTokenSupplier.get())
                .build();

        return RestClient.baseClient().newCall(request).execute();
    }

    @Override
    public HealthCheckResult checkHealth() {
        return HealthCheckUtils.pingUrl(joinPaths(veilarbregistreringUrl, "/internal/isReady"), client);
    }
}
