package no.nav.veilarbperson.client.difi;

import com.nimbusds.jwt.JWTParser;
import lombok.SneakyThrows;
import no.nav.common.auth.utils.TokenUtils;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class DifiAccessTokenProviderImpl implements DifiAccessTokenProvider {
    private volatile String token;
    private final SbsServiceUser sbsServiceUser;
    private final OkHttpClient client;
    private final String url;

    public DifiAccessTokenProviderImpl(SbsServiceUser sbsServiceUser, String url) {
        this.url = url;
        this.sbsServiceUser = sbsServiceUser;
        this.client = RestClient.baseClient();
    }

    @SneakyThrows
    public synchronized String getAccessToken() {
        if (token == null || TokenUtils.expiresWithin(JWTParser.parse(token), 30*1000)) {
            token = fetchNewToken();
        }
        return token;
    }

    @SneakyThrows
    private  String fetchNewToken() {
        Request request = new Request
                .Builder()
                .url(url)
                .header(AUTHORIZATION, okhttp3.Credentials.basic(sbsServiceUser.username, sbsServiceUser.password))
                .post(RequestBody.create(new byte[0]))
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            Token tokenResponse = RestUtils.parseJsonResponseOrThrow(response, Token.class);
            return tokenResponse.access_token;
        }
    }

    private static class Token {
        String access_token;
    }

}
