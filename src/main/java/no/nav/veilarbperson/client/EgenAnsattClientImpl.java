package no.nav.veilarbperson.client;

import no.nav.common.cxf.CXFClient;
import no.nav.common.health.HealthCheckResult;
import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1;
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest;
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse;
import org.apache.cxf.interceptor.LoggingOutInterceptor;

public class EgenAnsattClientImpl implements EgenAnsattClient {

    private final EgenAnsattV1 egenAnsattV1;

    private final EgenAnsattV1 egenAnsattV1Ping;

    public EgenAnsattClientImpl(String egenAnsattV1Endepoint) {
        egenAnsattV1 = new CXFClient<>(EgenAnsattV1.class)
                .address(egenAnsattV1Endepoint)
                .withOutInterceptor(new LoggingOutInterceptor())
                .configureStsForSubject()
                .build();


        egenAnsattV1Ping = new CXFClient<>(EgenAnsattV1.class)
                .address(egenAnsattV1Endepoint)
                .configureStsForSystemUser()
                .build();
    }

    @Override
    public boolean erEgenAnsatt(String ident) {
        final WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest request = new WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest().withIdent(ident);
        WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse wsEgenAnsatt = egenAnsattV1.hentErEgenAnsattEllerIFamilieMedEgenAnsatt(request);
        return wsEgenAnsatt.isEgenAnsatt();
    }

    @Override
    public HealthCheckResult checkHealth() {
        try {
            egenAnsattV1Ping.ping();
            return HealthCheckResult.healthy();
        } catch (Exception e) {
            return HealthCheckResult.unhealthy("Failed to ping egenAnsattV1", e);
        }
    }

}
