package no.nav.veilarbperson.config;

import no.nav.veilarbperson.controller.InternalController;
import no.nav.veilarbperson.controller.PersonController;
import no.nav.veilarbperson.controller.PingController;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        PersonController.class,
        InternalController.class,
        PingController.class
})
public class ControllerTestConfig {}
