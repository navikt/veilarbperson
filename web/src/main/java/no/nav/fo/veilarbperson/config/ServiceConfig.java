package  no.nav.fo.veilarbperson.config;

import no.nav.fo.veilarbperson.PersonFletter;
import no.nav.fo.veilarbperson.dkif.DigitalKontaktinformasjonService;
import no.nav.fo.veilarbperson.services.PersonService;
import org.springframework.context.annotation.Bean;

public class ServiceConfig {

    @Bean
    PersonService personService() {return new PersonService(); }

    @Bean
    PersonFletter personFletter(){ return new PersonFletter(); }

    @Bean
    DigitalKontaktinformasjonService dkifService() {
        return new DigitalKontaktinformasjonService();
    }
}