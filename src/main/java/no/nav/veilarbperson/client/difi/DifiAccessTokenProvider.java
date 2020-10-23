package no.nav.veilarbperson.client.difi;

import com.nimbusds.jwt.JWTParser;
import lombok.SneakyThrows;
import no.nav.common.auth.utils.TokenUtils;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import okhttp3.*;

import java.util.Optional;

import static no.nav.common.utils.EnvironmentUtils.getNamespace;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class DifiAccessTokenProvider {
    private static String token;
    private final SbsServiceUser sbsServiceUser;
    private final OkHttpClient client;
    private final String url;

    public DifiAccessTokenProvider(SbsServiceUser sbsServiceUser, String url) {
        this.url = url;
        this.sbsServiceUser = sbsServiceUser;
        this.client = RestClient.baseClient();
    }

    public static String getTokenUrl() {
        Optional<String> namespace = getNamespace();
        String name = namespace.orElse("defult");
        String urlpart = name.equals("defult") ? "" : "-" + name;
        return "https://api-gw"+ urlpart + ".adeo.no/ekstern/difi/idporten-oidc-provider/token";
    }

    @SneakyThrows
    public String getAccessToken() {
        if(token == null || TokenUtils.expiresWithin(JWTParser.parse(token), 30*1000)) {
            return refreshAccessToken();
        }
        return token;
    }

    @SneakyThrows
    private String refreshAccessToken() {
        String oToken = token;
        synchronized (this) {
            //hvis toknet er refreseht i annen tråd
            if(token != null && !token.equals(oToken)) {
                return token;
            }

            Request request = new Request
                    .Builder()
                    .url(url)
                    .header(AUTHORIZATION, okhttp3.Credentials.basic(sbsServiceUser.username, sbsServiceUser.password))
                    .post(RequestBody.create(null, new byte[0]))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                RestUtils.throwIfNotSuccessful(response);
                Token tokenResponse = RestUtils.parseJsonResponseOrThrow(response, Token.class);
                token = tokenResponse.access_token;
                return token;
            }
        }
    }

    private static class Token {
        String access_token;
    }

}
