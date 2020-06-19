package no.nav.veilarbperson.client.pdl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.health.HealthCheckUtils;
import no.nav.common.json.JsonUtils;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import no.nav.veilarbperson.utils.FileUtils;
import no.nav.veilarbperson.utils.RestClientUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.function.Supplier;

import static no.nav.common.utils.UrlUtils.joinPaths;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class PdlClientImpl implements PdlClient {

    private final String pdlUrl;

    private final OkHttpClient client;

    private final Supplier<String> systemUserTokenSupplier;

    private final String hentPersonQuery;

    public PdlClientImpl(String pdlUrl, Supplier<String> systemUserTokenSupplier) {
        this.pdlUrl = pdlUrl;
        this.client = RestClient.baseClient();
        this.systemUserTokenSupplier = systemUserTokenSupplier;
        this.hentPersonQuery = FileUtils.getResourceFileAsString("gql/hentPerson.gql");
    }

    @Override
    public HentPersonData.PdlPerson hentPerson(String personIdent, String userToken) {
        GqlRequest request = new GqlRequest<>(hentPersonQuery, new HentPersonVariables(personIdent));
        return graphqlRequest(request, userToken, HentPersonData.class).hentPerson;
    }

    @Override
    public HealthCheckResult checkHealth() {
        return HealthCheckUtils.pingUrl(joinPaths(pdlUrl, "/internal/isAlive"), client);
    }

    @SneakyThrows
    private <T> T graphqlRequest(GqlRequest gqlRequest, String userToken, Class<T> gqlResponseDataClass) {
        Request request = new Request.Builder()
                .url(joinPaths(pdlUrl, "/graphql"))
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION, RestClientUtils.createBearerToken(userToken))
                .header("Nav-Consumer-Token", RestClientUtils.createBearerToken(systemUserTokenSupplier.get()))
                .header("Tema", "GEN")
                .post(RestUtils.toJsonRequestBody(gqlRequest))
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            String jsonBody = RestUtils.getBodyStr(response)
                    .orElseThrow(() -> new IllegalStateException("Body is missing"));

            return parseGqlJsonResponse(jsonBody, gqlResponseDataClass);
        }
    }

    private static <T> T parseGqlJsonResponse(String gqlJsonResponse, Class<T> gqlDataClass) throws JsonProcessingException {
        ObjectMapper mapper = JsonUtils.getMapper();
        JsonNode gqlResponseNode = mapper.readTree(gqlJsonResponse);
        JsonNode errorsNode = gqlResponseNode.get("errors");

        if (errorsNode != null) {
            log.error("Kall mot PDL feilet:\n" + errorsNode.toPrettyString());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return mapper.treeToValue(gqlResponseNode.get("data"), gqlDataClass);
    }

}
