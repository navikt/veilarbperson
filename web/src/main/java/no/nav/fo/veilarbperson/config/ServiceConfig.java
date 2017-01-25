package  no.nav.fo.veilarbperson.config;

import no.nav.fo.veilarbperson.PersonFletter;
import no.nav.fo.veilarbperson.digitalkontaktinformasjon.DigitalKontaktinformasjonService;
import no.nav.fo.veilarbperson.kodeverk.KodeverkManager;
import no.nav.fo.veilarbperson.kodeverk.KodeverkService;
import no.nav.fo.veilarbperson.services.*;
import org.springframework.context.annotation.Bean;

public class ServiceConfig {

    @Bean
    PersonService personService() {return new PersonService(); }

    @Bean
    EgenAnsattService egenAnsattService() {return new EgenAnsattService();}

    @Bean
    PersonFletter personFletter(){ return new PersonFletter(); }

    @Bean
    EnhetService enhetService(){ return new EnhetService(); }

    @Bean
    DigitalKontaktinformasjonService digitalKontaktinformasjonService() {
        return new DigitalKontaktinformasjonService();
    }

    @Bean
    KodeverkService kodeverkService() {
        return new KodeverkService();
    }

    @Bean
    KodeverkManager kodeverkManager() {
        return new KodeverkManager();
    }

    @Bean
    PersonDataMapper personDataMapper() {
        return new PersonDataMapper();
    }
}