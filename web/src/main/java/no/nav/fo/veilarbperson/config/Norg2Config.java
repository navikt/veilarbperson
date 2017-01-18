package no.nav.fo.veilarbperson.config;

import no.nav.modig.security.ws.SystemSAMLOutInterceptor;
import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v1.*;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v1.meldinger.*;
import no.nav.tjeneste.virksomhet.person.v2.PersonV2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.lang.System.getProperty;


@Configuration
public class Norg2Config {

    private static final String NORG2_ENDPOINT_KEY = "norg2.endpoint.url";

    @Bean
    public OrganisasjonEnhetV1 organisasjonEnhetPortType() {
        return factory().withOutInterceptor(new SystemSAMLOutInterceptor()).build();
    }

    private CXFClient<OrganisasjonEnhetV1> factory() {
        return new CXFClient<>(OrganisasjonEnhetV1.class).address(getProperty(NORG2_ENDPOINT_KEY));
    }
}
