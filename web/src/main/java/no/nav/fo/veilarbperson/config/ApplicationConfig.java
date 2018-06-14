package no.nav.fo.veilarbperson.config;

import no.nav.apiapp.ApiApplication;
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
        AbacContext.class
})
public class ApplicationConfig implements ApiApplication {

    @Override
    public String getApplicationName() {
        return "veilarbperson";
    }

}