package no.nav.veilarbperson.config;

import no.nav.veilarbperson.controller.v1.InternalController;
import no.nav.veilarbperson.controller.v2.PersonV2Controller;
import no.nav.veilarbperson.controller.v1.PersonController;
import no.nav.veilarbperson.controller.v3.PersonV3Controller;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        PersonController.class,
        InternalController.class,
        PersonV2Controller.class,
        PersonV3Controller.class
})
public class ControllerTestConfig {}
