package no.nav.veilarbperson.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.dkif.DkifClient;
import no.nav.veilarbperson.client.dkif.DkifKontaktinfo;
import no.nav.veilarbperson.client.egenansatt.EgenAnsattClient;
import no.nav.veilarbperson.client.pdl.HentPdlPerson;
import no.nav.veilarbperson.client.pdl.PdlClient;
import no.nav.veilarbperson.client.pdl.PersonV2Data;
import no.nav.veilarbperson.client.pdl.domain.*;
import no.nav.veilarbperson.client.person.PersonClient;
import no.nav.veilarbperson.client.veilarbportefolje.Personinfo;
import no.nav.veilarbperson.client.veilarbportefolje.VeilarbportefoljeClient;
import no.nav.veilarbperson.domain.Enhet;
import no.nav.veilarbperson.domain.PersonData;
import no.nav.veilarbperson.domain.Telefon;
import no.nav.veilarbperson.utils.PersonDataMapper;
import no.nav.veilarbperson.utils.PersonV2DataMapper;
import no.nav.veilarbperson.utils.VergeOgFullmaktDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static no.nav.veilarbperson.utils.Mappers.fraNorg2Enhet;
import static no.nav.veilarbperson.utils.PersonV2DataMapper.getFirstElement;
import static no.nav.veilarbperson.utils.VergeOgFullmaktDataMapper.toVergeOgFullmaktData;

@Slf4j
@Service
public class PersonV2Service {

    private final PdlClient pdlClient;
    private final AuthService authService;
    private final DkifClient dkifClient;
    private final Norg2Client norg2Client;
    private final PersonClient personClient;
    private final EgenAnsattClient egenAnsattClient;
    private final VeilarbportefoljeClient veilarbportefoljeClient;
    private final KodeverkService kodeverkService;

    @Autowired
    public PersonV2Service(PdlClient pdlClient, AuthService authService, DkifClient dkifClient, Norg2Client norg2Client, PersonClient personClient,
                           EgenAnsattClient egenAnsattClient, VeilarbportefoljeClient veilarbportefoljeClient, KodeverkService kodeverkService) {
        this.pdlClient = pdlClient;
        this.authService = authService;
        this.dkifClient = dkifClient;
        this.norg2Client = norg2Client;
        this.personClient = personClient;
        this.egenAnsattClient = egenAnsattClient;
        this.veilarbportefoljeClient = veilarbportefoljeClient;
        this.kodeverkService = kodeverkService;
    }

    public HentPdlPerson.PdlPerson hentPerson(Fnr personIdent) {
        return pdlClient.hentPerson(personIdent, authService.getInnloggetBrukerToken());
    }

    public PersonData hentPersonDataFraTps(Fnr personIdent) {
        return PersonDataMapper.tilPersonData(personClient.hentPerson(personIdent));
    }

    public PersonV2Data hentFlettetPerson(Fnr fodselsnummer, String userToken) {
        PersonData personDataFraTps = hentPersonDataFraTps(fodselsnummer);
        HentPdlPerson.PdlPerson personDataFraPdl = ofNullable(pdlClient.hentPerson(fodselsnummer, userToken))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Fant ikke person i hentPerson operasjonen i PDL"));

        PersonV2Data personV2Data = PersonV2DataMapper.toPersonV2Data(personDataFraPdl, personDataFraTps);

        try {
            flettPersoninfoFraPortefolje(personV2Data, fodselsnummer);
        } catch (Exception e) {
            log.warn("Bruker fallbackløsning for egenAnsatt-sjekk", e);
            personV2Data.setEgenAnsatt(egenAnsattClient.erEgenAnsatt(fodselsnummer));
            personV2Data.setSikkerhetstiltak(ofNullable(getFirstElement(personDataFraPdl.getSikkerhetstiltak())).map(HentPdlPerson.Sikkerhetstiltak::getBeskrivelse).orElse(null));
        }

        flettBarnInformasjon(personDataFraPdl.getFamilierelasjoner(), personV2Data);
        flettPartnerInformasjon(personDataFraPdl.getSivilstand(), personV2Data, userToken);
        flettDigitalKontaktinformasjon(fodselsnummer, personV2Data);
        flettGeografiskEnhet(fodselsnummer, userToken, personV2Data);
        flettKodeverk(personV2Data);

        return personV2Data;
    }

