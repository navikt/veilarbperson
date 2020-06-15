package no.nav.veilarbperson.client;

import lombok.SneakyThrows;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.health.HealthCheckUtils;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import no.nav.veilarbperson.domain.Personinfo;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static no.nav.common.utils.UrlUtils.joinPaths;
import static no.nav.veilarbperson.utils.RestClientUtils.authHeaderMedInnloggetBruker;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class VeilarbportefoljeClientImpl implements VeilarbportefoljeClient {

    private final String veilarbportefoljeUrl;

    private final OkHttpClient client;

    public VeilarbportefoljeClientImpl(String veilarbportefoljeUrl) {
        this.veilarbportefoljeUrl = veilarbportefoljeUrl;
        this.client = RestClient.baseClient();
    }

    @SneakyThrows
    @Override
    // TODO: Cache
    public Personinfo hentPersonInfo(String fodselsnummer) {
        Request request = new Request.Builder()
                .url(joinPaths(veilarbportefoljeUrl, "/api/personinfo/", fodselsnummer))
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION, authHeaderMedInnloggetBruker())
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            return RestUtils.parseJsonResponseOrThrow(response, Personinfo.class);
        }
    }

    @Override
    public HealthCheckResult checkHealth() {
        return HealthCheckUtils.pingUrl(joinPaths(veilarbportefoljeUrl, "/internal/isAlive"), client);
    }
}
