package no.nav.veilarbperson.utils;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.auth.context.AuthContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
public class RestClientUtils {

    public static String authHeaderMedInnloggetBruker() {
        return AuthContextHolder.getIdTokenString()
                .map(RestClientUtils::createBearerToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Fant ikke token til innlogget bruker"));
    }

    public static String createBearerToken(String token) {
        return "Bearer " + token;
    }
}
