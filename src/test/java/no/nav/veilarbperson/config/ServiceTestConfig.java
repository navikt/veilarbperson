package no.nav.veilarbperson.config;

import no.nav.veilarbperson.service.AuthService;
import no.nav.veilarbperson.service.KodeverkService;
import no.nav.veilarbperson.service.PersonService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        AuthService.class,
        PersonService.class,
        KodeverkService.class
})
public class ServiceTestConfig {}