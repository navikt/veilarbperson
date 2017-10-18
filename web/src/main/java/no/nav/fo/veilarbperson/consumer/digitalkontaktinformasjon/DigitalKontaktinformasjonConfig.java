package no.nav.fo.veilarbperson.consumer.digitalkontaktinformasjon;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class DigitalKontaktinformasjonConfig {
    private static final String DIGITAL_KONTAKTINFORMASJON_MOCK_KEY = "digitalkontaktinformasjon.withmock";

    @Bean
    public DigitalKontaktinformasjonV1 digitalKontaktinformasjonPortType() {
        DigitalKontaktinformasjonV1 prod = factory().configureStsForOnBehalfOfWithJWT().build();
        DigitalKontaktinformasjonV1 mock = new DigitalKontaktinformasjonMock();

        return createMetricsProxyWithInstanceSwitcher("digitalkontaktinformasjon", prod, mock, DIGITAL_KONTAKTINFORMASJON_MOCK_KEY, DigitalKontaktinformasjonV1.class);
    }

    @Bean
    public Pingable digitalKontaktinformasjonPing() {
        final DigitalKontaktinformasjonV1 digitalKontaktinformasjonV1 = factory()
                .configureStsForSystemUserInFSS()
                .build();

        PingMetadata metadata = new PingMetadata(
                "virksomhet:DigitalKontakinformasjon_v1 via " + getEndpoint(),
                "Ping av digitalkontaktinformasjon.",
                true
        );

        return () -> {
            try {
                digitalKontaktinformasjonV1.ping();
                return lyktes(metadata);
            } catch (Exception e) {
                return feilet(metadata, e);
            }
        };
    }
    private CXFClient<DigitalKontaktinformasjonV1> factory() {
        return new CXFClient<>(DigitalKontaktinformasjonV1.class)
                .address(getProperty("digitalkontaktinformasjon.endpoint.url"))
                .withOutInterceptor(new LoggingOutInterceptor())
                ;
    }

    private static String getEndpoint() {
        if ("true".equalsIgnoreCase(System.getProperty(DIGITAL_KONTAKTINFORMASJON_MOCK_KEY))) {
            return "MOCK";
        }
        return System.getProperty("digitalkontaktinformasjon.endpoint.url");
    }
}