    public List<Familiemedlem> hentOpplysningerTilBarna(Fnr[] barnasFnrs, Bostedsadresse foreldresBostedsAdresse) {
        List<HentPdlPerson.Barn> barnasInformasjon = pdlClient.hentPersonBolk(barnasFnrs);

        return ofNullable(barnasInformasjon)
                .stream()
                .flatMap(Collection::stream)
                .filter(barn -> barn.getCode().equals("ok"))
                .map(HentPdlPerson.Barn::getPerson)
                .map(barn -> PersonV2DataMapper.familiemedlemMapper(barn, foreldresBostedsAdresse))
                .collect(Collectors.toList());
    }

    public Fnr[] hentFnrTilBarna(List<HentPdlPerson.Familierelasjoner> familierelasjoner) {
        return familierelasjoner.stream()
                .filter(familierelasjon -> "BARN".equals(familierelasjon.getRelatertPersonsRolle()))
                .map(HentPdlPerson.Familierelasjoner::getRelatertPersonsIdent)
                .map(Fnr::of)
                .toArray(Fnr[]::new);
    }

    public void flettBarnInformasjon(List<HentPdlPerson.Familierelasjoner> familierelasjoner, PersonV2Data personV2Data) {
        if (!familierelasjoner.isEmpty()) {
            Fnr[] barnasFnrListe = hentFnrTilBarna(familierelasjoner);

            if (barnasFnrListe.length != 0) {
                List<Familiemedlem> barnasInformasjon = hentOpplysningerTilBarna(barnasFnrListe, personV2Data.getBostedsadresse());
                personV2Data.setBarn(barnasInformasjon);
            }
        }
    }

    public Fnr hentFnrTilPartner(List<HentPdlPerson.Sivilstand> personsSivilstand) {
        return ofNullable(getFirstElement(personsSivilstand))
                .map(HentPdlPerson.Sivilstand::getRelatertVedSivilstand).map(Fnr::of).orElse(null);
    }

    public void flettPartnerInformasjon(List<HentPdlPerson.Sivilstand> personsSivilstand, PersonV2Data personV2Data, String userToken) {
        Fnr fnrTilPartner = hentFnrTilPartner(personsSivilstand);

        if(fnrTilPartner != null) {
            HentPdlPerson.Familiemedlem partnerInformasjon = pdlClient.hentPartner(fnrTilPartner, userToken);
            personV2Data.setPartner(ofNullable(partnerInformasjon)
                                    .map(partner -> PersonV2DataMapper.familiemedlemMapper(partner, personV2Data.getBostedsadresse()))
                                    .orElse(null)
            );
        }
    }

    private void flettPersoninfoFraPortefolje(PersonV2Data personV2Data, Fnr fodselsnummer) {
        Personinfo personinfo = veilarbportefoljeClient.hentPersonInfo(fodselsnummer);
        personV2Data.setEgenAnsatt(personinfo.egenAnsatt);
        personV2Data.setSikkerhetstiltak(personinfo.sikkerhetstiltak);
    }

    private void flettGeografiskEnhet(Fnr fodselsnummer,  String userToken, PersonV2Data personV2Data) {
        String geografiskTilknytning = ofNullable(pdlClient.hentGeografiskTilknytning(fodselsnummer, userToken))
                .map(HentPdlPerson.GeografiskTilknytning::getGtKommune).orElse(null);

        personV2Data.setGeografiskTilknytning(geografiskTilknytning);

        if (geografiskTilknytning != null && geografiskTilknytning.matches("\\d+")) {
            try {
                Enhet enhet = fraNorg2Enhet(norg2Client.hentTilhorendeEnhet(geografiskTilknytning));
                personV2Data.setGeografiskEnhet(enhet);
            } catch (Exception e) {
                log.error("Klarte ikke å flette inn geografisk enhet", e);
            }
        }
    }

