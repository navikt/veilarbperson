package no.nav.fo.veilarbperson.config;

import no.nav.fo.veilarbperson.consumer.digitalkontaktinformasjon.DigitalKontaktinformasjonService;
import no.nav.fo.veilarbperson.consumer.kodeverk.KodeverkService;
import no.nav.fo.veilarbperson.consumer.organisasjonenhet.EnhetService;
import no.nav.fo.veilarbperson.consumer.person.EgenAnsattService;
import no.nav.fo.veilarbperson.consumer.person.PersonService;
import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v1.OrganisasjonEnhetV1;
import no.nav.tjeneste.virksomhet.person.v2.PersonV2;
import org.springframework.context.annotation.Bean;

public class ServiceConfig {

    private final PersonV2 personV2;
    private final OrganisasjonEnhetV1 organisasjonenhet;
    private final DigitalKontaktinformasjonV1 digitalKontaktinformasjonV1;
    private final EgenAnsattV1 egenAnsattV1;
    private final KodeverkPortType kodeverkPortType;

    public ServiceConfig(PersonV2 personV2, OrganisasjonEnhetV1 organisasjonenhet, DigitalKontaktinformasjonV1 digitalKontaktinformasjonV1, EgenAnsattV1 egenAnsattV1, KodeverkPortType kodeverkPortType) {
        this.personV2 = personV2;
        this.organisasjonenhet = organisasjonenhet;
        this.digitalKontaktinformasjonV1 = digitalKontaktinformasjonV1;
        this.egenAnsattV1 = egenAnsattV1;
        this.kodeverkPortType = kodeverkPortType;
    }

    @Bean
    PersonService personService() {
        return new PersonService(personV2);
    }

    @Bean
    EgenAnsattService egenAnsattService() {
        return new EgenAnsattService(egenAnsattV1);
    }


    @Bean
    EnhetService enhetService() {
        return new EnhetService(organisasjonenhet);
    }

    @Bean
    DigitalKontaktinformasjonService digitalKontaktinformasjonService() {
        return new DigitalKontaktinformasjonService(digitalKontaktinformasjonV1);
    }

    @Bean
    KodeverkService kodeverkService() {
        return new KodeverkService(kodeverkPortType);
    }

}