package no.nav.veilarbperson.client.pdl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.health.HealthCheckUtils;
import no.nav.common.json.JsonUtils;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import no.nav.veilarbperson.utils.RestClientUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.function.Supplier;

import static no.nav.common.utils.UrlUtils.joinPaths;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class PdlClientImpl implements PdlClient {

    private final String pdlUrl;

    private final OkHttpClient client;

    private final Supplier<String> systemUserTokenSupplier;

    public PdlClientImpl(String pdlUrl, Supplier<String> systemUserTokenSupplier) {
        this.pdlUrl = pdlUrl;
        this.client = RestClient.baseClient();
        this.systemUserTokenSupplier = systemUserTokenSupplier;
    }

    @SneakyThrows
    @Override
    public <T> T graphqlRequest(String gqlRequestJson, String userToken, Class<T> responseDataClass) {
        Request request = new Request.Builder()
                .url(joinPaths(pdlUrl, "/graphql"))
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION, RestClientUtils.createBearerToken(userToken))
                .header("Nav-Consumer-Token", systemUserTokenSupplier.get())
                .post(RestUtils.toJsonRequestBody(gqlRequestJson))
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            String jsonBody = RestUtils.getBodyStr(response)
                    .orElseThrow(() -> new IllegalStateException("Body is missing"));

            return parseGqlJsonResponse(jsonBody, responseDataClass);
        }
    }

    @Override
    public HealthCheckResult checkHealth() {
        return HealthCheckUtils.pingUrl(joinPaths(pdlUrl, "/internal/isAlive"), client);
    }

    private static <T> T parseGqlJsonResponse(String gqlJsonResponse, Class<T> responseDataClass) throws JsonProcessingException {

        JsonNode gqlResponseNode = JsonUtils.getMapper().readTree(gqlJsonResponse);



        /*
  {
  "data": {
    "hentPerson": {
      "navn": [
        {
          "fornavn": "Ola",
          "mellomnavn": null,
          "etternavn": "Normann"
        }
      ]
    }
  }
}
         */

        return null;
    }

}
