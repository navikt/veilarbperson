package no.nav.veilarbperson.config;

import no.nav.veilarbperson.service.AuthService;
import no.nav.veilarbperson.service.KodeverkService;
import no.nav.veilarbperson.service.PersonService;
import no.nav.veilarbperson.service.PersonV2Service;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        AuthService.class,
        PersonService.class,
        KodeverkService.class,
        PersonV2Service.class
})
public class ServiceTestConfig {}
