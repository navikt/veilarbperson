package no.nav.veilarbperson.config;

import no.nav.veilarbperson.controller.PersonGraphQLController;
import no.nav.veilarbperson.controller.InternalController;
import no.nav.veilarbperson.controller.PersonController;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        InternalController.class,
        PersonController.class,
        PersonGraphQLController.class
})
public class ControllerTestConfig {}
