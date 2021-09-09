package no.nav.veilarbperson.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.dkif.DkifClient;
import no.nav.veilarbperson.client.dkif.DkifKontaktinfo;
import no.nav.veilarbperson.client.egenansatt.EgenAnsattClient;
import no.nav.veilarbperson.client.pdl.HentPerson;
import no.nav.veilarbperson.client.pdl.PdlClient;
import no.nav.veilarbperson.client.pdl.domain.*;
import no.nav.veilarbperson.client.person.PersonClient;
import no.nav.veilarbperson.client.veilarbportefolje.Personinfo;
import no.nav.veilarbperson.client.veilarbportefolje.VeilarbportefoljeClient;
import no.nav.veilarbperson.domain.*;
import no.nav.veilarbperson.utils.PersonDataMapper;
import no.nav.veilarbperson.utils.PersonV2DataMapper;
import no.nav.veilarbperson.utils.VergeOgFullmaktDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    public HentPerson.Person hentPerson(Fnr personIdent) {
        return pdlClient.hentPerson(personIdent, authService.getInnloggetBrukerToken());
    }

    public PersonData hentPersonDataFraTps(Fnr personIdent) {
        return PersonDataMapper.tilPersonData(personClient.hentPerson(personIdent));
    }

    public PersonV2Data hentFlettetPerson(Fnr fodselsnummer, String userToken) {
        PersonData personDataFraTps = hentPersonDataFraTps(fodselsnummer);
        HentPerson.Person personDataFraPdl = ofNullable(pdlClient.hentPerson(fodselsnummer, userToken))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Fant ikke person i hentPerson operasjonen i PDL"));

        PersonV2Data personV2Data = PersonV2DataMapper.toPersonV2Data(personDataFraPdl, personDataFraTps);

        try {
            flettPersoninfoFraPortefolje(personV2Data, fodselsnummer);
        } catch (Exception e) {
            log.warn("Bruker fallbackløsning for egenAnsatt-sjekk", e);
            personV2Data.setEgenAnsatt(egenAnsattClient.erEgenAnsatt(fodselsnummer));
            personV2Data.setSikkerhetstiltak(ofNullable(getFirstElement(personDataFraPdl.getSikkerhetstiltak())).map(HentPerson.Sikkerhetstiltak::getBeskrivelse).orElse(null));
        }

        flettPartnerOgBarnInformasjon(personDataFraPdl.getSivilstand(), personDataFraPdl.getForelderBarnRelasjon(), personV2Data);
        flettDigitalKontaktinformasjon(fodselsnummer, personV2Data);
        flettGeografiskEnhet(fodselsnummer, userToken, personV2Data);
        flettKodeverk(personV2Data);

        return personV2Data;
    }

    public List<Familiemedlem> hentFamiliemedlemOpplysninger(List<Fnr> familemedlemFnr, Bostedsadresse foreldresBostedsAdresse) {
        List<HentPerson.PersonFraBolk> familiemedlemInfo = pdlClient.hentPersonBolk(familemedlemFnr);

        return ofNullable(familiemedlemInfo)
                .stream()
                .flatMap(Collection::stream)
                .filter(medlemInfo -> medlemInfo.getCode().equals("ok"))
                .map(HentPerson.PersonFraBolk::getPerson)
                .map(familiemedlem -> mapPartnerOgBarnSomFamiliemedlem(familiemedlem, foreldresBostedsAdresse))
                .collect(Collectors.toList());
    }

    public Familiemedlem mapPartnerOgBarnSomFamiliemedlem(HentPerson.Familiemedlem familiemedlem, Bostedsadresse foreldresBostedsAdresse) {
        return PersonV2DataMapper.familiemedlemMapper(
            familiemedlem,
            egenAnsattClient.erEgenAnsatt(
                    ofNullable(getFirstElement(familiemedlem.getFolkeregisteridentifikator()))
                    .map(HentPerson.Folkeregisteridentifikator::getIdentifikasjonsnummer).map(Fnr::of).orElse(null)
            ),
            foreldresBostedsAdresse,
            authService
        );
    }

    public List<Fnr> hentBarnaFnr(List<HentPerson.Familierelasjoner> familierelasjoner) {
        return familierelasjoner.stream()
                .filter(familierelasjon -> "BARN".equals(familierelasjon.getRelatertPersonsRolle()))
                .map(HentPerson.Familierelasjoner::getRelatertPersonsIdent)
                .map(Fnr::of)
                .collect(Collectors.toList());
    }

    public Fnr hentPartnerFnr(List<HentPerson.Sivilstand> personsSivilstand) {
        return ofNullable(getFirstElement(personsSivilstand))
                .map(HentPerson.Sivilstand::getRelatertVedSivilstand).map(Fnr::of).orElse(null);
    }

    public void flettPartnerOgBarnInformasjon(List<HentPerson.Sivilstand> personsSivilstand, List<HentPerson.Familierelasjoner> familierelasjoner, PersonV2Data personV2Data) {
        List<Fnr> familiemedlemFnrListe = new ArrayList<>();

        if (!familierelasjoner.isEmpty()) {
            List<Fnr> barnaFnr = hentBarnaFnr(familierelasjoner);
            if(barnaFnr.size() > 0) {
                familiemedlemFnrListe = barnaFnr;
            }
        }

        if(!personsSivilstand.isEmpty()) {
            Fnr partnerFnr = hentPartnerFnr(personsSivilstand);
            if(partnerFnr != null) {
                familiemedlemFnrListe.add(partnerFnr);
            }
        }

        if (familiemedlemFnrListe.size() > 0) {
            List<Familiemedlem> familiemedlemInfo = hentFamiliemedlemOpplysninger(familiemedlemFnrListe, personV2Data.getBostedsadresse());

            Familiemedlem partnerInfo = familiemedlemInfo
                                            .stream()
                                            .filter(medlem -> hentPartnerFnr(personsSivilstand).equals(medlem.getFodselsnummer()))
                                            .findAny()
                                            .orElse(null);

            personV2Data.setPartner(partnerInfo);

            List<Familiemedlem> barnInfo =  familiemedlemInfo
                                            .stream()
                                            .filter(medlem -> !hentPartnerFnr(personsSivilstand).equals(medlem.getFodselsnummer()))
                                            .collect(Collectors.toList());

            personV2Data.setBarn(barnInfo);
        }
    }

    private void flettPersoninfoFraPortefolje(PersonV2Data personV2Data, Fnr fodselsnummer) {
        Personinfo personinfo = veilarbportefoljeClient.hentPersonInfo(fodselsnummer);
        personV2Data.setEgenAnsatt(personinfo.egenAnsatt);
        personV2Data.setSikkerhetstiltak(personinfo.sikkerhetstiltak);
    }

    private void flettGeografiskEnhet(Fnr fodselsnummer,  String userToken, PersonV2Data personV2Data) {
        String geografiskTilknytning = ofNullable(pdlClient.hentGeografiskTilknytning(fodselsnummer, userToken))
                .map(HentPerson.GeografiskTilknytning::getGtKommune).orElse(null);

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

            leggKrrTelefonNrIListe(kontaktinfo.getMobiltelefonnummer(), kontaktinfo.getMobilSistOppdatert(), personV2Data.getTelefon());
            personV2Data.setEpost(kontaktinfo.getEpostadresse());
            personV2Data.setMalform(kontaktinfo.getSpraak());
        } catch (Exception e) {
            log.warn("Kunne ikke flette digitalkontaktinfo fra KRR", e);
        }
    }

    /* Legger telefonnummer fra PDL og KRR til en liste. Hvis de er like da kan liste inneholde kun en av dem */
    public void leggKrrTelefonNrIListe(String telefonNummerFraKrr, String sistOppdatert, List<Telefon> telefonListe) {
        boolean ikkeKrrTelefonIListe = telefonNummerFraKrr != null
                && telefonListe.stream().noneMatch(t -> telefonNummerFraKrr.equals(t.getTelefonNr()));
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss,000+00:00");
        LocalDateTime dateTime = LocalDateTime.parse(sistOppdatert, dateTimeFormatter);
        String registrertDato = PersonV2DataMapper.formatNorskDato(dateTime);

        if (ikkeKrrTelefonIListe) {
            telefonListe.add(new Telefon()
                    .setPrioritet(telefonListe.size()+1+"")
                    .setTelefonNr(telefonNummerFraKrr)
                    .setRegistrertDato(registrertDato)
                    .setMaster("KRR"));
        }
    }

    public TilrettelagtKommunikasjonData hentSpraakTolkInfo(Fnr fnr, String userToken) {
        HentPerson.HentSpraakTolk spraakTolkInfo = pdlClient.hentTilrettelagtKommunikasjon(fnr, userToken);

        if(spraakTolkInfo.getTilrettelagtKommunikasjon().isEmpty()) {
             throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fant ikke tilrettelagtkommunikasjon til person i PDL");
        }

        HentPerson.TilrettelagtKommunikasjon tilrettelagtKommunikasjon = getFirstElement(spraakTolkInfo.getTilrettelagtKommunikasjon());
        String tegnSpraak = ofNullable(tilrettelagtKommunikasjon).map(HentPerson.TilrettelagtKommunikasjon::getTegnspraaktolk).map(HentPerson.Tolk::getSpraak).map(kodeverkService::getBeskrivelseForSpraakKode).orElse(null);
        String taleSpraak = ofNullable(tilrettelagtKommunikasjon).map(HentPerson.TilrettelagtKommunikasjon::getTalespraaktolk).map(HentPerson.Tolk::getSpraak).map(kodeverkService::getBeskrivelseForSpraakKode).orElse(null);

        return new TilrettelagtKommunikasjonData().setTegnspraak(tegnSpraak).setTalespraak(taleSpraak);
    }

    public VergeOgFullmaktData hentVergeEllerFullmakt(Fnr fnr, String userToken) {
        HentPerson.VergeOgFullmakt vergeOgFullmaktFraPdl = pdlClient.hentVergeOgFullmakt(fnr, userToken);

        if(vergeOgFullmaktFraPdl.getVergemaalEllerFremtidsfullmakt().isEmpty() && vergeOgFullmaktFraPdl.getFullmakt().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Person har ikke verge eller fullmakt i PDL");
        }

        VergeOgFullmaktData vergeOgFullmaktData = toVergeOgFullmaktData(vergeOgFullmaktFraPdl);

        flettMotpartsPersonNavnTilFullmakt(vergeOgFullmaktData, userToken);
        flettBeskrivelseForFullmaktOmraader(vergeOgFullmaktData);

        return vergeOgFullmaktData;
    }

    public void flettBeskrivelseForFullmaktOmraader(VergeOgFullmaktData vergeOgFullmaktData) {
        vergeOgFullmaktData.getFullmakt().forEach(fullmakt ->
            fullmakt.getOmraader().forEach(omraade ->
                    omraade.setBeskrivelse(kodeverkService.getBeskrivelseForTema(omraade.getKode()))
            )
        );
    }

    public void flettMotpartsPersonNavnTilFullmakt(VergeOgFullmaktData vergeOgFullmaktData, String userToken) {
        vergeOgFullmaktData.getFullmakt().forEach(fullmakt -> {
            HentPerson.PersonNavn fullmaktNavn = pdlClient.hentPersonNavn(Fnr.of(fullmakt.getMotpartsPersonident()), userToken);

            if(fullmaktNavn.getNavn().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fant ikke motpartspersonnavn til fullmakt");
            }

            VergeOgFullmaktData.Navn personNavn = VergeOgFullmaktDataMapper.personNavnMapper(fullmaktNavn.getNavn());
            fullmakt.setMotpartsPersonNavn(personNavn);
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

    public PersonNavnV2 hentNavn(Fnr fnr, String userToken) {
        HentPerson.PersonNavn personNavn = pdlClient.hentPersonNavn(fnr, userToken);

        if(personNavn.getNavn().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fant ikke navn til person");
        }

        return PersonV2DataMapper.navnMapper(personNavn.getNavn());
    }
}
