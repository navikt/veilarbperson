package no.nav.veilarbperson.client.pam;

import lombok.SneakyThrows;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.health.HealthCheckUtils;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import no.nav.common.types.identer.Fnr;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.function.Supplier;

import static no.nav.common.utils.UrlUtils.joinPaths;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class PamClientImpl implements PamClient {

    private final static List<Integer> PASS_THROUGH_STATUS_CODES = List.of(401, 403, 404, 204);

    private final String pamCvApiUrl;

    private final Supplier<String> userTokenProvider;

    private final OkHttpClient client;

    public PamClientImpl(String pamCvApiUrl, Supplier<String> userTokenProvider) {
        this.pamCvApiUrl = pamCvApiUrl;
        this.userTokenProvider = userTokenProvider;
        this.client = RestClient.baseClient();
    }

    @SneakyThrows
    @Override
    public String hentCvOgJobbprofilJson(Fnr fnr) {
        Request request = new Request.Builder()
                .url(joinPaths(pamCvApiUrl, "/rest/v1/arbeidssoker", fnr.get()))
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION, "Bearer " + userTokenProvider.get())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (PASS_THROUGH_STATUS_CODES.contains(response.code())) {
                throw new ResponseStatusException(HttpStatus.valueOf(response.code()));
            }

            RestUtils.throwIfNotSuccessful(response);

            return RestUtils.getBodyStr(response).orElseThrow(() -> new IllegalStateException("Body is missing from request to pam-cv-api"));
        }
    }

    @Override
    public HealthCheckResult checkHealth() {
        return HealthCheckUtils.pingUrl(joinPaths(pamCvApiUrl, "/rest/internal/isAlive"), client);
    }

}
