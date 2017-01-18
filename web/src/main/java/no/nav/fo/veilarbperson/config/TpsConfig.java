package no.nav.fo.veilarbperson.config;

import no.nav.modig.security.ws.SystemSAMLOutInterceptor;
import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.tjeneste.virksomhet.person.v2.PersonV2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class TpsConfig {

    private static final String PERSON_TPS_MOCK_KEY = "personservice.withmock";

    @Bean
    public PersonV2 personPortType() {
        PersonV2 prod = factory().withOutInterceptor(new TestOutInterceptor()).build();
        PersonV2 mock = new PersonMock();

        return createMetricsProxyWithInstanceSwitcher("TPS", prod, mock, PERSON_TPS_MOCK_KEY, PersonV2.class);
    }

    @Bean
    public Pingable personPing() {
        final PersonV2 personV2 = factory()
                .withOutInterceptor(new SystemSAMLOutInterceptor())
                .build();
        return () -> {
            try {
                personV2.ping();
                return lyktes("PERSON_V2");
            } catch (Exception e) {
                return feilet("PERSON_V2", e);
            }
        };
    }

    private CXFClient<PersonV2> factory() {
        return new CXFClient<>(PersonV2.class).address(getProperty("person.endpoint.url"));
    }

}
