package no.nav.veilarbperson.service;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.SneakyThrows;
import no.nav.common.audit_log.cef.AuthorizationDecision;
import no.nav.common.audit_log.cef.CefMessage;
import no.nav.common.audit_log.cef.CefMessageEvent;
import no.nav.common.audit_log.cef.CefMessageSeverity;
import no.nav.common.audit_log.log.AuditLogger;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.auth.utils.IdentUtils;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.token_client.client.AzureAdOnBehalfOfTokenClient;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.Fnr;
import no.nav.common.types.identer.NavIdent;
import no.nav.poao_tilgang.client.*;
import no.nav.veilarbperson.config.EnvironmentProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static no.nav.common.auth.Constants.AAD_NAV_IDENT_CLAIM;

@Service
@Primary
public class AuthService {

    private final AktorOppslagClient aktorOppslagClient;

    private final AuthContextHolder authContextHolder;

    private final EnvironmentProperties environmentProperties;

    private final AzureAdOnBehalfOfTokenClient aadOboTokenClient;

    private final PoaoTilgangClient poaoTilgangClient;

    private final AuditLogger auditLogger;

    public AuthService(
                       AktorOppslagClient aktorOppslagClient,
                       AuthContextHolder authContextHolder,
                       EnvironmentProperties environmentProperties,
                       AzureAdOnBehalfOfTokenClient aadOboTokenClient,
                       PoaoTilgangClient poaoTilgangClient,
                       AuditLogger auditLogger
    ) {
        this.aktorOppslagClient = aktorOppslagClient;
        this.authContextHolder = authContextHolder;
        this.environmentProperties = environmentProperties;
        this.aadOboTokenClient = aadOboTokenClient;
        this.poaoTilgangClient = poaoTilgangClient;
        this.auditLogger = auditLogger;
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
        if (erEksternBruker()) {
            harSikkerhetsNivaa4();
            return harEksternBrukerTilgangTilEksternBruker(fnr.get());
        } else if (erInternBruker()) {
            return harVeilederTilgangTilEksternBruker(fnr.get());
        } else {
            if (!erSystemBruker()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            return harAADRolleForSystemTilSystemTilgang();
        }
    }

    private boolean harEksternBrukerTilgangTilEksternBruker(String ressursNorskIdent) {
        Decision desicion = poaoTilgangClient.evaluatePolicy(new EksternBrukerTilgangTilEksternBrukerPolicyInput(
                authContextHolder.getUid().orElseThrow(), ressursNorskIdent
        )).getOrThrow();

        if (auditLogger != null){
            auditLogWithMessageAndDestinationUserId(
                    "Ekstern bruker har gjort oppslag på ekstern bruker",
                    ressursNorskIdent,
                    authContextHolder.getUid().get(),
                    desicion.isPermit() ? AuthorizationDecision.PERMIT : AuthorizationDecision.DENY
            );
        }


        return desicion.isPermit();
    }

    private boolean harVeilederTilgangTilEksternBruker(String eksternBruker) {
        Decision desicion = poaoTilgangClient.evaluatePolicy(new NavAnsattTilgangTilEksternBrukerPolicyInput(
                hentInnloggetVeilederUUID(), TilgangType.LESE, eksternBruker
        )).getOrThrow();

        if (auditLogger != null){
            auditLogWithMessageAndDestinationUserId(
                    "Veileder har gjort oppslag på bruker",
                    eksternBruker,
                    getNavIdentClaimHvisTilgjengelig().orElseThrow().get(),
                    desicion.isPermit() ? AuthorizationDecision.PERMIT : AuthorizationDecision.DENY
            );
        }

        return desicion.isPermit();
    }

    private void auditLogWithMessageAndDestinationUserId(String logMessage, String destinationUserId, String sourceUserID, AuthorizationDecision authorizationDecision) {
        auditLogger.log(CefMessage.builder()
                .timeEnded(System.currentTimeMillis())
                .applicationName("veilarbperson")
                .sourceUserId(sourceUserID)
                .authorizationDecision(authorizationDecision)
                .event(CefMessageEvent.ACCESS)
                .severity(CefMessageSeverity.INFO)
                .name("veilarbperson-audit-log")
                .destinationUserId(destinationUserId)
                .extension("msg", logMessage)
                .build());
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
        return aktorOppslagClient.hentAktorId(fnr);
    }

    public String getInnloggerBrukerUid() {
        return authContextHolder.getUid()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Subject is missing"));
    }

    public String getAadOboTokenForTjeneste(String tokenScope) {
        return aadOboTokenClient.exchangeOnBehalfOfToken(tokenScope, authContextHolder.requireIdTokenString());
    }

    public UUID hentInnloggetVeilederUUID() {
        return authContextHolder.getIdTokenClaims()
                .flatMap(claims -> getStringClaimOrEmpty(claims, "oid"))
                .map(UUID::fromString)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Fant ikke oid for innlogget veileder"));
    }

    public void harSikkerhetsNivaa4() {
        Optional<String> acrClaim = authContextHolder.getIdTokenClaims()
                .flatMap(claims -> getStringClaimOrEmpty(claims, "acr"));
        if (acrClaim.isEmpty() || !acrClaim.get().equals("Level4")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    public static Optional<String> getStringClaimOrEmpty(JWTClaimsSet claims, String claimName) {
        try {
            return ofNullable(claims.getStringClaim(claimName));
        } catch (Exception e) {
            return empty();
        }
    }

    @SneakyThrows
    private Optional<NavIdent> getNavIdentClaimHvisTilgjengelig() {
        if (erInternBruker()) {
            return Optional.ofNullable(authContextHolder.requireIdTokenClaims().getStringClaim(AAD_NAV_IDENT_CLAIM))
                    .filter(IdentUtils::erGydligNavIdent)
                    .map(NavIdent::of);
        }
        return empty();
    }

}
