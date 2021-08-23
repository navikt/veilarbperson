package no.nav.veilarbperson.service;

import no.nav.common.abac.Pep;
import no.nav.common.abac.domain.request.ActionId;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.client.aktorregister.AktorregisterClient;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.Fnr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final Pep veilarbPep;

    private final AktorregisterClient aktorregisterClient;

    @Autowired
    public AuthService(Pep veilarbPep, AktorregisterClient aktorregisterClient) {
        this.veilarbPep = veilarbPep;
        this.aktorregisterClient = aktorregisterClient;
    }

    public void stoppHvisEksternBruker() {
        if (erEksternBruker()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    public boolean erEksternBruker() {
        return AuthContextHolder.erEksternBruker();
    }

    public boolean erInternBruker() {
        return AuthContextHolder.erInternBruker();
    }

    public boolean erSystemBruker() {
        return AuthContextHolder.erSystemBruker();
    }
    public void sjekkLesetilgang(Fnr fnr) {
        if (!harLesetilgang(fnr)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    public boolean harLesetilgang(Fnr fnr) {
        AktorId aktorId = aktorregisterClient.hentAktorId(fnr);
        return veilarbPep.harTilgangTilPerson(getInnloggetBrukerToken(), ActionId.READ, aktorId);
    }

    public AktorId getAktorId(Fnr fnr) {
        return aktorregisterClient.hentAktorId(fnr);
    }

    public String getInnloggetBrukerToken() {
        return AuthContextHolder.getIdTokenString()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is missing"));
    }

    public String getInnloggerBrukerSubject() {
        return AuthContextHolder.getSubject()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Subject is missing"));
    }
}
