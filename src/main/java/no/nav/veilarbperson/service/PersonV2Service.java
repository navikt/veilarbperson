package no.nav.veilarbperson.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.featuretoggle.UnleashClient;
import no.nav.common.sts.SystemUserTokenProvider;
import no.nav.common.types.identer.Fnr;
import no.nav.common.utils.EnvironmentUtils;
import no.nav.veilarbperson.client.dkif.DkifClient;
import no.nav.veilarbperson.client.dkif.DkifKontaktinfo;
import no.nav.veilarbperson.client.egenansatt.EgenAnsattClient;
import no.nav.veilarbperson.client.nom.SkjermetClient;
import no.nav.veilarbperson.client.pdl.PdlAuth;
import no.nav.veilarbperson.client.pdl.HentPerson;
import no.nav.veilarbperson.client.pdl.PdlClient;
import no.nav.veilarbperson.client.pdl.domain.*;
import no.nav.veilarbperson.client.person.PersonClient;
import no.nav.veilarbperson.client.veilarbportefolje.Personinfo;
import no.nav.veilarbperson.client.veilarbportefolje.VeilarbportefoljeClient;
import no.nav.veilarbperson.domain.*;
import no.nav.veilarbperson.utils.DownstreamApi;
import no.nav.veilarbperson.utils.PersonDataMapper;
import no.nav.veilarbperson.utils.PersonV2DataMapper;
import no.nav.veilarbperson.utils.VergeOgFullmaktDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.*;
import static java.util.Optional.ofNullable;
import static no.nav.veilarbperson.utils.Mappers.fraNorg2Enhet;
import static no.nav.veilarbperson.utils.PersonV2DataMapper.getFirstElement;
import static no.nav.veilarbperson.utils.PersonV2DataMapper.erSammeAdresse;
import static no.nav.veilarbperson.utils.PersonV2DataMapper.sivilstandMapper;
import static no.nav.veilarbperson.utils.VergeOgFullmaktDataMapper.toVergeOgFullmaktData;

@Slf4j
@Service
public class PersonV2Service {
    private final PdlClient pdlClient;
    private final AuthService authService;
    private final DkifClient dkifClient;
    private final Norg2Client norg2Client;
    private final PersonClient personClient;
    private final SkjermetClient skjermetClient;
    private final KodeverkService kodeverkService;
    private final SystemUserTokenProvider systemUserTokenProvider;

    @Autowired
    public PersonV2Service(PdlClient pdlClient,
                           AuthService authService,
                           DkifClient dkifClient,
                           Norg2Client norg2Client,
                           PersonClient personClient,
                           SkjermetClient skjermetClient,
                           KodeverkService kodeverkService,
                           SystemUserTokenProvider systemUserTokenProvider) {
        this.pdlClient = pdlClient;
        this.authService = authService;
        this.dkifClient = dkifClient;
        this.norg2Client = norg2Client;
        this.personClient = personClient;
        this.skjermetClient = skjermetClient;
        this.kodeverkService = kodeverkService;
        this.systemUserTokenProvider = systemUserTokenProvider;
    }

    public HentPerson.Person hentPerson(Fnr personIdent) {
        return pdlClient.hentPerson(personIdent, getPdlAuth());
    }

    private PdlAuth getPdlAuth() {
        if (authService.erAadOboToken()) {
            DownstreamApi pdlDownstreamApi =
                    new DownstreamApi(EnvironmentUtils.requireClusterName(), "pdl", "pdl-api");
            return new PdlAuth(authService.getAadOboTokenForTjeneste(pdlDownstreamApi), Optional.empty());
        }
        return new PdlAuth(
                authService.getInnloggetBrukerToken(),
                Optional.of(systemUserTokenProvider.getSystemUserToken())
        );
    }

    public PersonData hentPersonDataFraTps(Fnr personIdent) {
        return PersonDataMapper.tilPersonData(personClient.hentPerson(personIdent));
    }

    public PersonV2Data hentFlettetPerson(Fnr fodselsnummer) {
        PdlAuth auth = getPdlAuth();
        HentPerson.Person personDataFraPdl = ofNullable(pdlClient.hentPerson(fodselsnummer, auth))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Fant ikke person i hentPerson operasjonen i PDL"));

        PersonV2Data personV2Data = PersonV2DataMapper.toPersonV2Data(personDataFraPdl);
        flettInnKontonummer(personV2Data);

        flettInnEgenAnsatt(personV2Data, fodselsnummer);
        flettBarn(personDataFraPdl.getForelderBarnRelasjon(), personV2Data);
        flettSivilstand(personDataFraPdl.getSivilstand(), personV2Data);
        flettDigitalKontaktinformasjon(fodselsnummer, personV2Data);
        flettGeografiskEnhet(fodselsnummer, auth, personV2Data);
        flettKodeverk(personV2Data);

        return personV2Data;
    }

    public void flettInnKontonummer(PersonV2Data person) {
        PersonData personDataFraTps = hentPersonDataFraTps(person.getFodselsnummer());
        person.setKontonummer(personDataFraTps.getKontonummer());
    }

