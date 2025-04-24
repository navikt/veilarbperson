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
import no.nav.veilarbperson.client.pdl.domain.PdlRequest;
import no.nav.veilarbperson.utils.FileUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;
import static no.nav.common.rest.client.RestUtils.MEDIA_TYPE_JSON;
import static no.nav.common.utils.UrlUtils.joinPaths;
import static no.nav.veilarbperson.utils.RestClientUtils.createBearerToken;
import static no.nav.veilarbperson.utils.SecureLog.secureLog;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class PdlClientImpl implements PdlClient {

    private final String pdlUrl;

    private final OkHttpClient client;

    private final String hentPersonQuery;

    private final String hentVergeQuery;

    private final String hentPersonNavnQuery;

    private final String hentPersonBolkQuery;

    private final String hentGeografiskTilknytningQuery;

    private final String hentAdressebeskyttelseQuery;

    private final String hentTilrettelagtKommunikasjonQuery;

    private final Supplier<String> userTokenProvider;
    private final Supplier<String> systemTokenProvider;

    public PdlClientImpl(String pdlUrl, Supplier<String> userTokenProvider, Supplier<String> systemTokenProvider) {
        this.pdlUrl = pdlUrl;
        this.client = RestClient.baseClient();
        this.userTokenProvider = userTokenProvider;
        this.systemTokenProvider = systemTokenProvider;
        this.hentPersonQuery = FileUtils.getResourceFileAsString("graphql/hentPerson.gql");
        this.hentPersonBolkQuery = FileUtils.getResourceFileAsString("graphql/hentPersonBolk.gql");
        this.hentPersonNavnQuery = FileUtils.getResourceFileAsString("graphql/hentPersonNavn.gql");
        this.hentVergeQuery = FileUtils.getResourceFileAsString("graphql/hentVerge.gql");
        this.hentGeografiskTilknytningQuery = FileUtils.getResourceFileAsString("graphql/hentGeografiskTilknytning.gql");
        this.hentTilrettelagtKommunikasjonQuery = FileUtils.getResourceFileAsString("graphql/hentTilrettelagtKommunikasjon.gql");
        this.hentAdressebeskyttelseQuery = FileUtils.getResourceFileAsString("graphql/hentAdressebeskyttelse.gql");
    }

    @Override
    public HentPerson.Person hentPerson(PdlRequest pdlRequest) {
        var request = new GqlRequest<>(hentPersonQuery, new GqlVariables.HentPerson(pdlRequest.fnr(), false));
        return graphqlRequest(request, userTokenProvider.get(), pdlRequest.behandlingsnummer(), HentPerson.class).hentPerson;
    }

    @Override
    public HentPerson.Verge hentVerge(PdlRequest pdlRequest) {
        var request = new GqlRequest<>(hentVergeQuery, new GqlVariables.HentPerson(pdlRequest.fnr(), false));
        return graphqlRequest(request, userTokenProvider.get(), pdlRequest.behandlingsnummer(), HentPerson.HentVerge.class).hentPerson;
    }

    @Override
    public HentPerson.PersonNavn hentPersonNavn(PdlRequest pdlRequest) {
        var request = new GqlRequest<>(hentPersonNavnQuery, new GqlVariables.HentPerson(pdlRequest.fnr(), false));
        log.info("userToken i HentPerson.PersonNavn {}", userTokenProvider.get());
        return graphqlRequest(request, userTokenProvider.get(), pdlRequest.behandlingsnummer(), HentPerson.HentFullmaktNavn.class).hentPerson;
    }

    @Override
    public List<HentPerson.PersonFraBolk> hentPersonBolk(List<Fnr> personIdenter, String behandlingsnummer) {
        var request = new GqlRequest<>(hentPersonBolkQuery, new GqlVariables.HentPersonBolk(personIdenter, false));
        return (!personIdenter.isEmpty())
                ? graphqlRequest(request, systemTokenProvider.get(), behandlingsnummer, HentPerson.class).hentPersonBolk
                : emptyList();
    }

    @Override
    public HentPerson.GeografiskTilknytning hentGeografiskTilknytning(PdlRequest pdlRequest) {
        var request = new GqlRequest<>(hentGeografiskTilknytningQuery, new GqlVariables.HentGeografiskTilknytning(pdlRequest.fnr()));
        return graphqlRequest(request, userTokenProvider.get(), pdlRequest.behandlingsnummer(), HentPerson.class).hentGeografiskTilknytning;
    }


    @Override
    public HentPerson.HentSpraakTolk hentTilrettelagtKommunikasjon(PdlRequest pdlRequest) {
        var request = new GqlRequest<>(hentTilrettelagtKommunikasjonQuery, new GqlVariables.HentTilrettelagtKommunikasjon(pdlRequest.fnr()));
        return graphqlRequest(request, userTokenProvider.get(), pdlRequest.behandlingsnummer(), HentPerson.HentTilrettelagtKommunikasjon.class).hentPerson;
    }

    @Override
    public List<HentPerson.Adressebeskyttelse> hentAdressebeskyttelse(PdlRequest pdlRequest) {
        var request = new GqlRequest<>(hentAdressebeskyttelseQuery, new GqlVariables.HentAdressebeskyttelse(pdlRequest.fnr()));
        return graphqlRequest(request, systemTokenProvider.get(), pdlRequest.behandlingsnummer(), HentPerson.class).hentPerson.getAdressebeskyttelse();
    }

    @SneakyThrows
    public String rawRequest(String gqlRequest, String userToken, String behandlingsnummer) {
        String behandlingsnr = (behandlingsnummer != null) ? behandlingsnummer : "";
        Request.Builder builder = new Request.Builder()
                .url(joinPaths(pdlUrl, "/graphql"))
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION, createBearerToken(userToken))
                .header("Tema", "GEN")
                .header("behandlingsnummer", behandlingsnr)
                .post(RequestBody.create(gqlRequest, MEDIA_TYPE_JSON));

        Request request = builder.build();

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

    private <T> T graphqlRequest(GqlRequest<?> gqlRequest, String token, String behandlingsnummer, Class<T> gqlResponseDataClass) {
        if (behandlingsnummer == null) {
            secureLog.info("Mottok graphQLrequest mot PDL med behandlingsnummer null");
        }
        try {
            String gqlResponse = rawRequest(JsonUtils.toJson(gqlRequest), token, behandlingsnummer);
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
