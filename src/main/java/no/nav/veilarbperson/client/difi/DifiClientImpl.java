package no.nav.veilarbperson.client.difi;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
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

public class DifiClientImpl implements  DifiCient {

    private final AccessTokenRepository accessTokenRepository;
    private final OkHttpClient client;
    private final String difiUrl;
    private final String xNavApikey;


    public DifiClientImpl(AccessTokenRepository accessTokenRepository, String xNavApikey, String difiUrl) {
        this.accessTokenRepository = accessTokenRepository;
        this.difiUrl = difiUrl;
        this.xNavApikey = xNavApikey;
        this.client = RestClient
                .baseClientBuilder()
                .authenticator(new AccessTokenAuthenticator(this.accessTokenRepository))
                .build();

    }

    public static String getNivaa4Url() {
        Optional<String> namespace = getNamespace();
        String name = namespace.orElse("defult");
        String urlpart = name.equals("defult") ? "" : "-" + name;
        return "https://api-gw"+ urlpart + ".adeo.no/ekstern/difi/authlevel/rest/v1/sikkerhetsnivaa";
    }


    @Cacheable(CacheConfig.DIFI_HAR_NIVA_4_CACHE_NAME)
    @SneakyThrows
    @Override
    public HarLoggetInnRespons harLoggetInnSiste18mnd(Fnr fnr) {
        String token = accessTokenRepository.getAccessToken();
        Request request = new Request.Builder()
                .url(difiUrl)
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION, "Bearer " + token)
                .header("x-nav-apiKey", xNavApikey)
                .post(RestUtils.toJsonRequestBody(new Personidentifikator(fnr)))
                .build();
        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            return RestUtils.parseJsonResponseOrThrow(response, HarLoggetInnRespons.class);
        }
    }

    @AllArgsConstructor
    private static class Personidentifikator {
        Fnr personidentifikator;
    }

}
