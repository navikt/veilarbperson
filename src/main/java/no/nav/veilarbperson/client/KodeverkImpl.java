package no.nav.veilarbperson.client;

import no.nav.common.health.HealthCheckResult;
import no.nav.veilarbperson.client.kodeverk.Kodeverk;

public class KodeverkImpl implements KodeverkClient {

    public KodeverkImpl(String egenAnsattV1Endepoint) {}

    @Override
    public String getVerdi(String kodeverkRef, String kode, String sprak) {
        return null;
    }

    @Override
    public Kodeverk hentKodeverk(String kodeverkRef) {
        return null;
    }

    @Override
    public HealthCheckResult checkHealth() {
        try {
//            egenAnsattV1Ping.ping();
            return HealthCheckResult.healthy();
        } catch (Exception e) {
            return HealthCheckResult.unhealthy("Failed to ping egenAnsattV1", e);
        }
    }

}
