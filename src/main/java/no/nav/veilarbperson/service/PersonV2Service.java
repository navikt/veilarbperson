package no.nav.veilarbperson.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.featuretoggle.UnleashClient;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.digdir.HarLoggetInnRespons;
import no.nav.veilarbperson.client.digdir.DigdirClient;
import no.nav.veilarbperson.client.digdir.DigdirKontaktinfo;
import no.nav.veilarbperson.client.nom.SkjermetClient;
import no.nav.veilarbperson.client.pdl.HentPerson;
import no.nav.veilarbperson.client.pdl.PdlClient;
import no.nav.veilarbperson.client.pdl.domain.*;
import no.nav.veilarbperson.client.person.PersonClient;
import no.nav.veilarbperson.client.person.PersonDataMapper;
import no.nav.veilarbperson.domain.*;
import no.nav.veilarbperson.utils.PersonV2DataMapper;
import no.nav.veilarbperson.utils.VergeOgFullmaktDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static no.nav.veilarbperson.client.pdl.domain.RelasjonsBosted.UKJENT_BOSTED;
import static no.nav.veilarbperson.client.person.Mappers.fraNorg2Enhet;
import static no.nav.veilarbperson.utils.PersonV2DataMapper.getFirstElement;
import static no.nav.veilarbperson.utils.PersonV2DataMapper.parseZonedDateToDateString;
import static no.nav.veilarbperson.utils.PersonV2DataMapper.sivilstandMapper;
import static no.nav.veilarbperson.utils.VergeOgFullmaktDataMapper.toVergeOgFullmaktData;

@Slf4j
@Service
public class PersonV2Service {
    private static final String UNLEASH_NIVAA4_DISABLED = "veilarbperson.nivaa4.disabled";
    private final PdlClient pdlClient;
    private final AuthService authService;
    private final DigdirClient digdirClient;
    private final Norg2Client norg2Client;
    private final PersonClient personClient;
    private final SkjermetClient skjermetClient;
    private final KodeverkService kodeverkService;
    private final UnleashClient unleashClient;

    @Autowired
    public PersonV2Service(PdlClient pdlClient,
                           AuthService authService,
                           DigdirClient digdirClient,
                           Norg2Client norg2Client,
                           PersonClient personClient,
                           UnleashClient unleashClient,
                           SkjermetClient skjermetClient,
                           KodeverkService kodeverkService) {
        this.pdlClient = pdlClient;
        this.authService = authService;
        this.digdirClient = digdirClient;
        this.norg2Client = norg2Client;
        this.personClient = personClient;
        this.skjermetClient = skjermetClient;
        this.kodeverkService = kodeverkService;
        this.unleashClient = unleashClient;
    }

    public HentPerson.Person hentPerson(Fnr personIdent) {
        return pdlClient.hentPerson(personIdent);
    }

    public PersonDataTPS hentPersonDataFraTps(Fnr personIdent) {
        return PersonDataMapper.tilPersonDataTPS(personClient.hentPerson(personIdent));
    }

    public PersonV2Data hentFlettetPerson(Fnr fodselsnummer) {
        HentPerson.Person personDataFraPdl = ofNullable(pdlClient.hentPerson(fodselsnummer))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Fant ikke person i hentPerson operasjonen i PDL"));

        PersonV2Data personV2Data = PersonV2DataMapper.toPersonV2Data(personDataFraPdl);
        flettInnKontonummer(personV2Data);

        flettInnEgenAnsatt(personV2Data, fodselsnummer);
        flettBarn(personDataFraPdl.getForelderBarnRelasjon(), personV2Data);
        flettSivilstand(personDataFraPdl.getSivilstand(), personV2Data);
        flettDigitalKontaktinformasjon(fodselsnummer, personV2Data);
        flettGeografiskEnhet(fodselsnummer, personV2Data);
        flettKodeverk(personV2Data);

        return personV2Data;
    }

    public void flettInnKontonummer(PersonV2Data person) {
        PersonDataTPS personDataTPSFraTps = hentPersonDataFraTps(person.getFodselsnummer());
        person.setKontonummer(personDataTPSFraTps.getKontonummer());
    }

    public List<Familiemedlem> hentFamiliemedlemOpplysninger(List<Fnr> familemedlemFnr, Bostedsadresse bostedsadresse) {
        List<HentPerson.PersonFraBolk> familiemedlemInfo = pdlClient.hentPersonBolk(familemedlemFnr);

        return familiemedlemInfo
                .stream()
                .filter(medlemInfo -> medlemInfo.getCode().equals("ok"))
                .map(HentPerson.PersonFraBolk::getPerson)
                .filter(PersonV2DataMapper::harGyldigIdent)
                .map(familiemedlem -> mapFamiliemedlem(familiemedlem, bostedsadresse))
                .collect(Collectors.toList());
    }

