package no.nav.veilarbperson.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentSikkerhetstiltakPersonIkkeFunnet;
import no.nav.veilarbperson.client.*;
import no.nav.veilarbperson.domain.DkifKontaktinfo;
import no.nav.veilarbperson.domain.Personinfo;
import no.nav.veilarbperson.domain.person.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static no.nav.veilarbperson.utils.Mappers.fraNorg2Enhet;

@Slf4j
@Service
public class PersonService {

    private final Norg2Client norg2Client;
    private final PersonClient personClient;
    private final EgenAnsattClient egenAnsattClient;
    private final DkifClient dkifClient;
    private final KodeverkClient kodeverkClient;
    private final VeilarbportefoljeClient veilarbportefoljeClient;

    @Autowired
    public PersonService(
            Norg2Client norg2Client, PersonClient personClient, EgenAnsattClient egenAnsattClient,
            DkifClient dkifClient, KodeverkClient kodeverkClient, VeilarbportefoljeClient veilarbportefoljeClient
    ) {
        this.norg2Client = norg2Client;
        this.personClient = personClient;
        this.egenAnsattClient = egenAnsattClient;
        this.dkifClient = dkifClient;
        this.kodeverkClient = kodeverkClient;
        this.veilarbportefoljeClient = veilarbportefoljeClient;
    }


    public PersonData hentPerson(String fodselsnummer) {
        PersonData personData = personClient.hentPersonData(fodselsnummer);

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

    public GeografiskTilknytning hentGeografisktilknytning(String fodselsnummer) {
        return personClient.hentGeografiskTilknytning(fodselsnummer);
    }

    private void flettPersoninfoFraPortefolje(PersonData personData, String fodselsnummer) {
        Personinfo personinfo = veilarbportefoljeClient.hentPersonInfo(fodselsnummer);
        personData.setSikkerhetstiltak(personinfo.sikkerhetstiltak);
        personData.setEgenAnsatt(personinfo.egenAnsatt);
    }

    private void flettEgenAnsatt(String fodselsnummer, PersonData personData) {
        personData.setEgenAnsatt(egenAnsattClient.erEgenAnsatt(fodselsnummer));
    }

    private void flettGeografiskEnhet(PersonData personData) {
        if (personData.getGeografiskTilknytning() != null) {
            Enhet enhet = fraNorg2Enhet(norg2Client.hentTilhorendeEnhet(personData.getGeografiskTilknytning()));
            personData.setGeografiskEnhet(enhet);
        }
    }

    private void flettKodeverk(PersonData personData) {
        personData.setStatsborgerskap(kodeverkClient.getBeskrivelseForLandkode(personData.getStatsborgerskap()));

        if (personData.getSivilstand() != null) {
            String sivilstandKode = personData.getSivilstand().getSivilstand();
            Sivilstand sivilstand = personData.getSivilstand()
                    .withSivilstand(kodeverkClient.getBeskrivelseForSivilstand(sivilstandKode));
            personData.setSivilstand(sivilstand);
        }
        kanskjePoststedBostedsadresse(personData);
        kanskjePoststedMidlertidigAdresseNorge(personData);

        hentLandForAdresser(personData);
    }

    private void kanskjePoststedBostedsadresse(PersonData personData) {
        personData.getPostnummerForBostedsadresse()
                .map(kodeverkClient::getPoststed)
                .ifPresent(personData::setPoststedForBostedsadresse);
    }

    private void kanskjePoststedMidlertidigAdresseNorge(PersonData personData) {
        personData.getPostnummerForMidlertidigAdresseNorge()
                .map(kodeverkClient::getPoststed)
                .ifPresent(personData::setPoststedForMidlertidigAdresseNorge);
    }

    private UstrukturertAdresse withLandForUstrukturertAdresse(UstrukturertAdresse ustrukturertAdresse) {
        String landkode = ustrukturertAdresse.getLandkode();
        return ustrukturertAdresse.withLandkode(kodeverkClient.getBeskrivelseForLandkode(landkode));
    }

    private StrukturertAdresse withLandForStrukturertAdresse(StrukturertAdresse strukturertAdresse) {
        String landkode = strukturertAdresse.getLandkode();
        return strukturertAdresse.withLandkode(kodeverkClient.getBeskrivelseForLandkode(landkode));
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
            DkifKontaktinfo kontaktinfo = dkifClient.hentKontaktInfo(fnr);
            personData.setTelefon(kontaktinfo.getMobiltelefonnummer());
            personData.setEpost(kontaktinfo.getEpostadresse());
        } catch (Exception e) {
            log.warn("Kunne ikke flette info fra KRR", e);
        }
    }

    private void flettSikkerhetstiltak(String fnr, PersonData personData) {
        try {
            Sikkerhetstiltak sikkerhetstiltak = personClient.hentSikkerhetstiltak(fnr);
            personData.setSikkerhetstiltak(sikkerhetstiltak.sikkerhetstiltaksbeskrivelse);
        } catch (Exception e) {
            log.warn("Kunne ikke flette Sikkerhetstiltak", e);
        }
    }

}
