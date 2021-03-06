package no.nav.veilarbperson.config;

import no.nav.veilarbperson.controller.InternalController;
import no.nav.veilarbperson.controller.PersonV2Controller;
import no.nav.veilarbperson.controller.PersonController;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        PersonController.class,
        InternalController.class,
        PersonV2Controller.class
})
public class ControllerTestConfig {}
