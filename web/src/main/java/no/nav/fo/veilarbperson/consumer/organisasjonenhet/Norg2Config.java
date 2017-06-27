package no.nav.fo.veilarbperson.consumer.organisasjonenhet;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v1.OrganisasjonEnhetV1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;


@Configuration
public class Norg2Config {

    private static final String ENHET_NORG2_ENDPOINT_KEY = "organisasjonenhet.endpoint.url";
    private static final String ENHET_NORG2_MOCK_KEY = "organisasjonenhet.endpoint.url";

    @Bean
    public OrganisasjonEnhetV1 organisasjonEnhetPortType() {
        OrganisasjonEnhetV1 prod = factory().configureStsForOnBehalfOfWithJWT().build();
        OrganisasjonEnhetV1 mock = new OrganisasjonEnhetMock();
        return createMetricsProxyWithInstanceSwitcher("NORG2", prod, mock, ENHET_NORG2_MOCK_KEY, OrganisasjonEnhetV1.class);
    }

    private CXFClient<OrganisasjonEnhetV1> factory() {
        return new CXFClient<>(OrganisasjonEnhetV1.class)
                .address(getProperty(ENHET_NORG2_ENDPOINT_KEY))
                .configureStsForSystemUser();
    }

    @Bean
    public Pingable organisasjonEnhetPing() {
        final OrganisasjonEnhetV1 organisasjonEnhetV1 = factory().build();

        Pingable.Ping.PingMetadata metadata = new Pingable.Ping.PingMetadata(
                "virksomhet:OrganisasjonEnhet_v1 via " + getEndpoint(),
                "Ping av organisasjonsenhet (NORG2).",
                true
        );

        return () -> {
            try {
                organisasjonEnhetV1.ping();
                return lyktes(metadata);
            } catch (Exception e) {
                return feilet(metadata, e);
            }
        };
    }

    private static String getEndpoint() {
        if ("true".equalsIgnoreCase(System.getProperty(ENHET_NORG2_MOCK_KEY))) {
            return "MOCK";
        }
        return System.getProperty(ENHET_NORG2_ENDPOINT_KEY);
    }
}
