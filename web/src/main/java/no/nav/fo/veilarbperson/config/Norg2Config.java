package no.nav.fo.veilarbperson.config;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v1.*;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v1.meldinger.*;
import no.nav.tjeneste.virksomhet.person.v2.PersonV2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class Norg2Config {

    @Bean
    public OrganisasjonEnhetV1 organisasjonEnhetPortType() {
        return factory().withOutInterceptor(new TestOutInterceptor()).build();
    }

    private CXFClient<OrganisasjonEnhetV1> factory() {
        return new CXFClient<>(OrganisasjonEnhetV1.class).address("https://app-t5.adeo.no/norg2/ws/OrganisasjonEnhet/v1");
    }
}