    public List<Familiemedlem> hentFamiliemedlemOpplysninger(List<Fnr> familemedlemFnr, Bostedsadresse bostedsadresse) {
        String token = systemUserTokenProvider.getSystemUserToken();
        PdlAuth auth = new PdlAuth(token, Optional.of(token));
        List<HentPerson.PersonFraBolk> familiemedlemInfo = pdlClient.hentPersonBolk(familemedlemFnr, auth);

        return familiemedlemInfo
                .stream()
                .filter(medlemInfo -> medlemInfo.getCode().equals("ok"))
                .map(HentPerson.PersonFraBolk::getPerson)
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

    public List<Fnr> hentBarnaFnr(List<HentPerson.ForelderBarnRelasjon> familierelasjoner) {
        return familierelasjoner.stream()
                .filter(familierelasjon -> "BARN".equals(familierelasjon.getRelatertPersonsRolle()))
                .map(HentPerson.ForelderBarnRelasjon::getRelatertPersonsIdent)
                .map(Fnr::of)
                .collect(Collectors.toList());
    }

    public void flettBarn(List<HentPerson.ForelderBarnRelasjon> forelderBarnRelasjoner, PersonV2Data personV2Data) {
        List<Fnr> barnFnrListe = hentBarnaFnr(forelderBarnRelasjoner);
        List<Familiemedlem> barnInfo = hentFamiliemedlemOpplysninger(barnFnrListe, personV2Data.getBostedsadresse());
        personV2Data.setBarn(barnInfo);
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

    private void flettGeografiskEnhet(Fnr fodselsnummer, PdlAuth auth, PersonV2Data personV2Data) {
        String geografiskTilknytning = ofNullable(pdlClient.hentGeografiskTilknytning(fodselsnummer, auth))
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
        ofNullable(personV2Data.getStatsborgerskap()).map(kodeverkService::getBeskrivelseForLandkode).ifPresent(
                personV2Data::setStatsborgerskap);

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
            DkifKontaktinfo kontaktinfo = dkifClient.hentKontaktInfo(fnr);
            String epostSisteOppdatert = kontaktinfo.getEpostSistOppdatert();
            String formatertEpostSisteOppdatert = epostSisteOppdatert != null ? PersonV2DataMapper.parseDateFromDateTime(
                    epostSisteOppdatert) : null;

            Epost epost = kontaktinfo.getEpostadresse() != null
                    ? new Epost().setEpostAdresse(kontaktinfo.getEpostadresse()).setEpostSistOppdatert(
                    formatertEpostSisteOppdatert).setMaster("KRR")
                    : null;

            personV2Data.setEpost(epost);
            personV2Data.setMalform(kontaktinfo.getSpraak());
            leggKrrTelefonNrIListe(kontaktinfo.getMobiltelefonnummer(),
                    kontaktinfo.getMobilSistOppdatert(),
                    personV2Data.getTelefon());
        } catch (Exception e) {
            log.warn("Kunne ikke flette digitalkontaktinfo fra KRR", e);
        }
    }

    /* Legger telefonnummer fra PDL og KRR til en liste. Hvis de er like da kan liste inneholde kun en av dem */
    public void leggKrrTelefonNrIListe(String telefonNummerFraKrr, String sistOppdatert, List<Telefon> telefonListe) {
        boolean ikkeKrrTelefonIListe = telefonNummerFraKrr != null
                && telefonListe.stream().noneMatch(t -> telefonNummerFraKrr.equals(t.getTelefonNr()));
        String registrertDato = sistOppdatert != null ? PersonV2DataMapper.parseDateFromDateTime(sistOppdatert) : null;

        if (ikkeKrrTelefonIListe) {
            telefonListe.add(new Telefon()
                    .setPrioritet(telefonListe.size() + 1 + "")
                    .setTelefonNr(telefonNummerFraKrr)
                    .setRegistrertDato(registrertDato)
                    .setMaster("KRR"));
        }
    }

    public TilrettelagtKommunikasjonData hentSpraakTolkInfo(Fnr fnr) {
        PdlAuth auth = getPdlAuth();
        HentPerson.HentSpraakTolk spraakTolkInfo = pdlClient.hentTilrettelagtKommunikasjon(fnr, auth);

        if (spraakTolkInfo.getTilrettelagtKommunikasjon().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Fant ikke tilrettelagtkommunikasjon til person i PDL");
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
        PdlAuth auth = getPdlAuth();
        HentPerson.VergeOgFullmakt vergeOgFullmaktFraPdl = pdlClient.hentVergeOgFullmakt(fnr, auth);

        if (vergeOgFullmaktFraPdl.getVergemaalEllerFremtidsfullmakt().isEmpty() && vergeOgFullmaktFraPdl.getFullmakt().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Person har ikke verge eller fullmakt i PDL");
        }

        VergeOgFullmaktData vergeOgFullmaktData = toVergeOgFullmaktData(vergeOgFullmaktFraPdl);

        flettMotpartsPersonNavnTilFullmakt(vergeOgFullmaktData, auth);
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

    public void flettMotpartsPersonNavnTilFullmakt(VergeOgFullmaktData vergeOgFullmaktData, PdlAuth auth) {
        vergeOgFullmaktData.getFullmakt().forEach(fullmakt -> {
            HentPerson.PersonNavn fullmaktNavn = pdlClient.hentPersonNavn(Fnr.of(fullmakt.getMotpartsPersonident()),
                    auth);

            if (fullmaktNavn.getNavn().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fant ikke motpartspersonnavn til fullmakt");
            }

            VergeOgFullmaktData.Navn personNavn = VergeOgFullmaktDataMapper.personNavnMapper(fullmaktNavn.getNavn());
            fullmakt.setMotpartsPersonNavn(personNavn);
        });
    }

    public String hentMalform(Fnr fnr) {
        try {
            DkifKontaktinfo kontaktinfo = dkifClient.hentKontaktInfo(fnr);
            return kontaktinfo.getSpraak();
        } catch (Exception e) {
            log.warn("Kunne ikke hente malform fra KRR", e);
        }
        return null;
    }

    public PersonNavnV2 hentNavn(Fnr fnr) {
        HentPerson.PersonNavn personNavn = pdlClient.hentPersonNavn(fnr, getPdlAuth());

        if (personNavn.getNavn().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fant ikke navn til person");
        }

        return PersonV2DataMapper.navnMapper(personNavn.getNavn());
    }
}
