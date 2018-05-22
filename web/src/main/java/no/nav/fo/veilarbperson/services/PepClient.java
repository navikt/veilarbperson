package no.nav.fo.veilarbperson.services;

import no.nav.brukerdialog.security.context.SubjectHandler;
import no.nav.brukerdialog.security.oidc.OidcTokenUtils;
import no.nav.common.auth.SsoToken;
import no.nav.sbl.dialogarena.common.abac.pep.Pep;
import no.nav.sbl.dialogarena.common.abac.pep.domain.response.BiasedDecisionResponse;
import no.nav.sbl.dialogarena.common.abac.pep.domain.response.Decision;
import no.nav.sbl.dialogarena.common.abac.pep.exception.PepException;
import org.slf4j.Logger;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;

import static no.nav.common.auth.SsoToken.Type.OIDC;
import static no.nav.common.auth.SubjectHandler.*;
import static org.slf4j.LoggerFactory.getLogger;

public class PepClient {

    final private Pep pep;
    private static final Logger LOG = getLogger(PepClient.class);

    public PepClient(Pep pep) {
        this.pep = pep;
    }

    public boolean isServiceCallAllowed(String fnr) {
        return getSsoToken(OIDC).map(t -> isServiceCallAllowed(fnr, t)).orElse(false);
    }

    private boolean isServiceCallAllowed(String fnr, String oidcToken) {
        BiasedDecisionResponse callAllowed;
        try {
            callAllowed = pep.isServiceCallAllowedWithOidcToken(oidcToken, "veilarb", fnr);

        } catch (PepException e) {
            LOG.error("Something went wrong in PEP", e);
            throw new InternalServerErrorException("something went wrong in PEP", e);
        }
        if (callAllowed.getBiasedDecision().equals(Decision.Deny)) {
            final String ident = SubjectHandler.getSubjectHandler().getUid();
            throw new NotAuthorizedException(ident + " doesn't have access to " + fnr);
        }
        return callAllowed.getBiasedDecision().equals(Decision.Permit);
    }

}