    private boolean erSkjermet(Fnr fnr) {
        return skjermetClient.hentSkjermet(fnr);
    }

    public Familiemedlem mapFamiliemedlem(HentPerson.Familiemedlem familiemedlem, Bostedsadresse bostedsadresse) {
        Fnr familiemedlemFnr = PersonV2DataMapper.hentFamiliemedlemFnr(familiemedlem);

        return PersonV2DataMapper.familiemedlemMapper(
                familiemedlem,
                erSkjermet(familiemedlemFnr),
                bostedsadresse,
                authService
        );
    }

    private Familiemedlem mapFamiliemedlemUtenFnr(HentPerson.RelatertPersonUtenFolkeregisteridentifikator personUtenFnr) {
        return new Familiemedlem()
                .setFornavn(personUtenFnr.getNavn().getFornavn())
                .setFodselsdato(personUtenFnr.getFoedselsdato())
                .setDodsdato(null)
                .setErEgenAnsatt(false)
                .setHarVeilederTilgang(true)
                .setRelasjonsBosted(UKJENT_BOSTED);
    }

    public List<Fnr> hentBarnaFnr(List<HentPerson.ForelderBarnRelasjon> familierelasjoner) {
        return familierelasjoner.stream()
                .filter(familierelasjon -> "BARN".equals(familierelasjon.getRelatertPersonsRolle()))
                .map(HentPerson.ForelderBarnRelasjon::getRelatertPersonsIdent)
                .filter(Objects::nonNull)
                .map(Fnr::of)
                .collect(Collectors.toList());
    }

    public void flettBarn(List<HentPerson.ForelderBarnRelasjon> forelderBarnRelasjoner, PersonV2Data personV2Data) {
        List<Fnr> barnFnrListe = hentBarnaFnr(forelderBarnRelasjoner);
        List<Familiemedlem> barnInfo = hentFamiliemedlemOpplysninger(barnFnrListe, personV2Data.getBostedsadresse());
        List<Familiemedlem> barnUtenFnrInfo = hentBarnUtenFnr(forelderBarnRelasjoner);
        barnInfo.addAll(barnUtenFnrInfo);

        personV2Data.setBarn(barnInfo);
    }

    private List<Familiemedlem> hentBarnUtenFnr(List<HentPerson.ForelderBarnRelasjon> forelderBarnRelasjoner) {
        return forelderBarnRelasjoner.stream()
                .filter(familierelasjon -> "BARN".equals(familierelasjon.getRelatertPersonsRolle()))
                .filter(barn -> barn.getRelatertPersonsIdent() == null)
                .map(HentPerson.ForelderBarnRelasjon::getRelatertPersonUtenFolkeregisteridentifikator)
                .filter(Objects::nonNull)
                .map(this::mapFamiliemedlemUtenFnr)
                .collect(Collectors.toList());
    }

    public void flettSivilstand(List<HentPerson.Sivilstand> sivilstands, PersonV2Data personV2Data) {
        List<Sivilstand> mappetSivilstand = sivilstands.stream().flatMap(sivilstand -> {
            Optional<Familiemedlem> relatert = Optional.ofNullable(sivilstand.getRelatertVedSivilstand())
                    .map(Fnr::of)
                    .map(fnr -> hentFamiliemedlemOpplysninger(List.of(fnr), personV2Data.getBostedsadresse()))
                    .flatMap(list -> list.stream().findFirst());
            return Stream.of(sivilstandMapper(sivilstand, relatert));
        }).collect(Collectors.toList());

        personV2Data.setSivilstandliste(mappetSivilstand);
    }

    private void flettInnEgenAnsatt(PersonV2Data personV2Data, Fnr fodselsnummer) {
        Boolean egenAnsatt = skjermetClient.hentSkjermet(fodselsnummer);
        personV2Data.setEgenAnsatt(egenAnsatt);
    }

    public GeografiskTilknytning hentGeografiskTilknytning(Fnr fnr) {
        HentPerson.GeografiskTilknytning geografiskTilknytning = pdlClient.hentGeografiskTilknytning(fnr);

        if (geografiskTilknytning == null) {
            return null;
        }

        switch (geografiskTilknytning.getGtType()) {
            case "KOMMUNE":
                return new GeografiskTilknytning(geografiskTilknytning.getGtKommune());
            case "BYDEL":
                return new GeografiskTilknytning(geografiskTilknytning.getGtBydel());
            case "UTLAND":
                return new GeografiskTilknytning(geografiskTilknytning.getGtLand());
            default:  // type == UDEFINERT
                return null;
        }
    }

