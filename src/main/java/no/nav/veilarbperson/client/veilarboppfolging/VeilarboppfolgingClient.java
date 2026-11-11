package no.nav.veilarbperson.client.veilarboppfolging;

import no.nav.common.health.HealthCheck;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.domain.visittkort.OppfolgingEtikettgrunnlag;

import java.util.Set;

public interface VeilarboppfolgingClient extends HealthCheck {

    UnderOppfolging hentUnderOppfolgingStatus(Fnr fnr);

    boolean hentKvpStatus(Fnr fnr);

    /**
     * Henter kun grunnlaget som trengs for de etterspurte etikettene, i ett GraphQL-kall.
     * Felter som ikke er etterspurt (eller avhengighet av etterspurte felter) er null.
     */
    OppfolgingEtikettgrunnlag hentEtikettgrunnlag(Fnr fnr, Set<String> felter);

}
