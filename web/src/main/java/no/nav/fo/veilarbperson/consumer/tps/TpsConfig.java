package no.nav.fo.veilarbperson.consumer.tps;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.tjeneste.virksomhet.person.v3.PersonV3;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
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
    public PersonV3 personPortType() {
        PersonV3 prod = factory().configureStsForOnBehalfOfWithJWT().build();
        PersonV3 mock = new PersonMock();

        return createMetricsProxyWithInstanceSwitcher("TPS", prod, mock, PERSON_TPS_MOCK_KEY, PersonV3.class);
    }

    @Bean
    public Pingable personPing() {
        final PersonV3 personV3 = factory()
                .configureStsForSystemUserInFSS()
                .build();

        Pingable.Ping.PingMetadata metadata = new Pingable.Ping.PingMetadata(
                "virksomhet:Person_V3 via " + getEndpoint(PERSON_TPS_MOCK_KEY, "person.endpoint.url"),
                "Henter informasjon om en bestemt person (TPS).",
                true
        );

        return () -> {
            try {
                personV3.ping();
                return lyktes(metadata);
            } catch (Exception e) {
                return feilet(metadata, e);
            }
        };
    }

    private CXFClient<PersonV3> factory() {
        return new CXFClient<>(PersonV3.class)
                .address(getProperty("person.endpoint.url"))
                .withOutInterceptor(new LoggingOutInterceptor());
    }

    private static String getEndpoint(String mockKey, String endpointKey) {
        if ("true".equalsIgnoreCase(System.getProperty(mockKey))) {
            return "MOCK";
        }
        return System.getProperty(endpointKey);
    }
}