    private void flettGeografiskEnhet(Fnr fnr, PersonV2Data personV2Data) {
        String geografiskTilknytning = Optional.ofNullable(hentGeografiskTilknytning(fnr))
                .map(GeografiskTilknytning::getGeografiskTilknytning)
                .orElse(null);

        personV2Data.setGeografiskTilknytning(geografiskTilknytning);

        // Sjekk at geografiskTilknytning er satt og at det ikke er en tre-bokstavs landkode (ISO 3166 Alpha-3, for utenlandske brukere så blir landskode brukt istedenfor nummer)
        if (geografiskTilknytning != null && geografiskTilknytning.matches("\\d+")) {
            try {
                // Henter geografisk enhet, derfor settes ikke diskresjonskode og skjermet
                Enhet enhet = fraNorg2Enhet(norg2Client.hentTilhorendeEnhet(geografiskTilknytning, null, false));
                personV2Data.setGeografiskEnhet(enhet);
            } catch (Exception e) {
                log.error("Klarte ikke å flette inn geografisk enhet", e);
            }
        }
    }

    public void flettKodeverk(PersonV2Data personV2Data) {
        Optional<String> postnrIBostedsVegAdr = ofNullable(personV2Data.getBostedsadresse()).map(Bostedsadresse::getVegadresse).map(
                Bostedsadresse.Vegadresse::getPostnummer);
        Optional<String> postnrIBostedsMatrikkelAdr = ofNullable(personV2Data.getBostedsadresse()).map(Bostedsadresse::getMatrikkeladresse).map(
                Bostedsadresse.Matrikkeladresse::getPostnummer);
        Optional<String> kommunenrIBostedsVegAdr = ofNullable(personV2Data.getBostedsadresse()).map(Bostedsadresse::getVegadresse).map(
                Bostedsadresse.Vegadresse::getKommunenummer);
        Optional<String> kommunenrIBostedsMatrikkelAdr = ofNullable(personV2Data.getBostedsadresse()).map(Bostedsadresse::getMatrikkeladresse).map(
                Bostedsadresse.Matrikkeladresse::getKommunenummer);
        Optional<String> kommunenrIBostedsUkjentAdr = ofNullable(personV2Data.getBostedsadresse()).map(Bostedsadresse::getUkjentBosted).map(
                Bostedsadresse.UkjentBosted::getBostedskommune);
        Optional<String> kommunenrIOppholdsVegAdr = ofNullable(personV2Data.getOppholdsadresse()).map(Oppholdsadresse::getVegadresse).map(
                Oppholdsadresse.Vegadresse::getKommunenummer);
        Optional<String> kommunenrIOppholdsMatrikkelAdr = ofNullable(personV2Data.getOppholdsadresse()).map(
                Oppholdsadresse::getMatrikkeladresse).map(Oppholdsadresse.Matrikkeladresse::getKommunenummer);
        Optional<String> landkodeIBostedsUtenlandskAdr = ofNullable(personV2Data.getBostedsadresse()).map(Bostedsadresse::getUtenlandskAdresse).map(
                Bostedsadresse.Utenlandskadresse::getLandkode);
        Optional<String> postnrIOppholdsVegAdr = ofNullable(personV2Data.getOppholdsadresse()).map(Oppholdsadresse::getVegadresse).map(
                Oppholdsadresse.Vegadresse::getPostnummer);
        Optional<String> postnrIOppholdsMatrikkelAdr = ofNullable(personV2Data.getOppholdsadresse()).map(Oppholdsadresse::getMatrikkeladresse).map(
                Oppholdsadresse.Matrikkeladresse::getPostnummer);
        Optional<String> landkodeIOppholdsUtenlandskAdr = ofNullable(personV2Data.getOppholdsadresse()).map(
                Oppholdsadresse::getUtenlandskAdresse).map(Oppholdsadresse.Utenlandskadresse::getLandkode);

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
        personV2Data.setStatsborgerskap(personV2Data.getStatsborgerskapKoder()
                .stream()
                .map(kodeverkService::getBeskrivelseForLandkode)
                .filter(Objects::nonNull)
                .toList());

        List<Kontaktadresse> kontaktadresseList = personV2Data.getKontaktadresser();

        for (Kontaktadresse kontaktadresse : kontaktadresseList) {
            Optional<String> postnrIKontaktsVegAdr = ofNullable(kontaktadresse).map(Kontaktadresse::getVegadresse).map(
                    Kontaktadresse.Vegadresse::getPostnummer);
            Optional<String> postnrIKontaktsPostboksAdr = ofNullable(kontaktadresse).map(Kontaktadresse::getPostboksadresse).map(
                    Kontaktadresse.Postboksadresse::getPostnummer);
            Optional<String> postnrIPostAdresseIFrittFormat = ofNullable(kontaktadresse).map(Kontaktadresse::getPostadresseIFrittFormat).map(
                    Kontaktadresse.PostadresseIFrittFormat::getPostnummer);
            Optional<String> landkodeIKontaktsUtenlandskAdr = ofNullable(kontaktadresse).map(Kontaktadresse::getUtenlandskAdresse).map(
                    Kontaktadresse.Utenlandskadresse::getLandkode);
            Optional<String> landkodeIUtenlandskAdresseIFrittFormat = ofNullable(kontaktadresse).map(Kontaktadresse::getUtenlandskAdresseIFrittFormat).map(
                    Kontaktadresse.UtenlandskAdresseIFrittFormat::getLandkode);
            Optional<String> kommunenrIKontaktsVegAdr = ofNullable(kontaktadresse).map(Kontaktadresse::getVegadresse).map(
                    Kontaktadresse.Vegadresse::getKommunenummer);

            postnrIKontaktsVegAdr.map(kodeverkService::getPoststedForPostnummer).ifPresent(poststed -> kontaktadresse.getVegadresse().setPoststed(
                    poststed));
            kommunenrIKontaktsVegAdr.map(kodeverkService::getBeskrivelseForKommunenummer).ifPresent(kommune -> kontaktadresse.getVegadresse().setKommune(
                    kommune));
            postnrIKontaktsPostboksAdr.map(kodeverkService::getPoststedForPostnummer).ifPresent(poststed -> kontaktadresse.getPostboksadresse().setPoststed(
                    poststed));
            postnrIPostAdresseIFrittFormat.map(kodeverkService::getPoststedForPostnummer).ifPresent(poststed -> kontaktadresse.getPostadresseIFrittFormat().setPoststed(
                    poststed));
            landkodeIKontaktsUtenlandskAdr.map(kodeverkService::getBeskrivelseForLandkode).ifPresent(landkode -> kontaktadresse.getUtenlandskAdresse().setLandkode(
                    landkode));
            landkodeIUtenlandskAdresseIFrittFormat.map(kodeverkService::getBeskrivelseForLandkode).ifPresent(landkode -> kontaktadresse.getUtenlandskAdresseIFrittFormat().setLandkode(
                    landkode));
        }
    }

