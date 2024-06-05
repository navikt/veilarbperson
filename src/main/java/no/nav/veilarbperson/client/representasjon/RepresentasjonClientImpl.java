package no.nav.veilarbperson.client.representasjon;

import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import no.nav.veilarbperson.client.digdir.DigdirKontaktinfo;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
    public RepresentasjonClientImpl(String reprUrl, Supplier<String> userTokenProvider, Supplier<String> systemTokenProvider) {
        this.reprUrl = reprUrl;
        this.client = RestClient.baseClient();
        this.userTokenProvider = userTokenProvider;
    }

    public RepresentasjonFullmakt getFullmakt(String kryptertIdent) throws IOException {
        Request request = new Request.Builder()
                .url(joinPaths(reprUrl, "/api/fullmakt/fullmaktsgiver"))
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION, createBearerToken(userTokenProvider.get()))
                .header("Nav-Personident", kryptertIdent)
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            return RestUtils.parseJsonResponse(response, RepresentasjonFullmakt.class)
                    .orElseThrow(() -> new IllegalStateException("Representasjon fullmakt body is missing"));
        }
    }
}
