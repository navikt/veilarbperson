package no.nav.fo.veilarbperson.consumer.tps;

import no.nav.modig.security.ws.SystemSAMLOutInterceptor;
import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1;
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
    private static final String EGENANSATT_TPS_MOCK_KEY = "egenansatt.withmock";

    @Bean
    public PersonV2 personPortType() {
        PersonV2 prod = factory().configureStsForOnBehalfOfWithJWT().build();
        PersonV2 mock = new PersonMock();

        return createMetricsProxyWithInstanceSwitcher("TPS", prod, mock, PERSON_TPS_MOCK_KEY, PersonV2.class);
    }

    @Bean
    public Pingable personPing() {
        final PersonV2 personV2 = factory()
                .withOutInterceptor(new SystemSAMLOutInterceptor())
                .build();

        PingMetadata metadata = new PingMetadata(
                "virksomhet:Person_V2 via " + getEndpoint(PERSON_TPS_MOCK_KEY, "person.endpoint.url"),
                "Henter informasjon om en bestemt person (TPS).",
                true
        );

        return () -> {
            try {
                personV2.ping();
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
                .withOutInterceptor(new SystemSAMLOutInterceptor())
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

    private CXFClient<PersonV2> factory() {
        return new CXFClient<>(PersonV2.class).address(getProperty("person.endpoint.url"));
    }

    private CXFClient<EgenAnsattV1> egenAnsattFactory() {
        return new CXFClient<>(EgenAnsattV1.class).address(getProperty("egenansatt.endpoint.url"));
    }

    private static String getEndpoint(String mockKey, String endpointKey) {
        if ("true".equalsIgnoreCase(System.getProperty(mockKey))) {
            return "MOCK";
        }
        return System.getProperty(endpointKey);
    }

}
