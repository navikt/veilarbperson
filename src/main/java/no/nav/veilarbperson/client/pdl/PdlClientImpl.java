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
import no.nav.common.types.identer.Fnr;
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

    private final String hentVergeOgFullmaktQuery;

    private final String hentPersonNavnQuery;

    private final String hentPersonBolkQuery;

    private final String hentGeografiskTilknytningQuery;

    private final String hentTilrettelagtKommunikasjonQuery;

    public PdlClientImpl(String pdlUrl, Supplier<String> systemUserTokenSupplier) {
        this.pdlUrl = pdlUrl;
        this.client = RestClient.baseClient();
        this.systemUserTokenSupplier = systemUserTokenSupplier;
        this.hentPersonQuery = FileUtils.getResourceFileAsString("graphql/hentPerson.gql");
        this.hentPersonBolkQuery = FileUtils.getResourceFileAsString("graphql/hentPersonBolk.gql");
        this.hentPersonNavnQuery = FileUtils.getResourceFileAsString("graphql/hentPersonNavn.gql");
        this.hentVergeOgFullmaktQuery = FileUtils.getResourceFileAsString("graphql/hentVergeOgFullmakt.gql");
        this.hentGeografiskTilknytningQuery = FileUtils.getResourceFileAsString("graphql/hentGeografiskTilknytning.gql");
        this.hentTilrettelagtKommunikasjonQuery = FileUtils.getResourceFileAsString("graphql/hentTilrettelagtKommunikasjon.gql");
    }

    @Override
    public HentPerson.Person hentPerson(Fnr personIdent, String userToken) {
        GqlRequest request = new GqlRequest<>(hentPersonQuery, new GqlVariables.HentPerson(personIdent, false));
        return graphqlRequest(request, userToken, HentPerson.class).hentPerson;
    }

    @Override
    public HentPerson.VergeOgFullmakt hentVergeOgFullmakt(Fnr personIdent, String userToken) {
        GqlRequest request = new GqlRequest<>(hentVergeOgFullmaktQuery, new GqlVariables.HentPerson(personIdent, false));
        return graphqlRequest(request, userToken, HentPerson.HentVergeOgFullmakt.class).hentPerson;
    }

    @Override
    public HentPerson.PersonNavn hentPersonNavn(Fnr personIdent, String userToken) {
        GqlRequest request = new GqlRequest<>(hentPersonNavnQuery, new GqlVariables.HentPerson(personIdent, false));
        return graphqlRequest(request, userToken, HentPerson.HentFullmaktNavn.class).hentPerson;
    }

    @Override
    public HentPerson.Familiemedlem hentPartner(Fnr personIdent, String userToken) {
        GqlRequest request = new GqlRequest<>(hentPersonQuery, new GqlVariables.HentPerson(personIdent, false));
        return graphqlRequest(request, userToken, HentPerson.Partner.class).hentPerson;
    }

    @Override
    public List<HentPerson.Barn> hentPersonBolk(Fnr[] personIdenter) {
        GqlRequest request = new GqlRequest<>(hentPersonBolkQuery, new GqlVariables.HentPersonBolk(personIdenter, false));
        return graphqlRequest(request, systemUserTokenSupplier.get(), HentPerson.class).hentPersonBolk;
    }

    @Override
    public HentPerson.GeografiskTilknytning hentGeografiskTilknytning(Fnr personIdent, String userToken) {
        GqlRequest request = new GqlRequest<>(hentGeografiskTilknytningQuery, new GqlVariables.HentGeografiskTilknytning(personIdent));
        return graphqlRequest(request, userToken, HentPerson.class).hentGeografiskTilknytning;
    }

    @Override
    public HentPerson.HentSpraakTolk hentTilrettelagtKommunikasjon(Fnr personIdent, String userToken) {
        GqlRequest request = new GqlRequest<>(hentTilrettelagtKommunikasjonQuery, new GqlVariables.HentTilrettelagtKommunikasjon(personIdent));
        return graphqlRequest(request, userToken, HentPerson.HentTilrettelagtKommunikasjon.class).hentPerson;
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
        JsonNode gqlResponseNode = mapper.readTree(gqlJsonResponse);
        JsonNode errorsNode = gqlResponseNode.get("errors");

        if (errorsNode != null) {
            log.error("Kall mot PDL feilet:\n" + errorsNode.toPrettyString());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return mapper.treeToValue(gqlResponseNode.get("data"), gqlDataClass);
    }
}
