package no.nav.fo.veilarbperson.config;

import no.nav.fo.veilarbperson.PersonFletter;
import org.springframework.context.annotation.*;

@Configuration
@ComponentScan(basePackages = "no.nav.fo.veilarbperson")
@Import({
        ServiceConfig.class,
        PersonFletter.class,
        CacheConfig.class
})
public class ApplicationConfig {

}