    private void flettDigitalKontaktinformasjon(Fnr fnr, PersonV2Data personV2Data) {
        try {
            DigdirKontaktinfo kontaktinfo = digdirClient.hentKontaktInfo(fnr);
            String epostSisteOppdatert = parseZonedDateToDateString(kontaktinfo.getEpostadresseOppdatert());
            String mobilSisteOppdatert = parseZonedDateToDateString(kontaktinfo.getMobiltelefonnummerOppdatert());
            Epost epost = kontaktinfo.getEpostadresse() != null
                    ? new Epost().setEpostAdresse(kontaktinfo.getEpostadresse()).setEpostSistOppdatert(epostSisteOppdatert).setMaster("KRR")
                    : null;

            personV2Data.setEpost(epost);
            personV2Data.setMalform(kontaktinfo.getSpraak());
            leggKrrTelefonNrIListe(kontaktinfo.getMobiltelefonnummer(), mobilSisteOppdatert, personV2Data.getTelefon());
        } catch (Exception e) {
            log.warn("Kunne ikke flette digitalkontaktinfo fra KRR", e);
        }
    }

    /* Legger telefonnummer fra PDL og KRR til en liste. Hvis de er like da kan liste inneholde kun en av dem */
    public void leggKrrTelefonNrIListe(String telefonNummerFraKrr, String sistOppdatert, List<Telefon> telefonListe) {
        boolean ikkeKrrTelefonIListe = telefonNummerFraKrr != null
                && telefonListe.stream().noneMatch(t -> telefonNummerFraKrr.equals(t.getTelefonNr()));
        if (ikkeKrrTelefonIListe) {
            telefonListe.add(new Telefon()
                    .setPrioritet(telefonListe.size() + 1 + "")
                    .setTelefonNr(telefonNummerFraKrr)
                    .setRegistrertDato(sistOppdatert)
                    .setMaster("KRR"));
        }
    }

