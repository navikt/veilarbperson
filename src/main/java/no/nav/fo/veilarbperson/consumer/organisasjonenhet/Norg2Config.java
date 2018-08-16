package no.nav.fo.veilarbperson.consumer.organisasjonenhet;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.OrganisasjonEnhetV2;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;


@Configuration
public class Norg2Config {

    public static final String ENHET_NORG2_ENDPOINT_KEY = "organisasjonenhet.endpoint.url";
    private static final String ENHET_NORG2_MOCK_KEY = "organisasjonenhet.endpoint.url";

    @Bean
    public OrganisasjonEnhetV2 organisasjonEnhetPortType() {
        OrganisasjonEnhetV2 prod = factory().configureStsForOnBehalfOfWithJWT().build();
        OrganisasjonEnhetV2 mock = new OrganisasjonEnhetMock();
        return createMetricsProxyWithInstanceSwitcher("NORG2", prod, mock, ENHET_NORG2_MOCK_KEY, OrganisasjonEnhetV2.class);
    }

    private CXFClient<OrganisasjonEnhetV2> factory() {
        return new CXFClient<>(OrganisasjonEnhetV2.class)
                .address(getRequiredProperty(ENHET_NORG2_ENDPOINT_KEY))
                .withOutInterceptor(new LoggingOutInterceptor())
                ;
    }

    @Bean
    public Pingable organisasjonEnhetPing() {
        final OrganisasjonEnhetV2 organisasjonEnhetV2 = factory()
                .configureStsForSystemUserInFSS()
                .build();

        Pingable.Ping.PingMetadata metadata = new Pingable.Ping.PingMetadata(
                "virksomhet:OrganisasjonEnhet_v2 via " + getEndpoint(),
                "Ping av organisasjonsenhet (NORG2).",
                true
        );

        return () -> {
            try {
                organisasjonEnhetV2.ping();
                return lyktes(metadata);
            } catch (Exception e) {
                return feilet(metadata, e);
            }
        };
    }

    private static String getEndpoint() {
        if ("true".equalsIgnoreCase(getProperty(ENHET_NORG2_MOCK_KEY))) {
            return "MOCK";
        }
        return getRequiredProperty(ENHET_NORG2_ENDPOINT_KEY);
    }
}
