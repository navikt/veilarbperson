package no.nav.veilarbperson.service;

import no.nav.common.abac.Pep;
import no.nav.common.abac.domain.AbacPersonId;
import no.nav.common.abac.domain.request.ActionId;
import no.nav.common.auth.subject.IdentType;
import no.nav.common.auth.subject.SsoToken;
import no.nav.common.auth.subject.SubjectHandler;
import no.nav.common.client.aktorregister.AktorregisterClient;
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
        IdentType identType = SubjectHandler.getIdentType().orElse(null);
        return IdentType.EksternBruker.equals(identType);
    }

    public boolean erInternBruker() {
        IdentType identType = SubjectHandler.getIdentType().orElse(null);
        return IdentType.InternBruker.equals(identType);
    }

    public void sjekkLesetilgang(String fnr) {
        if (!harLesetilgang(fnr)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    public boolean harLesetilgang(String fnr) {
        String aktorId = aktorregisterClient.hentAktorId(fnr);
        return veilarbPep.harVeilederTilgangTilPerson(getInnloggetBrukerToken(), ActionId.READ, AbacPersonId.aktorId(aktorId));
    }

    public String getAktorId(String fnr) {
        return aktorregisterClient.hentAktorId(fnr);
    }

    public String getInnloggetBrukerToken() {
        return SubjectHandler
                .getSsoToken()
                .map(SsoToken::getToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is missing"));
    }

    public String getInnloggerBrukerIdent() {
        return SubjectHandler
                .getIdent()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject is missing ident"));
    }

}
