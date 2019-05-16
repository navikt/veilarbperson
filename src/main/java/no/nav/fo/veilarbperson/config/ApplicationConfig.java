package no.nav.fo.veilarbperson.config;

import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.config.ApiAppConfigurator;
import no.nav.dialogarena.aktor.AktorConfig;
import no.nav.fo.veilarbperson.PersonFletter;
import no.nav.sbl.dialogarena.common.abac.pep.context.AbacContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ComponentScan(basePackages = "no.nav.fo.veilarbperson")
@Import({
        ServiceConfig.class,
        PersonFletter.class,
        CacheConfig.class,
        AbacContext.class,
        AktorConfig.class,
})
public class ApplicationConfig implements ApiApplication {
    public static final String AKTOER_V2_URL_PROPERTY = "AKTOER_V2_ENDPOINTURL";

    @Override
    public void configure(ApiAppConfigurator apiAppConfigurator) {
        apiAppConfigurator
                .azureADB2CLogin()
                .issoLogin()
                .sts();
    }
}
