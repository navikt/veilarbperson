package  no.nav.fo.veilarbperson.config;

import no.nav.fo.veilarbperson.PersonFletter;
import no.nav.fo.veilarbperson.services.EnhetService;
import no.nav.fo.veilarbperson.services.PersonService;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v1.binding.OrganisasjonEnhetV1;
import org.springframework.context.annotation.Bean;

public class ServiceConfig {

    @Bean
    PersonService personService() {return new PersonService(); }

    @Bean
    PersonFletter personFletter(){ return new PersonFletter(); }

    @Bean
    EnhetService enhetService(){ return new EnhetService(); }
}