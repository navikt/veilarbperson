package no.nav.veilarbperson.client.pam;

import lombok.SneakyThrows;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.health.HealthCheckUtils;
import no.nav.common.rest.client.RestClient;
import no.nav.common.types.identer.Fnr;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.function.Supplier;

import static no.nav.common.utils.UrlUtils.joinPaths;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class PamClientImpl implements PamClient {

    private final String pamCvApiUrl;

    private final Supplier<String> systemTokenProvider;

    private final OkHttpClient client;

    public PamClientImpl(String pamCvApiUrl, Supplier<String> systemTokenProvider) {
        this.pamCvApiUrl = pamCvApiUrl;
        this.systemTokenProvider = systemTokenProvider;
        this.client = RestClient.baseClient();
    }

    @SneakyThrows
    @Override
    public Response hentCvOgJobbprofil(Fnr fnr, boolean erBrukerManuell) {
        Request request = new Request.Builder()
                .url(joinPaths(pamCvApiUrl, "/rest/v2/arbeidssoker", fnr.get()) + "?erManuell=" + erBrukerManuell)
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION, "Bearer " + systemTokenProvider.get())
                .build();

        return client.newCall(request).execute();
    }

    @Override
    public HealthCheckResult checkHealth() {
        return HealthCheckUtils.pingUrl(joinPaths(pamCvApiUrl, "/rest/internal/isAlive"), client);
    }

}
