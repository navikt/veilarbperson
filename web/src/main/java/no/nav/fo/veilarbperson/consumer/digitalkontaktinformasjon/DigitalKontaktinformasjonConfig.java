package no.nav.fo.veilarbperson.consumer.digitalkontaktinformasjon;

import no.nav.fo.veilarbperson.config.TestOutInterceptor;
import no.nav.modig.security.ws.SystemSAMLOutInterceptor;
import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
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
        DigitalKontaktinformasjonV1 prod = factory().withOutInterceptor(new TestOutInterceptor()).build();
        DigitalKontaktinformasjonV1 mock = new DigitalKontaktinformasjonMock();

        return createMetricsProxyWithInstanceSwitcher("digitalkontaktinformasjon", prod, mock, DIGITAL_KONTAKTINFORMASJON_MOCK_KEY, DigitalKontaktinformasjonV1.class);
    }

    @Bean
    public Pingable digitalKontaktinformasjonPing() {
        final DigitalKontaktinformasjonV1 digitalKontaktinformasjonV1 = factory()
                .withOutInterceptor(new SystemSAMLOutInterceptor())
                .build();
        return () -> {
            try {
                digitalKontaktinformasjonV1.ping();
                return lyktes("DIGITALKONTAKTINFORMASJON_V1");
            } catch (Exception e) {
                return feilet("DIGITALKONTAKTINFORMASJON_V1", e);
            }
        };
    }

    private CXFClient<DigitalKontaktinformasjonV1> factory() {
        return new CXFClient<>(DigitalKontaktinformasjonV1.class).address(getProperty("digitalkontaktinformasjon.endpoint.url"));
    }
}