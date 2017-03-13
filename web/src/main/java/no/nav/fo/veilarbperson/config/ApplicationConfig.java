package no.nav.fo.veilarbperson.config;

import no.nav.fo.veilarbperson.PersonFletter;
import org.springframework.context.annotation.*;
import no.nav.sbl.dialogarena.common.abac.pep.context.AbacContext;

@Configuration
@ComponentScan(basePackages = "no.nav.fo.veilarbperson")
@Import({
        ServiceConfig.class,
        PersonFletter.class,
        CacheConfig.class,
        AbacContext.class
})
public class ApplicationConfig {

}