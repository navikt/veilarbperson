package no.nav.veilarbperson.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.featuretoggle.UnleashService;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.difi.DifiCient;
import no.nav.veilarbperson.client.difi.HarLoggetInnRespons;
import no.nav.veilarbperson.client.dkif.DkifClient;
import no.nav.veilarbperson.client.dkif.DkifKontaktinfo;
import no.nav.veilarbperson.client.egenansatt.EgenAnsattClient;
import no.nav.veilarbperson.client.person.PersonClient;
import no.nav.veilarbperson.client.person.domain.StrukturertAdresse;
import no.nav.veilarbperson.client.person.domain.TpsPerson;
import no.nav.veilarbperson.client.person.domain.UstrukturertAdresse;
import no.nav.veilarbperson.client.veilarbportefolje.Personinfo;
import no.nav.veilarbperson.client.veilarbportefolje.VeilarbportefoljeClient;
import no.nav.veilarbperson.domain.Enhet;
import no.nav.veilarbperson.domain.GeografiskTilknytning;
import no.nav.veilarbperson.domain.PersonData;
import no.nav.veilarbperson.utils.PersonDataMapper;
import org.springframework.stereotype.Service;

import static no.nav.veilarbperson.utils.Mappers.fraNorg2Enhet;

@RequiredArgsConstructor
@Slf4j
@Service
public class PersonService {

    private static final String UNLEASH_NIVAA4_DISABLED = "veilarbperson.nivaa4.disabled";

    private final Norg2Client norg2Client;
    private final PersonClient personClient;
    private final EgenAnsattClient egenAnsattClient;
    private final DkifClient dkifClient;
    private final KodeverkService kodeverkService;
    private final VeilarbportefoljeClient veilarbportefoljeClient;
    private final DifiCient difiCient;
    private final UnleashService unleashService;

    public TpsPerson hentPerson(Fnr fodselsnummer){
        return personClient.hentPerson(fodselsnummer);
    }

    public PersonData hentFlettetPerson(Fnr fodselsnummer) {
        PersonData personData = PersonDataMapper.tilPersonData(hentPerson(fodselsnummer));

        try {
            flettPersoninfoFraPortefolje(personData, fodselsnummer);
        } catch (Exception e) {
            log.warn("Bruker fallbackløsning for sikkerhetstiltak og egenAnsatt-sjekk", e);
            flettEgenAnsatt(fodselsnummer, personData);
            flettSikkerhetstiltak(fodselsnummer, personData);
        }

        flettGeografiskEnhet(personData);
        flettDigitalKontaktinformasjon(fodselsnummer, personData);
        flettKodeverk(personData);
        return personData;
    }

    public GeografiskTilknytning hentGeografisktilknytning(Fnr fodselsnummer) {
        String geografiskTilknytning = hentPerson(fodselsnummer).getGeografiskTilknytning();
        return new GeografiskTilknytning(geografiskTilknytning);
    }

    public HarLoggetInnRespons hentHarNivaa4(Fnr fodselsnummer) {
        if (unleashService.isEnabled(UNLEASH_NIVAA4_DISABLED)) {
            return new HarLoggetInnRespons()
                    .setErRegistrertIdPorten(true)
                    .setHarbruktnivaa4(true)
                    .setPersonidentifikator(fodselsnummer);
        }
        return difiCient.harLoggetInnSiste18mnd(fodselsnummer);
    }

    private void flettPersoninfoFraPortefolje(PersonData personData, Fnr fodselsnummer) {
        Personinfo personinfo = veilarbportefoljeClient.hentPersonInfo(fodselsnummer);
        personData.setSikkerhetstiltak(personinfo.sikkerhetstiltak);
        personData.setEgenAnsatt(personinfo.egenAnsatt);
    }

    private void flettEgenAnsatt(Fnr fodselsnummer, PersonData personData) {
        personData.setEgenAnsatt(egenAnsattClient.erEgenAnsatt(fodselsnummer));
    }

    private void flettGeografiskEnhet(PersonData personData) {
        String geografiskTilknytning = personData.getGeografiskTilknytning();

        // Sjekk at geografiskTilknytning er satt og at det er en ISO 3166 kode (for utenlandske brukere så blir landskode brukt istedenfor)
        if (geografiskTilknytning != null && geografiskTilknytning.matches("\\d+")) {
            try {
                Enhet enhet = fraNorg2Enhet(norg2Client.hentTilhorendeEnhet(geografiskTilknytning));
                personData.setGeografiskEnhet(enhet);
            } catch (Exception e) {
                log.error("Klarte ikke å flette inn geografisk enhet", e);
            }
        }
    }

    private void flettKodeverk(PersonData personData) {
        personData.setStatsborgerskap(kodeverkService.getBeskrivelseForLandkode(personData.getStatsborgerskap()));

        if (personData.getSivilstand() != null) {
            String sivilstandKode = personData.getSivilstand().getSivilstand();
            personData.getSivilstand().setSivilstand(kodeverkService.getBeskrivelseForSivilstand(sivilstandKode));
        }

        kanskjePoststedBostedsadresse(personData);
        kanskjePoststedMidlertidigAdresseNorge(personData);

        hentLandForAdresser(personData);
    }

    private void kanskjePoststedBostedsadresse(PersonData personData) {
        personData.getPostnummerForBostedsadresse()
                .map(kodeverkService::getPoststedForPostnummer)
                .ifPresent(personData::setPoststedForBostedsadresse);
    }

    private void kanskjePoststedMidlertidigAdresseNorge(PersonData personData) {
        personData.getPostnummerForMidlertidigAdresseNorge()
                .map(kodeverkService::getPoststedForPostnummer)
                .ifPresent(personData::setPoststedForMidlertidigAdresseNorge);
    }

    private UstrukturertAdresse withLandForUstrukturertAdresse(UstrukturertAdresse ustrukturertAdresse) {
        String landkode = ustrukturertAdresse.getLandkode();
        return ustrukturertAdresse.withLandkode(kodeverkService.getBeskrivelseForLandkode(landkode));
    }

    private StrukturertAdresse withLandForStrukturertAdresse(StrukturertAdresse strukturertAdresse) {
        String landkode = strukturertAdresse.getLandkode();
        return strukturertAdresse.withLandkode(kodeverkService.getBeskrivelseForLandkode(landkode));
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

    private void flettDigitalKontaktinformasjon(Fnr fnr, PersonData personData) {
        try {
            DkifKontaktinfo kontaktinfo = dkifClient.hentKontaktInfo(fnr);
            personData.setTelefon(kontaktinfo.getMobiltelefonnummer());
            personData.setEpost(kontaktinfo.getEpostadresse());
        } catch (Exception e) {
            log.warn("Kunne ikke flette info fra KRR", e);
        }
    }

    private void flettSikkerhetstiltak(Fnr fnr, PersonData personData) {
        try {
            String sikkerhetstiltak = personClient.hentSikkerhetstiltak(fnr);
            personData.setSikkerhetstiltak(sikkerhetstiltak);
        } catch (Exception e) {
            log.warn("Kunne ikke flette Sikkerhetstiltak", e);
        }
    }

}
