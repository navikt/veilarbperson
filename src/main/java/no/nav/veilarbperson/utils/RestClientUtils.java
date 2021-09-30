package no.nav.veilarbperson.utils;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.auth.context.AuthContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
public class RestClientUtils {

    public static String createBearerToken(String token) {
        return "Bearer " + token;
    }
}
