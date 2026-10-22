package no.nav.veilarbperson.client.representasjon;

import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tools.jackson.core.JacksonException;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import static no.nav.common.utils.UrlUtils.joinPaths;
import static no.nav.veilarbperson.utils.RestClientUtils.createBearerToken;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class RepresentasjonClientImpl implements RepresentasjonClient{

    private final String reprUrl;
    private final OkHttpClient client;
    private final Supplier<String> userTokenProvider;
    public RepresentasjonClientImpl(String reprUrl, Supplier<String> userTokenProvider) {
        this(reprUrl, userTokenProvider, RestClient.baseClient());
    }

    RepresentasjonClientImpl(String reprUrl, Supplier<String> userTokenProvider, OkHttpClient client) {
        this.reprUrl = reprUrl;
        this.client = client;
        this.userTokenProvider = userTokenProvider;
    }

    @Override
    public List<ReprFullmaktV2Data> hentFullmakt(String personIdent) throws IOException {
        Request request = new Request.Builder()
                .url(joinPaths(reprUrl, "/api/v2/internbruker/fullmakt/bruker-som-fullmaktsgiver/alle-fullmakter"))
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION, createBearerToken(userTokenProvider.get()))
                .post(RestUtils.toJsonRequestBody(new PersonIdentDTO(personIdent)))
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            List<ReprFullmaktV2Data> fullmakter;
            try {
                fullmakter = RestUtils.parseJsonArrayResponse(response, ReprFullmaktV2Data.class)
                        .orElseThrow(() -> new IllegalStateException("Mangler body i responsen fra representasjon"));
            } catch (JacksonException e) {
                // Parserfeil kan inneholde persondata, og skal ikke viderefoeres som aarsak.
                throw new IllegalStateException("Ugyldig fullmaktsrespons fra representasjon");
            }
            if (fullmakter.stream().anyMatch(fullmakt -> fullmakt == null)) {
                throw new IllegalStateException("Ugyldig fullmaktsrespons fra representasjon");
            }
            return fullmakter;
        }
    }
}
