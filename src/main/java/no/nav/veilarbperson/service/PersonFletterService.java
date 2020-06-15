package no.nav.veilarbperson.service;

import no.nav.common.client.norg2.Norg2Client;
import no.nav.veilarbperson.client.digitalkontaktinformasjon.DigitalKontaktinformasjon;
import no.nav.veilarbperson.client.digitalkontaktinformasjon.DigitalKontaktinformasjonService;
import no.nav.veilarbperson.client.kodeverk.KodeverkManager;
import no.nav.veilarbperson.client.kodeverk.KodeverkService;
import no.nav.veilarbperson.client.VeilarbportefoljeClientImpl;
import no.nav.veilarbperson.client.tps.EgenAnsattService;
import no.nav.veilarbperson.client.tps.PersonService;
import no.nav.veilarbperson.domain.Personinfo;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonSikkerhetsbegrensing;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentSikkerhetstiltakPersonIkkeFunnet;
import no.nav.veilarbperson.domain.person.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static no.nav.veilarbperson.utils.Mappers.fraNorg2Enhet;

@Service
public class PersonFletterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonFletterService.class);

    private final PersonService personService;
    private final EgenAnsattService egenAnsattService;
    private final Norg2Client norg2Client;
    private final DigitalKontaktinformasjonService digitalKontaktinformasjonService;
    private final KodeverkManager kodeverkManager;
    private final VeilarbportefoljeClientImpl veilarbportefoljeClientImpl;

    public PersonFletterService(Norg2Client norg2Client,
                                DigitalKontaktinformasjonService digitalKontaktinformasjonService,
                                PersonService personService,
                                EgenAnsattService egenAnsattService,
                                KodeverkService kodeverkService,
                                VeilarbportefoljeClientImpl veilarbportefoljeClientImpl
    ) {
        this.norg2Client = norg2Client;
        this.digitalKontaktinformasjonService = digitalKontaktinformasjonService;
        this.kodeverkManager = new KodeverkManager(kodeverkService);
        this.personService = personService;
        this.egenAnsattService = egenAnsattService;
        this.veilarbportefoljeClientImpl = veilarbportefoljeClientImpl;
    }

    public PersonData hentPerson(String fodselsnummer) throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        PersonData personData = personService.hentPerson(fodselsnummer);

        try {
            flettPersoninfoFraPortefolje(personData, fodselsnummer);
        } catch (Exception e) {
            LOGGER.warn("Bruker fallbackløsning for sikkerhetstiltak og egenAnsatt-sjekk", e);
            flettEgenAnsatt(fodselsnummer, personData);
            flettSikkerhetstiltak(fodselsnummer, personData);
        }

        flettGeografiskEnhet(personData);
        flettDigitalKontaktinformasjon(fodselsnummer, personData);
        flettKodeverk(personData);
        return personData;
    }

    public GeografiskTilknytning hentGeografisktilknytning(String fodselsnummer) throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        return personService.hentGeografiskTilknytning(fodselsnummer);
    }

    private void flettPersoninfoFraPortefolje(PersonData personData, String fodselsnummer) {
        Personinfo personinfo = veilarbportefoljeClientImpl.hentPersonInfo(fodselsnummer);
        personData.setSikkerhetstiltak(personinfo.sikkerhetstiltak);
        personData.setEgenAnsatt(personinfo.egenAnsatt);
    }

    private void flettEgenAnsatt(String fodselsnummer, PersonData personData) {
        personData.setEgenAnsatt(egenAnsattService.erEgenAnsatt(fodselsnummer));
    }

    private void flettGeografiskEnhet(PersonData personData) {
        if (personData.getGeografiskTilknytning() != null) {
            Enhet enhet = fraNorg2Enhet(norg2Client.hentTilhorendeEnhet(personData.getGeografiskTilknytning()));
            personData.setGeografiskEnhet(enhet);
        }
    }

    private void flettKodeverk(PersonData personData) {
        personData.setStatsborgerskap(kodeverkManager.getBeskrivelseForLandkode(personData.getStatsborgerskap()));

        if (personData.getSivilstand() != null) {
            String sivilstandKode = personData.getSivilstand().getSivilstand();
            Sivilstand sivilstand = personData.getSivilstand()
                    .withSivilstand(kodeverkManager.getBeskrivelseForSivilstand(sivilstandKode));
            personData.setSivilstand(sivilstand);
        }
        kanskjePoststedBostedsadresse(personData);
        kanskjePoststedMidlertidigAdresseNorge(personData);

        hentLandForAdresser(personData);
    }

    private void kanskjePoststedBostedsadresse(PersonData personData) {
        personData.getPostnummerForBostedsadresse()
                .map(kodeverkManager::getPoststed)
                .ifPresent(personData::setPoststedForBostedsadresse);
    }

    private void kanskjePoststedMidlertidigAdresseNorge(PersonData personData) {
        personData.getPostnummerForMidlertidigAdresseNorge()
                .map(kodeverkManager::getPoststed)
                .ifPresent(personData::setPoststedForMidlertidigAdresseNorge);
    }

    private UstrukturertAdresse withLandForUstrukturertAdresse(UstrukturertAdresse ustrukturertAdresse) {
        String landkode = ustrukturertAdresse.getLandkode();
        return ustrukturertAdresse.withLandkode(kodeverkManager.getBeskrivelseForLandkode(landkode));
    }

    private StrukturertAdresse withLandForStrukturertAdresse(StrukturertAdresse strukturertAdresse) {
        String landkode = strukturertAdresse.getLandkode();
        return strukturertAdresse.withLandkode(kodeverkManager.getBeskrivelseForLandkode(landkode));
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

        if (personData.getBostedsadresse() != null) {
            personData.getBostedsadresse()
                    .withStrukturertAdresse(withLandForStrukturertAdresse(personData.getBostedsadresse().getStrukturertAdresse()));
        }

        if (personData.getMidlertidigAdresseNorge() != null) {
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
            personData.setSikkerhetstiltak(sikkerhetstiltak.sikkerhetstiltaksbeskrivelse);
        } catch (HentSikkerhetstiltakPersonIkkeFunnet feil) {
            LOGGER.warn("Kunne ikke flette Sikkerhetstiltak. Fikk feil [{}]", feil.getMessage());
        }
    }

}
