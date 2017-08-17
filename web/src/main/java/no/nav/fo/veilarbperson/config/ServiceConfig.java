package no.nav.fo.veilarbperson.config;

import no.nav.fo.veilarbperson.consumer.digitalkontaktinformasjon.DigitalKontaktinformasjonService;
import no.nav.fo.veilarbperson.consumer.kodeverk.KodeverkService;
import no.nav.fo.veilarbperson.consumer.organisasjonenhet.EnhetService;
import no.nav.fo.veilarbperson.consumer.tps.EgenAnsattService;
import no.nav.fo.veilarbperson.consumer.tps.PersonService;
import no.nav.fo.veilarbperson.services.PepClient;
import no.nav.sbl.dialogarena.common.abac.pep.Pep;
import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v1.OrganisasjonEnhetV1;
import no.nav.tjeneste.virksomhet.person.v3.PersonV3;
import org.springframework.context.annotation.Bean;

public class ServiceConfig {

    private final PersonV3 personV3;
    private final OrganisasjonEnhetV1 organisasjonenhet;
    private final DigitalKontaktinformasjonV1 digitalKontaktinformasjonV1;
    private final EgenAnsattV1 egenAnsattV1;
    private final KodeverkPortType kodeverkPortType;
    private final Pep pep;

    public ServiceConfig(PersonV3 personV3,
                         OrganisasjonEnhetV1 organisasjonenhet,
                         DigitalKontaktinformasjonV1 digitalKontaktinformasjonV1,
                         EgenAnsattV1 egenAnsattV1,
                         KodeverkPortType kodeverkPortType,
                         Pep pep) {
        this.personV3 = personV3;
        this.organisasjonenhet = organisasjonenhet;
        this.digitalKontaktinformasjonV1 = digitalKontaktinformasjonV1;
        this.egenAnsattV1 = egenAnsattV1;
        this.kodeverkPortType = kodeverkPortType;
        this.pep = pep;
    }

    @Bean
    PersonService personService() {
        return new PersonService(personV3);
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

    @Bean
    PepClient pepClient() {
        return new PepClient(pep);
    }
}