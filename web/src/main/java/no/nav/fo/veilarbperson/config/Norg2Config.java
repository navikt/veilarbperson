package no.nav.fo.veilarbperson.config;

import no.nav.modig.security.ws.SystemSAMLOutInterceptor;
import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v1.OrganisasjonEnhetV1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;


@Configuration
public class Norg2Config {

    private static final String ENHET_NORG2_ENDPOINT_KEY = "organisasjonenhet.endpoint.url";
    private static final String ENHET_NORG2_MOCK_KEY = "organisasjonenhet.endpoint.url";

    @Bean
    public OrganisasjonEnhetV1 organisasjonEnhetPortType() {
        OrganisasjonEnhetV1 prod = factory().withOutInterceptor(new SystemSAMLOutInterceptor()).build();
        OrganisasjonEnhetV1 mock = new OrganisasjonEnhetMock();
        return createMetricsProxyWithInstanceSwitcher("NORG2", prod, mock, ENHET_NORG2_MOCK_KEY, OrganisasjonEnhetV1.class);
    }

    private CXFClient<OrganisasjonEnhetV1> factory() {
        return new CXFClient<>(OrganisasjonEnhetV1.class).address(getProperty(ENHET_NORG2_ENDPOINT_KEY));
    }
}
