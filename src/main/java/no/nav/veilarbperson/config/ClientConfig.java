package no.nav.veilarbperson.config;

import no.nav.veilarbperson.client.DkifClient;
import no.nav.veilarbperson.client.DkifClientImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfig {

    @Bean
    public DkifClient dkifClient() {
        return new DkifClientImpl("http://dkif.default.svc.nais.local");
    }

}