    public void flettKodeverk(PersonV2Data personV2Data) {
        Optional<String> postnrIBostedsVegAdr = ofNullable(personV2Data.getBostedsadresse()).map(Bostedsadresse::getVegadresse).map(Bostedsadresse.Vegadresse::getPostnummer);
        Optional<String> postnrIBostedsMatrikkelAdr = ofNullable(personV2Data.getBostedsadresse()).map(Bostedsadresse::getMatrikkeladresse).map(Bostedsadresse.Matrikkeladresse::getPostnummer);
        Optional<String> kommunenrIBostedsVegAdr = ofNullable(personV2Data.getBostedsadresse()).map(Bostedsadresse::getVegadresse).map(Bostedsadresse.Vegadresse::getKommunenummer);
        Optional<String> kommunenrIBostedsMatrikkelAdr = ofNullable(personV2Data.getBostedsadresse()).map(Bostedsadresse::getMatrikkeladresse).map(Bostedsadresse.Matrikkeladresse::getKommunenummer);
        Optional<String> kommunenrIBostedsUkjentAdr = ofNullable(personV2Data.getBostedsadresse()).map(Bostedsadresse::getUkjentBosted).map(Bostedsadresse.UkjentBosted::getBostedskommune);
        Optional<String> kommunenrIOppholdsVegAdr = ofNullable(personV2Data.getOppholdsadresse()).map(Oppholdsadresse::getVegadresse).map(Oppholdsadresse.Vegadresse::getKommunenummer);
        Optional<String> kommunenrIOppholdsMatrikkelAdr = ofNullable(personV2Data.getOppholdsadresse()).map(Oppholdsadresse::getMatrikkeladresse).map(Oppholdsadresse.Matrikkeladresse::getKommunenummer);
        Optional<String> landkodeIBostedsUtenlandskAdr = ofNullable(personV2Data.getBostedsadresse()).map(Bostedsadresse::getUtenlandskAdresse).map(Bostedsadresse.Utenlandskadresse::getLandkode);
        Optional<String> postnrIOppholdsVegAdr = ofNullable(personV2Data.getOppholdsadresse()).map(Oppholdsadresse::getVegadresse).map(Oppholdsadresse.Vegadresse::getPostnummer);
        Optional<String> postnrIOppholdsMatrikkelAdr = ofNullable(personV2Data.getOppholdsadresse()).map(Oppholdsadresse::getMatrikkeladresse).map(Oppholdsadresse.Matrikkeladresse::getPostnummer);
        Optional<String> landkodeIOppholdsUtenlandskAdr = ofNullable(personV2Data.getBostedsadresse()).map(Bostedsadresse::getUtenlandskAdresse).map(Bostedsadresse.Utenlandskadresse::getLandkode);

        postnrIBostedsVegAdr.map(kodeverkService::getPoststedForPostnummer).ifPresent(personV2Data::setPoststedIBostedsVegadresse);
        postnrIBostedsMatrikkelAdr.map(kodeverkService::getPoststedForPostnummer).ifPresent(personV2Data::setPoststedIBostedsMatrikkeladresse);
        kommunenrIBostedsVegAdr.map(kodeverkService::getBeskrivelseForKommunenummer).ifPresent(personV2Data::setKommuneIBostedsVegadresse);
        kommunenrIBostedsMatrikkelAdr.map(kodeverkService::getBeskrivelseForKommunenummer).ifPresent(personV2Data::setKommuneIBostedsMatrikkeladresse);
        kommunenrIBostedsUkjentAdr.map(kodeverkService::getBeskrivelseForKommunenummer).ifPresent(personV2Data::setKommuneIBostedsUkjentadresse);
        kommunenrIOppholdsVegAdr.map(kodeverkService::getBeskrivelseForKommunenummer).ifPresent(personV2Data::setKommuneIOppholdssVegadresse);
        kommunenrIOppholdsMatrikkelAdr.map(kodeverkService::getBeskrivelseForKommunenummer).ifPresent(personV2Data::setKommuneIOppholdsMatrikkeladresse);
        postnrIOppholdsVegAdr.map(kodeverkService::getPoststedForPostnummer).ifPresent(personV2Data::setPoststedIOppholdsVegadresse);
        postnrIOppholdsMatrikkelAdr.map(kodeverkService::getPoststedForPostnummer).ifPresent(personV2Data::setPoststedIOppholdsMatrikkeladresse);
        landkodeIBostedsUtenlandskAdr.map(kodeverkService::getBeskrivelseForLandkode).ifPresent(personV2Data::setLandkodeIBostedsUtenlandskadresse);
        landkodeIOppholdsUtenlandskAdr.map(kodeverkService::getBeskrivelseForLandkode).ifPresent(personV2Data::setLandkodeIOppholdsUtenlandskadresse);
        ofNullable(personV2Data.getStatsborgerskap()).map(kodeverkService::getBeskrivelseForLandkode).ifPresent(personV2Data::setStatsborgerskap);

        List<Kontaktadresse> kontaktadresseList = personV2Data.getKontaktadresser();

        for (Kontaktadresse kontaktadresse: kontaktadresseList) {
                Optional<String> postnrIKontaktsVegAdr = ofNullable(kontaktadresse).map(Kontaktadresse::getVegadresse).map(Kontaktadresse.Vegadresse::getPostnummer);
                Optional<String> postnrIKontaktsPostboksAdr = ofNullable(kontaktadresse).map(Kontaktadresse::getPostboksadresse).map(Kontaktadresse.Postboksadresse::getPostnummer);
                Optional<String> postnrIPostAdresseIFrittFormat = ofNullable(kontaktadresse).map(Kontaktadresse::getPostadresseIFrittFormat).map(Kontaktadresse.PostadresseIFrittFormat::getPostnummer);
                Optional<String> landkodeIKontaktsUtenlandskAdr = ofNullable(kontaktadresse).map(Kontaktadresse::getUtenlandskAdresse).map(Kontaktadresse.Utenlandskadresse::getLandkode);
                Optional<String> landkodeIUtenlandskAdresseIFrittFormat = ofNullable(kontaktadresse).map(Kontaktadresse::getUtenlandskAdresseIFrittFormat).map(Kontaktadresse.UtenlandskAdresseIFrittFormat::getLandkode);
                Optional<String> kommunenrIKontaktsVegAdr = ofNullable(kontaktadresse).map(Kontaktadresse::getVegadresse).map(Kontaktadresse.Vegadresse::getKommunenummer);

                postnrIKontaktsVegAdr.map(kodeverkService::getPoststedForPostnummer).ifPresent(poststed -> kontaktadresse.getVegadresse().setPoststed(poststed));
                kommunenrIKontaktsVegAdr.map(kodeverkService::getBeskrivelseForKommunenummer).ifPresent(kommune -> kontaktadresse.getVegadresse().setKommune(kommune));
                postnrIKontaktsPostboksAdr.map(kodeverkService::getPoststedForPostnummer).ifPresent(poststed -> kontaktadresse.getPostboksadresse().setPoststed(poststed));
                postnrIPostAdresseIFrittFormat.map(kodeverkService::getPoststedForPostnummer).ifPresent(poststed -> kontaktadresse.getPostadresseIFrittFormat().setPoststed(poststed));
                landkodeIKontaktsUtenlandskAdr.map(kodeverkService::getBeskrivelseForLandkode).ifPresent(landkode -> kontaktadresse.getUtenlandskAdresse().setLandkode(landkode));
                landkodeIUtenlandskAdresseIFrittFormat.map(kodeverkService::getBeskrivelseForLandkode).ifPresent(landkode -> kontaktadresse.getUtenlandskAdresseIFrittFormat().setLandkode(landkode));
        }
    }

