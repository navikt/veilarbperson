package no.nav.veilarbperson.client.dkif;

import no.nav.common.health.HealthCheck;

public interface DkifClient extends HealthCheck {

    DkifKontaktinfo hentKontaktInfo(String fnr);

}
