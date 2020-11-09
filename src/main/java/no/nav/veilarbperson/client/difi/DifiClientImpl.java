package no.nav.veilarbperson.client.difi;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.config.CacheConfig;
import okhttp3.*;
import org.springframework.cache.annotation.Cacheable;

import java.util.Optional;

import static no.nav.common.utils.EnvironmentUtils.getNamespace;
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

    public static String getNivaa4Url() {
        Optional<String> namespace = getNamespace();
        String name = namespace.orElse("default");
        String urlpart = name.equalsIgnoreCase("default") ? "" : "-" + name;
        return "https://api-gw"+ urlpart + ".adeo.no/ekstern/difi/authlevel/rest/v1/sikkerhetsnivaa";
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
            RestUtils.throwIfNotSuccessful(response);
            log.info(response.toString());
            log.info(response.peekBody(Long.MAX_VALUE).toString()); //TODO slett disse
            return RestUtils.parseJsonResponseOrThrow(response, HarLoggetInnRespons.class);
        }
    }

    @AllArgsConstructor
    private static class Personidentifikator {
        Fnr personidentifikator;
    }

}