    private void flettDigitalKontaktinformasjon(Fnr fnr, PersonV2Data personV2Data) {
        try {
            DkifKontaktinfo kontaktinfo = dkifClient.hentKontaktInfo(fnr);

            leggKrrTelefonNrIListe(kontaktinfo.getMobiltelefonnummer(), personV2Data.getTelefon());
            personV2Data.setEpost(kontaktinfo.getEpostadresse());
            personV2Data.setMalform(kontaktinfo.getSpraak());
        } catch (Exception e) {
            log.warn("Kunne ikke flette digitalkontaktinfo fra KRR", e);
        }
    }

    /* Legger telefonnummer fra PDL og KRR til en liste. Hvis de er like da kan liste inneholde kun en av dem */
    public void leggKrrTelefonNrIListe(String telefonNummerFraKrr, List<Telefon> telefonListe) {
        boolean harIkkeTelefonFraKrr = telefonNummerFraKrr != null
                && telefonListe.stream().noneMatch(t -> telefonNummerFraKrr.equals(t.getTelefonNr()));

        if (harIkkeTelefonFraKrr) {
            Telefon telefonNrFraKrr = new Telefon().setPrioritet(telefonListe.size()+1+"").setTelefonNr(telefonNummerFraKrr).setMaster("KRR");
            telefonListe.add(telefonNrFraKrr);
        }
    }

