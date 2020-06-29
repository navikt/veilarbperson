package no.nav.veilarbperson.client.egenansatt;

import no.nav.common.cxf.CXFClient;
import no.nav.common.cxf.StsConfig;
import no.nav.common.health.HealthCheckResult;
import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1;
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest;
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse;
import no.nav.veilarbperson.config.CacheConfig;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.springframework.cache.annotation.Cacheable;

public class EgenAnsattClientImpl implements EgenAnsattClient {

    private final EgenAnsattV1 egenAnsattV1;

    private final EgenAnsattV1 egenAnsattV1Ping;

    public EgenAnsattClientImpl(String egenAnsattV1Endepoint, StsConfig stsConfig) {
        egenAnsattV1 = new CXFClient<>(EgenAnsattV1.class)
                .address(egenAnsattV1Endepoint)
                .withOutInterceptor(new LoggingOutInterceptor())
                .configureStsForSubject(stsConfig)
                .build();


        egenAnsattV1Ping = new CXFClient<>(EgenAnsattV1.class)
                .address(egenAnsattV1Endepoint)
                .configureStsForSystemUser(stsConfig)
                .build();
    }

    @Cacheable(CacheConfig.EGEN_ANSATT_CACHE_NAME)
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
