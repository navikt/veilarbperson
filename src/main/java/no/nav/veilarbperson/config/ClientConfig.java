package no.nav.veilarbperson.config;

import no.nav.veilarbperson.client.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.common.utils.EnvironmentUtils.getRequiredProperty;

@Configuration
public class ClientConfig {

    @Bean
    public DkifClient dkifClient() {
        return new DkifClientImpl("http://dkif.default.svc.nais.local");
    }

    @Bean
    public EgenAnsattClient egenAnsattClient() {
        return new EgenAnsattClientImpl(getRequiredProperty("VIRKSOMHET_EGENANSATT_V1_ENDPOINTURL"));
    }

    @Bean
    public PersonClient personClient() {
        return new PersonClientImpl(getRequiredProperty("VIRKSOMHET_PERSON_V3_ENDPOINTURL"));
    }

}
