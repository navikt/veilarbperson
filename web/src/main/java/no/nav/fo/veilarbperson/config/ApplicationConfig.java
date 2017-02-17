package no.nav.fo.veilarbperson.config;

import no.nav.fo.veilarbperson.PersonFletter;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "no.nav.fo.veilarbperson")
@Import({
        ServiceConfig.class,
        PersonFletter.class,
        CacheConfig.class
})
public class ApplicationConfig {

}