    public TilrettelagtKommunikasjonData hentSpraakTolkInfo(Fnr fnr, String userToken) {
        HentPdlPerson.HentSpraakTolk spraakTolkInfo = ofNullable(pdlClient.hentTilrettelagtKommunikasjon(fnr, userToken))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Klarte ikke å hente persons tolkinformasjon"));

        HentPdlPerson.TilrettelagtKommunikasjon tilrettelagtKommunikasjon = ofNullable(spraakTolkInfo)
                .map(HentPdlPerson.HentSpraakTolk::getTilrettelagtKommunikasjon)
                .map(PersonV2DataMapper::getFirstElement).orElse(null);
        String tegnSpraak = ofNullable(tilrettelagtKommunikasjon).map(HentPdlPerson.TilrettelagtKommunikasjon::getTegnspraaktolk).map(HentPdlPerson.Tolk::getSpraak).map(kodeverkService::getBeskrivelseForSpraakKode).orElse(null);
        String taleSpraak = ofNullable(tilrettelagtKommunikasjon).map(HentPdlPerson.TilrettelagtKommunikasjon::getTalespraaktolk).map(HentPdlPerson.Tolk::getSpraak).map(kodeverkService::getBeskrivelseForSpraakKode).orElse(null);

        return new TilrettelagtKommunikasjonData().setTegnspraak(tegnSpraak).setTalespraak(taleSpraak);
    }

    public VergeOgFullmaktData hentVergeEllerFullmakt(Fnr fnr, String userToken) {
        HentPdlPerson.VergeOgFullmakt vergeOgFullmaktFraPdl = ofNullable(pdlClient.hentVergeOgFullmakt(fnr, userToken))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error mens å hente Verge og Fullmakt til personen"));

        VergeOgFullmaktData vergeOgFullmaktData = toVergeOgFullmaktData(vergeOgFullmaktFraPdl);
        flettMotpartsPersonNavnTilFullmakt(vergeOgFullmaktData, userToken);

        return vergeOgFullmaktData;
    }

    public void flettMotpartsPersonNavnTilFullmakt(VergeOgFullmaktData vergeOgFullmaktData, String userToken) {
        List<VergeOgFullmaktData.Fullmakt> fullmaktListe = vergeOgFullmaktData.getFullmakt();

        fullmaktListe.forEach(fullmakt -> {
            try {
                 HentPdlPerson.PersonNavn fullmaktNavn = pdlClient.hentPersonNavn(Fnr.of(fullmakt.getMotpartsPersonident()), userToken);
                 VergeOgFullmaktData.Navn personNavn = ofNullable(fullmaktNavn).map(HentPdlPerson.PersonNavn::getNavn).map(PersonV2DataMapper::getFirstElement).map(VergeOgFullmaktDataMapper::personNavnMapper).orElse(null);
                 fullmakt.setMotpartsPersonNavn(personNavn);
             } catch (Exception e) {
                 log.error("Error mens å hente motpartsPersonNavn til fullmakt");
             }
        });
    }

    public String hentMalform(Fnr fnr) {
        try{
            DkifKontaktinfo kontaktinfo = dkifClient.hentKontaktInfo(fnr);
            return kontaktinfo.getSpraak();
        } catch(Exception e) {
            log.warn("Kunne ikke hente malform fra KRR", e);
        }
        return null;
    }
}
