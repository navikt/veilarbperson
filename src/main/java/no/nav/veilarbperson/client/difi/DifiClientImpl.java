package no.nav.veilarbperson.client.difi;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.config.CacheConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.cache.annotation.Cacheable;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class DifiClientImpl implements  DifiCient {

    private final DifiAccessTokenProvider difiAccessTokenProvider;
    private final OkHttpClient client;
    private final String difiUrl;
    private final String xNavApikey;


    public DifiClientImpl(DifiAccessTokenProvider difiAccessTokenProvider, String xNavApikey, String difiUrl) {
        this.difiAccessTokenProvider = difiAccessTokenProvider;
        this.difiUrl = difiUrl;
        this.xNavApikey = xNavApikey;
        this.client = RestClient.baseClient();
    }

    @Cacheable(CacheConfig.DIFI_HAR_NIVA_4_CACHE_NAME)
    @SneakyThrows
    @Override
    public HarLoggetInnRespons harLoggetInnSiste18mnd(Fnr fnr) {
        String token = difiAccessTokenProvider.getAccessToken();
        Request request = new Request.Builder()
                .url(difiUrl)
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION, "Bearer " + token)
                .header("x-nav-apiKey", xNavApikey)
                .post(RestUtils.toJsonRequestBody(new Personidentifikator(fnr)))
                .build();
        try (Response response = client.newCall(request).execute()) {
            if(response.code() == 404) {
                //brukere som ikke er registret som idporten brukere returnerer 404
                return new HarLoggetInnRespons()
                        .setHarbruktnivaa4(false)
                        .setErRegistrertIdPorten(false)
                        .setPersonidentifikator(fnr);
            }

            log.error(String.valueOf(RestUtils.getBodyStr(response)));
            RestUtils.throwIfNotSuccessful(response);
            HarLoggetInnRespons harLoggetInnRespons = RestUtils.parseJsonResponseOrThrow(response, HarLoggetInnRespons.class);

            harLoggetInnRespons.setErRegistrertIdPorten(true);

            return harLoggetInnRespons;
        }
    }

    @AllArgsConstructor
    private static class Personidentifikator {
        Fnr personidentifikator;
    }

}
