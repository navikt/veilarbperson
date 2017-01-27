package no.nav.fo.veilarbperson;

import no.nav.fo.veilarbperson.digitalkontaktinformasjon.DigitalKontaktinformasjon;
import no.nav.fo.veilarbperson.digitalkontaktinformasjon.DigitalKontaktinformasjonService;
import no.nav.fo.veilarbperson.domain.*;
import no.nav.fo.veilarbperson.kodeverk.KodeverkManager;
import no.nav.fo.veilarbperson.kodeverk.KodeverkService;
import no.nav.fo.veilarbperson.organisasjonenhet.EnhetService;
import no.nav.fo.veilarbperson.person.EgenAnsattService;
import no.nav.fo.veilarbperson.person.PersonService;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.*;
import no.nav.tjeneste.virksomhet.person.v2.HentSikkerhetstiltakPersonIkkeFunnet;

public class PersonFletter {

    private final PersonService personService;

    private final EgenAnsattService egenAnsattService;

    private final EnhetService enhetService;

    private final DigitalKontaktinformasjonService digitalKontaktinformasjonService;

    private final KodeverkManager kodeverkManager;

    public PersonFletter(EnhetService enhetService, DigitalKontaktinformasjonService digitalKontaktinformasjonService, PersonService personService, EgenAnsattService egenAnsattService, KodeverkService kodeverkService) {

        this.enhetService = enhetService;
        this.digitalKontaktinformasjonService = digitalKontaktinformasjonService;
        this.kodeverkManager = new KodeverkManager(kodeverkService);
        this.personService = personService;
        this.egenAnsattService = egenAnsattService;
    }

    PersonData hentPerson(String fnr) {
        PersonData personData = personService.hentPerson(fnr);
        personData.withEgenAnsatt(egenAnsattService.erEgenAnsatt(fnr));

        if (personData.getAnsvarligEnhetsnummer() != null) {
            personData.withBehandlendeEnhet(enhetService.hentBehandlendeEnhet(personData.getAnsvarligEnhetsnummer()));
        }

        hentPersondata(fnr, personData);
        hentDigitalKontaktinformasjon(fnr, personData);
        hentTermerBasertPaKoder(personData);

        return personData;
    }

    private void hentTermerBasertPaKoder(PersonData personData) {
        personData.withStatsborgerskap(kodeverkManager.getBeskrivelseForLandkode(personData.getStatsborgerskap())
                .orElse(personData.getStatsborgerskap()));

        String sivilstandKode = personData.getSivilstand().getSivilstand();
        Sivilstand sivilstand = personData.getSivilstand()
                .withSivilstand(kodeverkManager.getBeskrivelseForSivilstand(sivilstandKode)
                        .orElse(sivilstandKode));
        personData.withSivilstand(sivilstand);
    }

    private void hentDigitalKontaktinformasjon(String fnr, PersonData personData) {
        try {
            DigitalKontaktinformasjon kontaktinformasjon = digitalKontaktinformasjonService.hentDigitalKontaktinformasjon(fnr);
            personData
                    .withTelefon(kontaktinformasjon.getTelefon())
                    .withEpost(kontaktinformasjon.getEpost());
        } catch (HentDigitalKontaktinformasjonSikkerhetsbegrensing |
                HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet |
                HentDigitalKontaktinformasjonPersonIkkeFunnet hentDigitalKontaktinformasjonSikkerhetsbegrensing) {
            hentDigitalKontaktinformasjonSikkerhetsbegrensing.printStackTrace();
        }
    }

    private void hentPersondata(String fnr, PersonData personData) {
        try {
            Sikkerhetstiltak sikkerhetstiltak = personService.hentSikkerhetstiltak(fnr);
            personData.withSikkerhetstiltak(sikkerhetstiltak.getSikkerhetstiltaksbeskrivelse());
        } catch (HentSikkerhetstiltakPersonIkkeFunnet hentSikkerhetstiltakPersonIkkeFunnet) {
            hentSikkerhetstiltakPersonIkkeFunnet.printStackTrace();
        }
    }

}
