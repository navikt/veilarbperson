package no.nav.fo.veilarbperson;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonFletter.class);

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

    private void flettEgenAnsatt(String fodselsnummer, PersonData personData) {
        personData.setEgenAnsatt(egenAnsattService.erEgenAnsatt(fodselsnummer));
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
        kanskjePoststedBostedsadresse(personData);
        kanskjePoststedMidlertidigAdresseNorge(personData);

        hentLandForAdresser(personData);
    }

    private void kanskjePoststedBostedsadresse(PersonData personData) {
        personData.getPostnummerForBostedsadresse()
                .flatMap(kodeverkManager::getPoststed)
                .ifPresent(personData::setPoststedForBostedsadresse);
    }

    private void kanskjePoststedMidlertidigAdresseNorge(PersonData personData) {
        personData.getPostnummerForMidlertidigAdresseNorge()
                .flatMap(kodeverkManager::getPoststed)
                .ifPresent(personData::setPoststedForMidlertidigAdresseNorge);
    }

    private UstrukturertAdresse withLandForUstrukturertAdresse(UstrukturertAdresse ustrukturertAdresse) {
        String landkode = ustrukturertAdresse.getLandkode();
        return ustrukturertAdresse.withLandkode(kodeverkManager.getBeskrivelseForLandkode(landkode)
            .orElse(landkode));
    }

    private StrukturertAdresse withLandForStrukturertAdresse(StrukturertAdresse strukturertAdresse) {
        String landkode = strukturertAdresse.getLandkode();
        return strukturertAdresse.withLandkode(kodeverkManager.getBeskrivelseForLandkode(landkode)
                .orElse(landkode));
    }

    private void hentLandForAdresser(PersonData personData) {
        if (personData.getMidlertidigAdresseUtland() != null) {
            personData.getMidlertidigAdresseUtland()
                    .withUstrukturertAdresse(withLandForUstrukturertAdresse(personData.getMidlertidigAdresseUtland().getUstrukturertAdresse()));
        }

        if (personData.getPostAdresse() != null) {
            personData.getPostAdresse()
                    .withUstrukturertAdresse(withLandForUstrukturertAdresse(personData.getPostAdresse().getUstrukturertAdresse()));
        }

        if(personData.getBostedsadresse() != null) {
            personData.getBostedsadresse()
                    .withStrukturertAdresse(withLandForStrukturertAdresse(personData.getBostedsadresse().getStrukturertAdresse()));
        }

        if(personData.getMidlertidigAdresseNorge() != null) {
            personData.getMidlertidigAdresseNorge()
                    .withStrukturertAdresse(withLandForStrukturertAdresse(personData.getMidlertidigAdresseNorge().getStrukturertAdresse()));
        }
    }

    private void flettDigitalKontaktinformasjon(String fnr, PersonData personData) {
        try {
            DigitalKontaktinformasjon kontaktinformasjon = digitalKontaktinformasjonService.hentDigitalKontaktinformasjon(fnr);
            personData.setTelefon(kontaktinformasjon.getTelefon());
            personData.setEpost(kontaktinformasjon.getEpost());
        } catch (HentDigitalKontaktinformasjonSikkerhetsbegrensing |
                HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet |
                HentDigitalKontaktinformasjonPersonIkkeFunnet feil) {
            LOGGER.warn("Kunne ikke flette info fra KRR. Fikk feil [{}]", feil.getMessage());
        }
    }

    private void flettSikkerhetstiltak(String fnr, PersonData personData) {
        try {
            Sikkerhetstiltak sikkerhetstiltak = personService.hentSikkerhetstiltak(fnr);
            personData.setSikkerhetstiltak(sikkerhetstiltak.getSikkerhetstiltaksbeskrivelse());
        } catch (HentSikkerhetstiltakPersonIkkeFunnet feil) {
            LOGGER.warn("Kunne ikke flette Sikkerhetstiltak. Fikk feil [{}]", feil.getMessage());
        }
    }

}
