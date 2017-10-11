package no.nav.fo.veilarbperson.consumer.kodeverk;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;
import static org.apache.cxf.ws.security.SecurityConstants.MUST_UNDERSTAND;

@Configuration
public class KodeverkConfig {
    private static final String DIGITAL_KONTAKTINFORMASJON_MOCK_KEY = "kodeverk.withmock";

    @Bean
    public KodeverkPortType kodverkPortType() {
        KodeverkPortType prod = factory().build();
        return createMetricsProxyWithInstanceSwitcher("kodeverk", prod, null, DIGITAL_KONTAKTINFORMASJON_MOCK_KEY, KodeverkPortType.class);
    }

    @Bean
    public Pingable kodeverkPing() {
        final KodeverkPortType kodeverkPortType = factory()
                .build();
        return () -> {
            try {
                kodeverkPortType.ping();
                return lyktes("KODEVERK");
            } catch (Exception e) {
                return feilet("KODEVERK", e);
            }
        };
    }

    private CXFClient<KodeverkPortType> factory() {
        return new CXFClient<>(KodeverkPortType.class)
                .wsdl("classpath:kodeverk/no/nav/tjeneste/virksomhet/kodeverk/v2/Kodeverk.wsdl")
                .address(getProperty("kodeverk.endpoint.url"))
                .withProperty(MUST_UNDERSTAND, false);
    }
}
