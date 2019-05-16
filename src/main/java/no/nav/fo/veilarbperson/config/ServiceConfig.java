package no.nav.fo.veilarbperson.config;

import no.nav.apiapp.security.veilarbabac.VeilarbAbacPepClient;
import no.nav.brukerdialog.security.oidc.SystemUserTokenProvider;
import no.nav.fo.veilarbperson.consumer.digitalkontaktinformasjon.DigitalKontaktinformasjonService;
import no.nav.fo.veilarbperson.consumer.organisasjonenhet.EnhetService;
import no.nav.fo.veilarbperson.consumer.tps.EgenAnsattService;
import no.nav.fo.veilarbperson.consumer.tps.PersonService;
import no.nav.sbl.dialogarena.common.abac.pep.Pep;
import no.nav.sbl.featuretoggle.unleash.UnleashService;
import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.OrganisasjonEnhetV2;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import org.springframework.context.annotation.Bean;

import javax.inject.Inject;

public class ServiceConfig {

    private final PersonV3 personV3;
    private final OrganisasjonEnhetV2 organisasjonenhet;
    private final DigitalKontaktinformasjonV1 digitalKontaktinformasjonV1;
    private final EgenAnsattV1 egenAnsattV1;
    private final KodeverkPortType kodeverkPortType;
    private final Pep pep;

    public ServiceConfig(PersonV3 personV3,
                         OrganisasjonEnhetV2 organisasjonenhet,
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

    private SystemUserTokenProvider systemUserTokenProvider = new SystemUserTokenProvider();

    @Inject
    UnleashService unleashService;

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
    public VeilarbAbacPepClient pepClient(Pep pep) {

        return VeilarbAbacPepClient.ny()
                .medPep(pep)
                .medResourceTypePerson()
                .medSystemUserTokenProvider(()->systemUserTokenProvider.getToken())
                .brukAktoerId(()->unleashService.isEnabled("veilarbperson.veilarbabac.aktor"))
                .sammenlikneTilgang(()->unleashService.isEnabled("veilarbperson.veilarbabac.sammenlikn"))
                .foretrekkVeilarbAbacResultat(()->unleashService.isEnabled("veilarbperson.veilarbabac.foretrekk_veilarbabac"))
                .bygg();
    }

}