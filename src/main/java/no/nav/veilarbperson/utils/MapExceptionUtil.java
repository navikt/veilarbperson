package no.nav.veilarbperson.utils;

import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class MapExceptionUtil {

    public static ResponseStatusException map(Throwable error) {

        HttpStatus feilStatus;
        if (error instanceof HentPersonPersonIkkeFunnet) {
            feilStatus = HttpStatus.NOT_FOUND;
        } else if (error instanceof HentPersonSikkerhetsbegrensning) {
            feilStatus = HttpStatus.FORBIDDEN;
        } else {
            feilStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseStatusException(feilStatus);
    }
}
