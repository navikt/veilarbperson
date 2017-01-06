package no.nav.fo.veilarbperson.config;

import no.nav.modig.security.ws.SystemSAMLOutInterceptor;
import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.tjeneste.virksomhet.person.v2.PersonV2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class TpsConfig {

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
        return new CXFClient<>(PersonV2.class).address("https://wasapp-t4.adeo.no/tpsws/Person_v2");
    }

}
