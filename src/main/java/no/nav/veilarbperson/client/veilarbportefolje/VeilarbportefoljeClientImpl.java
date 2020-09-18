package no.nav.veilarbperson.client.veilarbportefolje;

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

    @Cacheable(CacheConfig.VEILARBPORTEFOLJE_PERSONINFO_CACHE_NAME)
    @SneakyThrows
    @Override
    public Personinfo hentPersonInfo(Fnr fodselsnummer) {
        Request request = new Request.Builder()
                .url(joinPaths(veilarbportefoljeUrl, "/api/personinfo/", fodselsnummer.get()))
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
