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

    private final AuthContextHolder authContextHolder;

    @Autowired
    public AuthService(Pep veilarbPep, AktorregisterClient aktorregisterClient, AuthContextHolder authContextHolder) {
        this.veilarbPep = veilarbPep;
        this.aktorregisterClient = aktorregisterClient;
        this.authContextHolder = authContextHolder;
    }

    public void stoppHvisEksternBruker() {
        if (erEksternBruker()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    public boolean erEksternBruker() {
        return authContextHolder.erEksternBruker();
    }

    public boolean erInternBruker() {
        return authContextHolder.erInternBruker();
    }

    public boolean erSystemBruker() {
        return authContextHolder.erSystemBruker();
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
        return authContextHolder.getIdTokenString()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is missing"));
    }

    public String getInnloggerBrukerUid() {
        return authContextHolder.getUid()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Subject is missing"));
    }
}