    public TilrettelagtKommunikasjonData hentSpraakTolkInfo(Fnr fnr) {
        HentPerson.HentSpraakTolk spraakTolkInfo = pdlClient.hentTilrettelagtKommunikasjon(fnr);

        if (spraakTolkInfo.getTilrettelagtKommunikasjon().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT,
                    "Ingen tilrettelagtkommunikasjon for person i PDL");
        }

        HentPerson.TilrettelagtKommunikasjon tilrettelagtKommunikasjon = getFirstElement(spraakTolkInfo.getTilrettelagtKommunikasjon());
        String tegnSpraak = ofNullable(tilrettelagtKommunikasjon)
                .map(HentPerson.TilrettelagtKommunikasjon::getTegnspraaktolk)
                .map(HentPerson.Tolk::getSpraak)
                .map(kodeverkService::getBeskrivelseForSpraakKode).orElse(null);
        String taleSpraak = ofNullable(tilrettelagtKommunikasjon)
                .map(HentPerson.TilrettelagtKommunikasjon::getTalespraaktolk)
                .map(HentPerson.Tolk::getSpraak)
                .map(kodeverkService::getBeskrivelseForSpraakKode).orElse(null);

        return new TilrettelagtKommunikasjonData().setTegnspraak(tegnSpraak).setTalespraak(taleSpraak);
    }

    public VergeOgFullmaktData hentVergeEllerFullmakt(Fnr fnr) {
        HentPerson.VergeOgFullmakt vergeOgFullmaktFraPdl = pdlClient.hentVergeOgFullmakt(fnr);

        if (vergeOgFullmaktFraPdl.getVergemaalEllerFremtidsfullmakt().isEmpty() && vergeOgFullmaktFraPdl.getFullmakt().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "Person har ikke verge eller fullmakt i PDL");
        }

        VergeOgFullmaktData vergeOgFullmaktData = toVergeOgFullmaktData(vergeOgFullmaktFraPdl);

        flettMotpartsPersonNavnTilFullmakt(vergeOgFullmaktData);
        flettBeskrivelseForFullmaktOmraader(vergeOgFullmaktData);

        return vergeOgFullmaktData;
    }

    public void flettBeskrivelseForFullmaktOmraader(VergeOgFullmaktData vergeOgFullmaktData) {
        vergeOgFullmaktData.getFullmakt().forEach(fullmakt -> {
                    if (!fullmakt.getOmraader().isEmpty() && fullmakt.getOmraader().get(0).getKode().equals("*")) {
                        fullmakt.getOmraader().get(0).setBeskrivelse("alle ytelser");
                    } else {
                        fullmakt.getOmraader().forEach(omraade ->
                                omraade.setBeskrivelse(kodeverkService.getBeskrivelseForTema(omraade.getKode()))
                        );
                    }
                }
        );
    }

    public void flettMotpartsPersonNavnTilFullmakt(VergeOgFullmaktData vergeOgFullmaktData) {
        vergeOgFullmaktData.getFullmakt().forEach(fullmakt -> {
            HentPerson.PersonNavn fullmaktNavn = pdlClient.hentPersonNavn(Fnr.of(fullmakt.getMotpartsPersonident()));

            if (fullmaktNavn.getNavn().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fant ikke motpartspersonnavn til fullmakt");
            }

            VergeOgFullmaktData.Navn personNavn = VergeOgFullmaktDataMapper.personNavnMapper(fullmaktNavn.getNavn());
            fullmakt.setMotpartsPersonNavn(personNavn);
        });
    }

    public String hentMalform(Fnr fnr) {
        try {
            DigdirKontaktinfo kontaktinfo = digdirClient.hentKontaktInfo(fnr);
            return kontaktinfo.getSpraak();
        } catch (Exception e) {
            log.warn("Kunne ikke hente malform fra KRR", e);
        }
        return null;
    }

    public PersonNavnV2 hentNavn(Fnr fnr) {
        HentPerson.PersonNavn personNavn = pdlClient.hentPersonNavn(fnr);

        if (personNavn.getNavn().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fant ikke navn til person");
        }

        return PersonV2DataMapper.navnMapper(personNavn.getNavn());
    }

    public HarLoggetInnRespons hentHarNivaa4(Fnr fodselsnummer) {
        if (unleashClient.isEnabled(UNLEASH_NIVAA4_DISABLED)) {
            return new HarLoggetInnRespons()
                    .setErRegistrertIdPorten(true)
                    .setHarbruktnivaa4(true)
                    .setPersonidentifikator(fodselsnummer);
        }
        return digdirClient.harLoggetInnSiste18mnd(fodselsnummer);
    }
}
