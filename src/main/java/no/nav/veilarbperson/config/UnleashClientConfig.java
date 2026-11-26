package no.nav.veilarbperson.config;

import io.getunleash.DefaultUnleash;
import io.getunleash.FakeUnleash;
import io.getunleash.Unleash;
import io.getunleash.UnleashContext;
import io.getunleash.util.UnleashConfig;
import no.nav.common.utils.EnvironmentUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import static no.nav.veilarbperson.config.ApplicationConfig.APPLICATION_NAME;

@Configuration
public class UnleashClientConfig {

    @Bean(destroyMethod = "shutdown")
    public Unleash unleashClient(Environment environment) {
        if (!EnvironmentUtils.isDevelopment().orElse(false)) {
            FakeUnleash disabledClient = new FakeUnleash();
            disabledClient.disableAll();
            return disabledClient;
        }

        String unleashUrl = environment.getRequiredProperty("UNLEASH_SERVER_API_URL");
        String unleashApiToken = environment.getRequiredProperty("UNLEASH_SERVER_API_TOKEN");

        return new DefaultUnleash(UnleashConfig.builder()
                .appName(APPLICATION_NAME)
                .instanceId(environment.getProperty("NAIS_POD_NAME", "local"))
                .unleashAPI(unleashUrl + "/api")
                .apiKey(unleashApiToken)
                .unleashContextProvider(() -> UnleashContext.builder()
                        .environment("development")
                        .build())
                .build());
    }
}
