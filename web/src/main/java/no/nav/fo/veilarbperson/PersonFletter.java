package no.nav.fo.veilarbperson;


import no.nav.fo.veilarbperson.consumer.digitalkontaktinformasjon.DigitalKontaktinformasjon;
import no.nav.fo.veilarbperson.consumer.digitalkontaktinformasjon.DigitalKontaktinformasjonService;
import no.nav.fo.veilarbperson.consumer.kodeverk.KodeverkManager;
import no.nav.fo.veilarbperson.consumer.kodeverk.KodeverkService;
import no.nav.fo.veilarbperson.consumer.organisasjonenhet.EnhetService;
import no.nav.fo.veilarbperson.consumer.tps.EgenAnsattService;
import no.nav.fo.veilarbperson.consumer.tps.PersonService;
import no.nav.fo.veilarbperson.domain.person.*;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.*;
import no.nav.tjeneste.virksomhet.person.v2.*;

public class PersonFletter {

    private final PersonService personService;

    private final EgenAnsattService egenAnsattService;

    private final EnhetService enhetService;

    private final DigitalKontaktinformasjonService digitalKontaktinformasjonService;

    private final KodeverkManager kodeverkManager;

    public PersonFletter(EnhetService enhetService,
                         DigitalKontaktinformasjonService digitalKontaktinformasjonService,
                         PersonService personService,
                         EgenAnsattService egenAnsattService,
                         KodeverkService kodeverkService) {

        this.enhetService = enhetService;
        this.digitalKontaktinformasjonService = digitalKontaktinformasjonService;
        this.kodeverkManager = new KodeverkManager(kodeverkService);
        this.personService = personService;
        this.egenAnsattService = egenAnsattService;
    }

    public PersonData hentPerson(String fodselsnummer) throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        PersonData personData = personService.hentPerson(fodselsnummer);

        flettEgenAnsatt(fodselsnummer, personData);
        flettOrganisasjonsenhet(personData);
        flettSikkerhetstiltak(fodselsnummer, personData);
        flettDigitalKontaktinformasjon(fodselsnummer, personData);
        flettKodeverk(personData);

        return personData;
    }

    private void flettEgenAnsatt(String personnummer, PersonData personData) {
        personData.setEgenAnsatt(egenAnsattService.erEgenAnsatt(personnummer));
    }

    private void flettOrganisasjonsenhet(PersonData personData) {
        if (personData.getAnsvarligEnhetsnummer() != null) {
            personData.setBehandlendeEnhet(enhetService.hentBehandlendeEnhet(personData.getAnsvarligEnhetsnummer()));
        }
    }

    private void flettKodeverk(PersonData personData) {
        personData.setStatsborgerskap(kodeverkManager.getBeskrivelseForLandkode(personData.getStatsborgerskap())
                .orElse(personData.getStatsborgerskap()));

        if (personData.getSivilstand() != null) {
            String sivilstandKode = personData.getSivilstand().getSivilstand();
            Sivilstand sivilstand = personData.getSivilstand()
                    .withSivilstand(kodeverkManager.getBeskrivelseForSivilstand(sivilstandKode)
                            .orElse(sivilstandKode));
            personData.setSivilstand(sivilstand);
        }
        kanskjePoststed(personData);
    }

    private void kanskjePoststed(PersonData personData) {
        personData.getPostnummer()
                .flatMap(kodeverkManager::getPoststed)
                .ifPresent(poststed -> personData.setPoststed(poststed));

    }

    private void flettDigitalKontaktinformasjon(String fnr, PersonData personData) {
        try {
            DigitalKontaktinformasjon kontaktinformasjon = digitalKontaktinformasjonService.hentDigitalKontaktinformasjon(fnr);
            personData.setTelefon(kontaktinformasjon.getTelefon());
            personData.setEpost(kontaktinformasjon.getEpost());
        } catch (HentDigitalKontaktinformasjonSikkerhetsbegrensing |
                HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet |
                HentDigitalKontaktinformasjonPersonIkkeFunnet hentDigitalKontaktinformasjonSikkerhetsbegrensing) {
            hentDigitalKontaktinformasjonSikkerhetsbegrensing.printStackTrace();
        }
    }

    private void flettSikkerhetstiltak(String fnr, PersonData personData) {
        try {
            Sikkerhetstiltak sikkerhetstiltak = personService.hentSikkerhetstiltak(fnr);
            personData.setSikkerhetstiltak(sikkerhetstiltak.getSikkerhetstiltaksbeskrivelse());
        } catch (HentSikkerhetstiltakPersonIkkeFunnet hentSikkerhetstiltakPersonIkkeFunnet) {
            hentSikkerhetstiltakPersonIkkeFunnet.printStackTrace();
        }
    }

}
