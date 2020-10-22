package no.nav.veilarbperson.client.difi;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;


public class AccessTokenAuthenticator implements Authenticator {
    private final AccessTokenRepository accessTokenRepository;

    public AccessTokenAuthenticator(AccessTokenRepository accessTokenRepository) {
        this.accessTokenRepository = accessTokenRepository;
    }


    @Override
    public Request authenticate(Route route, Response response) {
        if (!isRequestWithAccessToken(response)) {
            return null;
        }
        String authHeader = response.request().header(AUTHORIZATION);
        String oldAccessToken = authHeader.replace("Bearer ", "");


        synchronized (this) {
            final String newAccessToken = accessTokenRepository.getAccessToken();
            // If access token is refreshed while request in fligth
            if (!oldAccessToken.equals(newAccessToken)) {
                return newRequestWithAccessToken(response.request(), newAccessToken);
            }

            // Need to refresh an access token
            final String updatedAccessToken = accessTokenRepository.refreshAccessToken();
            return newRequestWithAccessToken(response.request(), updatedAccessToken);
        }
    }

    private boolean isRequestWithAccessToken( Response response) {
        String header = response.request().header("Authorization");
        return header != null && header.startsWith("Bearer");
    }

    
    private Request newRequestWithAccessToken( Request request,  String accessToken) {
        return request.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .build();
    }
}
