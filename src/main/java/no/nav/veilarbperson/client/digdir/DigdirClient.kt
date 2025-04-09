package no.nav.veilarbperson.client.digdir;

import no.nav.common.health.HealthCheck;
import no.nav.common.types.identer.Fnr;

public interface DigdirClient extends HealthCheck {

    DigdirKontaktinfo hentKontaktInfo(Fnr fnr);

}
