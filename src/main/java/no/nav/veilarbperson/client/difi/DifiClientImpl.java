package no.nav.veilarbperson.client.difi;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import no.nav.common.types.identer.Fnr;
import no.nav.common.utils.Credentials;
import no.nav.veilarbperson.config.CacheConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.cache.annotation.Cacheable;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class DifiClientImpl implements  DifiCient {


    private final Credentials serviceUserCredentials;
    private final OkHttpClient client;

    private final String difiUrl;


    public DifiClientImpl(Credentials serviceUserCredentials, String difiUrl) {
        this.difiUrl = difiUrl;
        this.serviceUserCredentials = serviceUserCredentials;
        this.client = RestClient.baseClient();
    }

    @Cacheable(CacheConfig.DIFI_HAR_NIVA_4_CACHE_NAME)
    @SneakyThrows
    @Override
    public HarLoggetInnRespons harLoggetInnSiste18mnd(Fnr fnr) {
        Request request = new Request.Builder()
                .url(difiUrl)
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION, okhttp3.Credentials.basic(serviceUserCredentials.username, serviceUserCredentials.password))
                .post(RestUtils.toJsonRequestBody(new Personidentifikator(fnr, null)))
                .build();
        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            return RestUtils.parseJsonResponseOrThrow(response, HarLoggetInnRespons.class);
        }
    }

    @AllArgsConstructor
    private static class Personidentifikator {
        Fnr personidentifikator;
        String fraDato;
    }

}
