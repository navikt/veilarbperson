package no.nav.veilarbperson.client.veilarboppfolging;

import no.nav.common.health.HealthCheck;
import no.nav.common.types.identer.Fnr;

public interface VeilarboppfolgingClient extends HealthCheck {

    UnderOppfolging hentUnderOppfolgingStatus(Fnr fnr);

}
