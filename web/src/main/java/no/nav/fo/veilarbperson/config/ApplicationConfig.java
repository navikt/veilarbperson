package no.nav.fo.veilarbperson.config;

import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "no.nav.fo.veilarbperson")
public class ApplicationConfig {

}