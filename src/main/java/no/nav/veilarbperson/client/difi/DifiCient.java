package no.nav.veilarbperson.client.difi;

import no.nav.common.health.HealthCheck;

public interface DifiCient extends HealthCheck {
    HarLoggetInnRespons harLoggetInnSiste18mnd(String fnr);

    static String getDifiUrl() {
        return null;
    }
}
