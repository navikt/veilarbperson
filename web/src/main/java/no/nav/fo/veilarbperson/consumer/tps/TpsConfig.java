package no.nav.fo.veilarbperson.consumer.tps;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1;
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
    private static final String EGENANSATT_TPS_MOCK_KEY = "egenansatt.withmock";

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

    @Bean
    public EgenAnsattV1 egenAnsattPortType() {
        EgenAnsattV1 prod = egenAnsattFactory().configureStsForOnBehalfOfWithJWT().build();
        EgenAnsattV1 mock = new EgenAnsattMock();

        return createMetricsProxyWithInstanceSwitcher("TPS", prod, mock, EGENANSATT_TPS_MOCK_KEY, EgenAnsattV1.class);
    }

    @Bean
    public Pingable egenAnsattPing() {
        final EgenAnsattV1 egenAnsattV1 = egenAnsattFactory()
                .configureStsForSystemUserInFSS()
                .build();

        PingMetadata metadata = new PingMetadata(
                "virksomhet:EgenAnsatt_v1 via " + getEndpoint(EGENANSATT_TPS_MOCK_KEY, "egenansatt.endpoint.url"),
                "Tjeneste for å hente informasjon om EgenAnsatt",
                true
        );

        return () -> {
            try {
                egenAnsattV1.ping();
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

    private CXFClient<EgenAnsattV1> egenAnsattFactory() {
        return new CXFClient<>(EgenAnsattV1.class)
                .address(getProperty("egenansatt.endpoint.url"))
                .withOutInterceptor(new LoggingOutInterceptor());
    }

    private static String getEndpoint(String mockKey, String endpointKey) {
        if ("true".equalsIgnoreCase(System.getProperty(mockKey))) {
            return "MOCK";
        }
        return System.getProperty(endpointKey);
    }
}
