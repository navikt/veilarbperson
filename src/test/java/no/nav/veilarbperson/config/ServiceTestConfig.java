package no.nav.veilarbperson.config;

import no.nav.veilarbperson.service.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


@Configuration
@Import({
		AuthService.class,
		KodeverkService.class,
		PersonV2Service.class,
		CvJobbprofilService.class,
		OppslagArbeidssoekerregisteretService.class
})
public class ServiceTestConfig {}
