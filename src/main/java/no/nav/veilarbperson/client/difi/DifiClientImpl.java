package no.nav.veilarbperson.client.difi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.json.JsonUtils;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import no.nav.common.utils.Credentials;
import no.nav.veilarbperson.config.CacheConfig;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.cache.annotation.Cacheable;

import java.util.Optional;

import static no.nav.common.rest.client.RestUtils.MEDIA_TYPE_JSON;
import static no.nav.common.utils.EnvironmentUtils.getNamespace;
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

    public static String getDifiUrl() {
        Optional<String> namespace = getNamespace();
        String name = namespace.orElse("defult");
        String urlpart = name.equals("defult") ? "" : "-" + name;
        return "https://api-gw"+ urlpart + ".adeo.no/ekstern/difi/authlevel/rest/v1/sikkerhetsnivaa;";
    }


    @Cacheable(CacheConfig.DIFI_HAR_NIVA_4_CACHE_NAME)
    @SneakyThrows
    @Override
    public HarLoggetInnRespons harLoggetInnSiste18mnd(String fnr) {

        JSONObject jo = new JSONObject();
        String body = jo.put("personidentifikator", fnr).toString();

        Request request = new Request.Builder()
                .url(difiUrl)
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION, okhttp3.Credentials.basic(serviceUserCredentials.username, serviceUserCredentials.password))
                .post(RequestBody.create(MEDIA_TYPE_JSON, body))
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            return RestUtils.parseJsonResponseOrThrow(response, HarLoggetInnRespons.class);
        }
    }
}
