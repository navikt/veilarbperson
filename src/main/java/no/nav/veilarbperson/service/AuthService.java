package no.nav.veilarbperson.service;

import com.nimbusds.jwt.JWTClaimsSet;
import no.nav.common.abac.Pep;
import no.nav.common.abac.domain.request.ActionId;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.client.aktorregister.AktorregisterClient;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.config.EnvironmentProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

@Service
public class AuthService {

    private final Pep veilarbPep;

    private final AktorregisterClient aktorregisterClient;

    private final AuthContextHolder authContextHolder;

    private final EnvironmentProperties environmentProperties;

    @Autowired
    public AuthService(Pep veilarbPep,
                       AktorregisterClient aktorregisterClient,
                       AuthContextHolder authContextHolder,
                       EnvironmentProperties environmentProperties) {
        this.veilarbPep = veilarbPep;
        this.aktorregisterClient = aktorregisterClient;
        this.authContextHolder = authContextHolder;
        this.environmentProperties = environmentProperties;
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

        if (erEksternBruker()) {
            return veilarbPep.harTilgangTilPerson(getInnloggetBrukerToken(), ActionId.READ, aktorId);
        } else if (erInternBruker()) {
            return veilarbPep.harVeilederTilgangTilPerson(authContextHolder.requireNavIdent(), ActionId.READ, aktorId);
        } else {
            if (!erSystemBruker()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            return harAADRolleForSystemTilSystemTilgang()
                    || veilarbPep.harTilgangTilPerson(getInnloggetBrukerToken(), ActionId.READ, aktorId);
        }
    }

    /*
    Applikasjoner som er pre-authorized og bruker client credentials flow har et claim "role" der verdien er en liste
    og inneholder "access_as_application" som standard
    Ref. https://doc.nais.io/security/auth/azure-ad/access-policy/index.html#custom-roles
     */
    private boolean harAADRolleForSystemTilSystemTilgang() {
        return getIssuerClaim().map(issuer -> issuer.equals(environmentProperties.getNaisAadIssuer())).orElse(false)
                && getRolesClaim().contains("access_as_application");
    }

    private Optional<String> getIssuerClaim() {
        return authContextHolder.getIdTokenClaims().map(JWTClaimsSet::getIssuer);
    }

    private List<String> getRolesClaim() {
        return authContextHolder.getIdTokenClaims().flatMap(claims -> {
            try {
                return Optional.ofNullable(claims.getStringListClaim("roles"));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }).orElse(emptyList());
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
