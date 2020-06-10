package no.nav.veilarbperson.consumer.kodeverk;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;
import static org.apache.cxf.ws.security.SecurityConstants.MUST_UNDERSTAND;

@Configuration
public class KodeverkConfig {
    private static final String KODEVERK_MOCK_KEY = "kodeverk.withmock";
    public static final String KODEVERK_ENDPOINT = "VIRKSOMHET_KODEVERK_V2_ENDPOINTURL";

    @Bean
    public KodeverkPortType kodverkPortType() {
        KodeverkPortType prod = factory().build();
        return createMetricsProxyWithInstanceSwitcher("kodeverk", prod, null, KODEVERK_MOCK_KEY, KodeverkPortType.class);
    }

    @Bean
    KodeverkService kodeverkService(KodeverkPortType kodeverkPortType) {
        return new KodeverkService(kodeverkPortType);
    }

    @Bean
    KodeverkSchedule kodeverkSchedule(KodeverkService kodeverkService) {
        return new KodeverkSchedule(kodeverkService);
    }

    @Bean
    public Pingable kodeverkPing() {
        final KodeverkPortType kodeverkPortType = factory()
                .build();

        PingMetadata metadata = new PingMetadata(
                "kodeverk via " + getEndpoint(),
                "Henter ting som stedsnavn, beskrivelser av sivistand med mer.",
                true
        );

        return () -> {
            try {
                kodeverkPortType.ping();
                return lyktes(metadata);
            } catch (Exception e) {
                return feilet(metadata, e);
            }
        };
    }

    private CXFClient<KodeverkPortType> factory() {
        return new CXFClient<>(KodeverkPortType.class)
                .address(getRequiredProperty(KODEVERK_ENDPOINT))
                .timeout(10000, 30000)
                .withProperty(MUST_UNDERSTAND, false);
    }

    private static String getEndpoint() {
        if ("true".equalsIgnoreCase(System.getProperty(KODEVERK_MOCK_KEY))) {
            return "MOCK";
        }
        return getRequiredProperty(KODEVERK_ENDPOINT);
    }
}
