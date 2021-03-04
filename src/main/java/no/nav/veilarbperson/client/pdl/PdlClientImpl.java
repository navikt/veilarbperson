package no.nav.veilarbperson.client.pdl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.function.Supplier;

import static no.nav.common.rest.client.RestUtils.MEDIA_TYPE_JSON;
import static no.nav.common.utils.UrlUtils.joinPaths;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class PdlClientImpl implements PdlClient {

    private final String pdlUrl;

    private final OkHttpClient client;

    private final Supplier<String> systemUserTokenSupplier;

    private final String hentPersonQuery;

    private final String hentPersonBolkQuery;

    private final String hentGeografiskTilknytningQuery;


    public PdlClientImpl(String pdlUrl, Supplier<String> systemUserTokenSupplier) {
        this.pdlUrl = pdlUrl;
        this.client = RestClient.baseClient();
        this.systemUserTokenSupplier = systemUserTokenSupplier;
        this.hentPersonQuery = FileUtils.getResourceFileAsString("graphql/hentPerson.gql");
        this.hentPersonBolkQuery = FileUtils.getResourceFileAsString("graphql/hentPersonBolk.gql");
        this.hentGeografiskTilknytningQuery = FileUtils.getResourceFileAsString("graphql/hentGeografiskTilknytning.gql");
    }

    @Override
    public HentPdlPerson.PdlPerson hentPerson(String personIdent, String userToken) {
        GqlRequest request = new GqlRequest<>(hentPersonQuery, new PdlPersonVariables.HentPersonVariables(personIdent, false));
        return graphqlRequest(request, userToken, HentPdlPerson.class).hentPerson;
    }

    @Override
    public HentPdlPerson.Familiemedlem hentPartner(String personIdent, String userToken) {
        GqlRequest request = new GqlRequest<>(hentPersonQuery, new PdlPersonVariables.HentPersonVariables(personIdent, false));
        return graphqlRequest(request, userToken, HentPdlPerson.Partner.class).hentPerson;
    }

    @Override
    public List<HentPdlPerson.Barn> hentPersonBolk(String[] personIdent) {
        GqlRequest request = new GqlRequest<>(hentPersonBolkQuery, new PdlPersonVariables.HentPersonBolkVariables(personIdent, false));
        return graphqlRequest(request, systemUserTokenSupplier.get(), HentPdlPerson.class).hentPersonBolk;
    }

    @Override
    public HentPdlPerson.GeografiskTilknytning hentGeografiskTilknytning(String personIdent, String userToken) {
        GqlRequest request = new GqlRequest<>(hentGeografiskTilknytningQuery, new PdlPersonVariables.HentGeografiskTilknytningVariables(personIdent));
        return graphqlRequest(request, userToken, HentPdlPerson.class).hentGeografiskTilknytning;
    }

    @SneakyThrows
    public String rawRequest(String gqlRequest, String userToken) {
        Request request = new Request.Builder()
                .url(joinPaths(pdlUrl, "/graphql"))
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION, RestClientUtils.createBearerToken(userToken))
                .header("Nav-Consumer-Token", RestClientUtils.createBearerToken(systemUserTokenSupplier.get()))
                .header("Tema", "GEN")
                .post(RequestBody.create(MEDIA_TYPE_JSON, gqlRequest))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code() >= 300) {
                HttpStatus status = HttpStatus.resolve(response.code());
                status = status != null
                        ? status
                        : HttpStatus.INTERNAL_SERVER_ERROR;

                String body = RestUtils.getBodyStr(response).orElse("");
                throw new ResponseStatusException(status, body);
            }

            RestUtils.throwIfNotSuccessful(response);
            return RestUtils.getBodyStr(response)
                    .orElseThrow(() -> new IllegalStateException("Body is missing"));
        }
    }

    @Override
    public HealthCheckResult checkHealth() {
        return HealthCheckUtils.pingUrl(joinPaths(pdlUrl, "/internal/health/liveness"), client);
    }

    private <T> T graphqlRequest(GqlRequest gqlRequest, String userToken, Class<T> gqlResponseDataClass) {
        try {
            String gqlResponse = rawRequest(JsonUtils.toJson(gqlRequest), userToken);
            return parseGqlJsonResponse(gqlResponse, gqlResponseDataClass);
        } catch (Exception e) {
            log.error("Graphql request feilet", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static <T> T parseGqlJsonResponse(String gqlJsonResponse, Class<T> gqlDataClass) throws JsonProcessingException {
        ObjectMapper mapper = JsonUtils.getMapper();
        mapper.registerModule(new JavaTimeModule());
        JsonNode gqlResponseNode = mapper.readTree(gqlJsonResponse);
        JsonNode errorsNode = gqlResponseNode.get("errors");

        if (errorsNode != null) {
            log.error("Kall mot PDL feilet:\n" + errorsNode.toPrettyString());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return mapper.treeToValue(gqlResponseNode.get("data"), gqlDataClass);
    }
}
