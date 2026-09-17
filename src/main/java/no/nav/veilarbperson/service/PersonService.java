package no.nav.veilarbperson.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.digdir.DigdirClient;
import no.nav.veilarbperson.client.digdir.DigdirKontaktinfo;
import no.nav.veilarbperson.client.digdir.KRRPostPersonerRequest;
import no.nav.veilarbperson.client.digdir.KRRPostPersonerResponse;
import no.nav.veilarbperson.client.nom.SkjermetClient;
import no.nav.veilarbperson.client.pdl.HentPerson;
import no.nav.veilarbperson.client.pdl.PdlClient;
import no.nav.veilarbperson.client.pdl.domain.*;
import no.nav.veilarbperson.client.representasjon.ReprFullmaktData;
import no.nav.veilarbperson.client.representasjon.RepresentasjonClient;
import no.nav.veilarbperson.domain.*;
import no.nav.veilarbperson.utils.PersonDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static no.nav.veilarbperson.utils.PersonDataMapper.*;
import static no.nav.veilarbperson.utils.VergeOgFullmaktDataMapper.*;

@Slf4j
@Service
public class PersonService {
    private static final String KRR = "KRR";

    private final PdlClient pdlClient;
    private final AuthService authService;
    private final DigdirClient digdirClient;
    private final Norg2Client norg2Client;
    private final SkjermetClient skjermetClient;
    private final KodeverkService kodeverkService;
    private final RepresentasjonClient representasjonClient;


    @Autowired
    public PersonService(PdlClient pdlClient,
                         @Qualifier("authServiceWithoutAuditLog") AuthService authServiceWithoutAuditLogg,
                         DigdirClient digdirClient,
                         Norg2Client norg2Client,
                         SkjermetClient skjermetClient,
                         KodeverkService kodeverkService,
                         RepresentasjonClient representasjonClient) {
        this.pdlClient = pdlClient;
        this.authService = authServiceWithoutAuditLogg;
        this.digdirClient = digdirClient;
        this.norg2Client = norg2Client;
        this.skjermetClient = skjermetClient;
        this.kodeverkService = kodeverkService;
        this.representasjonClient = representasjonClient;
    }

    public PersonData hentFlettetPersonTilgangsstyrt(PersonRequest personRequest) {
        HentPerson.Person personDataFraPdl = ofNullable(pdlClient.hentPerson(new PdlRequest(personRequest.getFnr(), personRequest.getBehandlingsnummer())))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Fant ikke person i hentPerson operasjonen i PDL"));

        PersonData personData = PersonDataMapper.toPersonData(personDataFraPdl);
        flettInnEgenAnsatt(personData, personRequest.getFnr());
        flettBarnTilgangsstyrt(personDataFraPdl.getForelderBarnRelasjon(), personData, personRequest.getBehandlingsnummer());
        flettSivilstand(personDataFraPdl.getSivilstand(), personData, personRequest.getBehandlingsnummer());
        flettDigitalKontaktinformasjon(personRequest.getFnr(), personData);
        flettGeografiskEnhet(personRequest, personData);
        flettKodeverk(personData);

        return personData;
    }

    public List<Familiemedlem> hentFamiliemedlemOpplysninger(List<Fnr> familemedlemFnr, Bostedsadresse bostedsadresse, String behandlingsnummer) {
        List<HentPerson.PersonFraBolk> familiemedlemInfo = pdlClient.hentPersonBolk(familemedlemFnr, behandlingsnummer);

        return familiemedlemInfo
                .stream()
                .filter(medlemInfo -> medlemInfo.getCode().equals("ok"))
                .map(HentPerson.PersonFraBolk::getPerson)
                .filter(PersonDataMapper::harGyldigIdent)
                .map(familiemedlem -> mapFamiliemedlem(familiemedlem, bostedsadresse))
                .collect(Collectors.toList());
    }

    public List<FamiliemedlemTilgangsstyrt> hentFamiliemedlemOpplysningerTilgangsstyrt(List<Fnr> familemedlemFnr, Bostedsadresse bostedsadresse, String behandlingsnummer) {
        List<HentPerson.PersonFraBolk> familiemedlemInfo = pdlClient.hentPersonBolk(familemedlemFnr, behandlingsnummer);

        return familiemedlemInfo
                .stream()
                .filter(medlemInfo -> medlemInfo.getCode().equals("ok"))
                .map(HentPerson.PersonFraBolk::getPerson)
                .filter(PersonDataMapper::harGyldigIdent)
                .map(familiemedlemTilgangsstyrt -> mapFamiliemedlemTilgangsstyrt(familiemedlemTilgangsstyrt, bostedsadresse))
                .collect(Collectors.toList());
    }
    private boolean erSkjermet(Fnr fnr) {
        return skjermetClient.hentSkjermet(fnr);
    }

    public Familiemedlem mapFamiliemedlem(HentPerson.Familiemedlem familiemedlem, Bostedsadresse bostedsadresse) {
        Fnr familiemedlemFnr = PersonDataMapper.hentFamiliemedlemFnr(familiemedlem);

        return PersonDataMapper.familiemedlemMapper(
                familiemedlem,
                erSkjermet(familiemedlemFnr),
                bostedsadresse,
                authService
        );
    }

    public FamiliemedlemTilgangsstyrt mapFamiliemedlemTilgangsstyrt(HentPerson.Familiemedlem familiemedlem, Bostedsadresse bostedsadresse) {
        Fnr familiemedlemFnr = PersonDataMapper.hentFamiliemedlemFnr(familiemedlem);

        return PersonDataMapper.familiemedlemTilgangsstyrtMapper(
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
                .filter(Objects::nonNull)
                .map(Fnr::of)
                .collect(Collectors.toList());
    }

    public void flettBarnTilgangsstyrt(List<HentPerson.ForelderBarnRelasjon> forelderBarnRelasjoner, PersonData personData, String behandlingsnummer) {
        List<Fnr> barnFnrListe = hentBarnaFnr(forelderBarnRelasjoner);
        List<FamiliemedlemTilgangsstyrt> barnInfo = hentFamiliemedlemOpplysningerTilgangsstyrt(barnFnrListe, personData.getBostedsadresse(), behandlingsnummer);

        if (barnInfo.isEmpty()) {
            personData.setBarn(Collections.emptyList());
        } else {
            personData.setBarn(new ArrayList<>(barnInfo));
        }
    }

    public void flettSivilstand(List<HentPerson.Sivilstand> sivilstands, PersonData personData, String behandlingsnummer) {
        List<Sivilstand> mappetSivilstand = sivilstands.stream().flatMap(sivilstand -> {
            Optional<Familiemedlem> relatert = Optional.ofNullable(sivilstand.getRelatertVedSivilstand())
                    .map(Fnr::of)
                    .map(fnr -> hentFamiliemedlemOpplysninger(List.of(fnr), personData.getBostedsadresse(), behandlingsnummer))
                    .flatMap(list -> list.stream().findFirst());
            return Stream.of(sivilstandMapper(sivilstand, relatert));
        }).toList();

        personData.setSivilstandliste(mappetSivilstand);
    }

    private void flettInnEgenAnsatt(PersonData personData, Fnr fodselsnummer) {
        Boolean egenAnsatt = skjermetClient.hentSkjermet(fodselsnummer);
        personData.setEgenAnsatt(egenAnsatt);
    }

    public GeografiskTilknytning hentGeografiskTilknytning(PersonRequest personRequest) {
        HentPerson.GeografiskTilknytning geografiskTilknytning = pdlClient.hentGeografiskTilknytning(new PdlRequest(personRequest.getFnr(), personRequest.getBehandlingsnummer()));

        if (geografiskTilknytning == null) {
            return null;
        }

        return switch (geografiskTilknytning.getGtType()) {
            case "KOMMUNE" -> new GeografiskTilknytning(geografiskTilknytning.getGtKommune());
            case "BYDEL" -> new GeografiskTilknytning(geografiskTilknytning.getGtBydel());
            case "UTLAND" -> new GeografiskTilknytning(geografiskTilknytning.getGtLand());
            default ->  // type == UDEFINERT
                    null;
        };
    }

    private void flettGeografiskEnhet(PersonRequest personRequest, PersonData personData) {
        String geografiskTilknytning = Optional.ofNullable(hentGeografiskTilknytning(personRequest))
                .map(GeografiskTilknytning::getGeografiskTilknytning)
                .orElse(null);

        personData.setGeografiskTilknytning(geografiskTilknytning);

        // Sjekk at geografiskTilknytning er satt og at det ikke er en tre-bokstavs landkode (ISO 3166 Alpha-3, for utenlandske brukere så blir landskode brukt istedenfor nummer)
        if (geografiskTilknytning != null && geografiskTilknytning.matches("\\d+")) {
            try {
                // Henter geografisk enhet, derfor settes ikke diskresjonskode og skjermet
                Enhet enhet = fraNorg2Enhet(norg2Client.hentTilhorendeEnhet(geografiskTilknytning, null, false));
                personData.setGeografiskEnhet(enhet);
            } catch (Exception e) {
                log.error("Klarte ikke å flette inn geografisk enhet", e);
            }
        }
    }

    private Enhet fraNorg2Enhet(no.nav.common.client.norg2.Enhet enhet) {
        return new Enhet(enhet.getEnhetNr(), enhet.getNavn());
    }

    public void flettKodeverk(PersonData personData) {
        Optional<String> postnrIBostedsVegAdr = ofNullable(personData.getBostedsadresse()).map(Bostedsadresse::getVegadresse).map(
                Adresse.Vegadresse::getPostnummer);
        Optional<String> postnrIBostedsMatrikkelAdr = ofNullable(personData.getBostedsadresse()).map(Bostedsadresse::getMatrikkeladresse).map(
                Bostedsadresse.Matrikkeladresse::getPostnummer);
        Optional<String> kommunenrIBostedsVegAdr = ofNullable(personData.getBostedsadresse()).map(Bostedsadresse::getVegadresse).map(
                Adresse.Vegadresse::getKommunenummer);
        Optional<String> kommunenrIBostedsMatrikkelAdr = ofNullable(personData.getBostedsadresse()).map(Bostedsadresse::getMatrikkeladresse).map(
                Bostedsadresse.Matrikkeladresse::getKommunenummer);
        Optional<String> kommunenrIBostedsUkjentAdr = ofNullable(personData.getBostedsadresse()).map(Bostedsadresse::getUkjentBosted).map(
                Bostedsadresse.UkjentBosted::getBostedskommune);
        Optional<String> kommunenrIOppholdsVegAdr = ofNullable(personData.getOppholdsadresse()).map(Oppholdsadresse::getVegadresse).map(
                Adresse.Vegadresse::getKommunenummer);
        Optional<String> kommunenrIOppholdsMatrikkelAdr = ofNullable(personData.getOppholdsadresse()).map(
                Oppholdsadresse::getMatrikkeladresse).map(Oppholdsadresse.Matrikkeladresse::getKommunenummer);
        Optional<String> landkodeIBostedsUtenlandskAdr = ofNullable(personData.getBostedsadresse()).map(Bostedsadresse::getUtenlandskAdresse).map(
                Adresse.Utenlandskadresse::getLandkode);
        Optional<String> postnrIOppholdsVegAdr = ofNullable(personData.getOppholdsadresse()).map(Oppholdsadresse::getVegadresse).map(
                Adresse.Vegadresse::getPostnummer);
        Optional<String> postnrIOppholdsMatrikkelAdr = ofNullable(personData.getOppholdsadresse()).map(Oppholdsadresse::getMatrikkeladresse).map(
                Oppholdsadresse.Matrikkeladresse::getPostnummer);
        Optional<String> landkodeIOppholdsUtenlandskAdr = ofNullable(personData.getOppholdsadresse()).map(
                Oppholdsadresse::getUtenlandskAdresse).map(Adresse.Utenlandskadresse::getLandkode);

        postnrIBostedsVegAdr.map(kodeverkService::getPoststedForPostnummer).ifPresent(personData::setPoststedIBostedsVegadresse);
        postnrIBostedsMatrikkelAdr.map(kodeverkService::getPoststedForPostnummer).ifPresent(personData::setPoststedIBostedsMatrikkeladresse);
        kommunenrIBostedsVegAdr.map(kodeverkService::getBeskrivelseForKommunenummer).ifPresent(personData::setKommuneIBostedsVegadresse);
        kommunenrIBostedsMatrikkelAdr.map(kodeverkService::getBeskrivelseForKommunenummer).ifPresent(personData::setKommuneIBostedsMatrikkeladresse);
        kommunenrIBostedsUkjentAdr.map(kodeverkService::getBeskrivelseForKommunenummer).ifPresent(personData::setKommuneIBostedsUkjentadresse);
        kommunenrIOppholdsVegAdr.map(kodeverkService::getBeskrivelseForKommunenummer).ifPresent(personData::setKommuneIOppholdssVegadresse);
        kommunenrIOppholdsMatrikkelAdr.map(kodeverkService::getBeskrivelseForKommunenummer).ifPresent(personData::setKommuneIOppholdsMatrikkeladresse);
        postnrIOppholdsVegAdr.map(kodeverkService::getPoststedForPostnummer).ifPresent(personData::setPoststedIOppholdsVegadresse);
        postnrIOppholdsMatrikkelAdr.map(kodeverkService::getPoststedForPostnummer).ifPresent(personData::setPoststedIOppholdsMatrikkeladresse);
        landkodeIBostedsUtenlandskAdr.map(kodeverkService::getBeskrivelseForLandkode).ifPresent(personData::setLandkodeIBostedsUtenlandskadresse);
        landkodeIOppholdsUtenlandskAdr.map(kodeverkService::getBeskrivelseForLandkode).ifPresent(personData::setLandkodeIOppholdsUtenlandskadresse);
        personData.setStatsborgerskap(personData.getStatsborgerskapKoder()
                .stream()
                .map(kodeverkService::getBeskrivelseForLandkode)
                .filter(Objects::nonNull)
                .toList());

        List<Kontaktadresse> kontaktadresseList = personData.getKontaktadresser();

        for (Kontaktadresse kontaktadresse : kontaktadresseList) {
            Optional<String> postnrIKontaktsVegAdr = ofNullable(kontaktadresse).map(Kontaktadresse::getVegadresse).map(
                    Adresse.Vegadresse::getPostnummer);
            Optional<String> postnrIKontaktsPostboksAdr = ofNullable(kontaktadresse).map(Kontaktadresse::getPostboksadresse).map(
                    Kontaktadresse.Postboksadresse::getPostnummer);
            Optional<String> postnrIPostAdresseIFrittFormat = ofNullable(kontaktadresse).map(Kontaktadresse::getPostadresseIFrittFormat).map(
                    Kontaktadresse.PostadresseIFrittFormat::getPostnummer);
            Optional<String> landkodeIKontaktsUtenlandskAdr = ofNullable(kontaktadresse).map(Kontaktadresse::getUtenlandskAdresse).map(
                    Adresse.Utenlandskadresse::getLandkode);
            Optional<String> landkodeIUtenlandskAdresseIFrittFormat = ofNullable(kontaktadresse).map(Kontaktadresse::getUtenlandskAdresseIFrittFormat).map(
                    Kontaktadresse.UtenlandskAdresseIFrittFormat::getLandkode);
            Optional<String> kommunenrIKontaktsVegAdr = ofNullable(kontaktadresse).map(Kontaktadresse::getVegadresse).map(
                    Adresse.Vegadresse::getKommunenummer);

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

    private void flettDigitalKontaktinformasjon(Fnr fnr, PersonData personData) {
        KRRPostPersonerRequest krrPostPersonerRequest = new KRRPostPersonerRequest(Set.of(fnr.get()));
        try {
            KRRPostPersonerResponse kontaktinfo = digdirClient.hentKontaktInfo(krrPostPersonerRequest);
            DigdirKontaktinfo digdirKontaktinfo = kontaktinfo != null ? kontaktinfo.getPersoner().get(fnr.get()) : null;
            if (digdirKontaktinfo != null) {
                Optional<String> epostSisteOppdatert = Optional.ofNullable(digdirKontaktinfo.getEpostadresseOppdatert()).map(dato -> ZonedDateTime.parse(dato).format(frontendDatoformat));
                Optional<String> mobilSisteOppdatert = Optional.ofNullable(digdirKontaktinfo.getMobiltelefonnummerOppdatert()).map(dato -> ZonedDateTime.parse(dato).format(frontendDatoformat));
                Epost epost = digdirKontaktinfo.getEpostadresse() != null
                        ? new Epost().setEpostAdresse(digdirKontaktinfo.getEpostadresse()).setEpostSistOppdatert(epostSisteOppdatert.orElse(null)).setMaster(KRR)
                        : null;
                personData.setEpost(epost);
                personData.setMalform(digdirKontaktinfo.getSpraak());
                leggKrrTelefonNrIListe(digdirKontaktinfo.getMobiltelefonnummer(), mobilSisteOppdatert.orElse(null), personData.getTelefon());
            } else {
                log.warn("Fant ikke kontaktinfo i KRR");
            }
        } catch (Exception e) {
            log.warn("Kunne ikke flette digitalkontaktinfo fra KRR", e);
        }
    }

    /* Telefonnummer fra PDL og KRR legges sammen i en liste.
       KRR telefonnummeret vil alltid ha høyere prioritet enn PDL telefonnummeret.
       Hvis like nummer, fjernes PDL-nummeret
    */
    public void leggKrrTelefonNrIListe(String telefonNummerFraKrr, String sistOppdatert, List<Telefon> telefonListe) {
        int prioritet;
        if (telefonNummerFraKrr != null) {
            telefonListe.removeIf(telefon -> telefonNummerFraKrr.equals(telefon.getTelefonNr()));
            telefonListe.add(new Telefon()
                    .setPrioritet(1 + "")
                    .setTelefonNr(telefonNummerFraKrr)
                    .setRegistrertDato(sistOppdatert)
                    .setMaster(KRR));
            for (Telefon telefon : telefonListe) {
                if (!telefon.getMaster().equals(KRR)) {
                    prioritet = Integer.parseInt(telefon.getPrioritet()) + 1;
                    telefon.setPrioritet(prioritet + "");
                }
            }
        }
    }

    public TilrettelagtKommunikasjonData hentSpraakTolkInfo(PersonRequest personRequest) {
        HentPerson.HentSpraakTolk spraakTolkInfo = pdlClient.hentTilrettelagtKommunikasjon(new PdlRequest(personRequest.getFnr(), personRequest.getBehandlingsnummer()));

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

    public VergeData hentVerge(PersonRequest personRequest) {
        HentPerson.Verge vergeOgFullmaktFraPdl = pdlClient.hentVerge(new PdlRequest(personRequest.getFnr(), personRequest.getBehandlingsnummer()));

        List<VergeData.VergemaalEllerFremtidsfullmakt> vergeMedNavn =
                vergeOgFullmaktFraPdl.getVergemaalEllerFremtidsfullmakt()
                        .stream()
                        .map(vergemaalEllerFremtidsfullmakt -> {
                            String motpartsFnr = vergemaalEllerFremtidsfullmakt.getVergeEllerFullmektig().getMotpartsPersonident();
                            PersonNavn vergeNavn = null;

                            if (motpartsFnr != null) {
                                Fnr vergeFnr = Fnr.of(motpartsFnr);
                                vergeNavn = hentNavn(new PersonRequest(vergeFnr, personRequest.getBehandlingsnummer()));
                            }

                            return toVergemaalEllerFremtidsfullmakt(vergemaalEllerFremtidsfullmakt, vergeNavn);
                        })
                        .collect(Collectors.toList());

        VergeData vergeData = new VergeData();
        vergeData.setVergemaalEllerFremtidsfullmakt(vergeMedNavn);
        return vergeData;
    }

    public FullmaktDTO hentFullmakt(PersonRequest personRequest) throws IOException {
        List<ReprFullmaktData.Fullmakt> fullmaktListe = representasjonClient.hentFullmakt(personRequest.getFnr().get());
        if (fullmaktListe.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "Person har ikke fullmakt i representasjon");
        }
        FullmaktDTO fullmaktDTO = toFullmaktDTO(fullmaktListe);
        flettBeskrivelseTilFullmaktTema(fullmaktDTO);
        return fullmaktDTO;
    }

    public void flettBeskrivelseTilFullmaktTema(FullmaktDTO fullmaktDto) {
        if (!fullmaktDto.getFullmakt().isEmpty()) {
            fullmaktDto.getFullmakt().forEach(fullmakt -> {
                if (!fullmakt.getOmraade().isEmpty()) {
                    fullmakt.getOmraade().forEach(omraade -> {
                        if (omraade.getTema().equals("*")) {
                            omraade.setTema("alle ytelser");
                        } else {
                            String beskrivelseForTema = kodeverkService.getBeskrivelseForTema(omraade.getTema());
                            omraade.setTema(beskrivelseForTema);
                        }
                    });
                }
            });
        }
    }

    public String hentMalform(Fnr fnr) {
        KRRPostPersonerRequest krrPostPersonerRequest = new KRRPostPersonerRequest(Set.of(fnr.get()));
        try {
            KRRPostPersonerResponse kontaktinfo = digdirClient.hentKontaktInfo(krrPostPersonerRequest);
            if (kontaktinfo == null) {
                log.warn("Fant ikke kontaktinfo (målform) i KRR");
                return null;
            }
            DigdirKontaktinfo digdirKontaktinfo = kontaktinfo.getPersoner().get(fnr.get());
            return digdirKontaktinfo.getSpraak();
        } catch (Exception e) {
            log.warn("Kunne ikke hente malform fra KRR", e);
        }
        return null;
    }

    public PersonNavn hentNavn(PersonRequest personRequest) {
        HentPerson.PersonNavn personNavn = pdlClient.hentPersonNavn(new PdlRequest(personRequest.getFnr(), personRequest.getBehandlingsnummer()));

        if (personNavn.getNavn().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fant ikke navn til person");
        }
        log.info("Ferdig med hentNavn i PersonService");
        return PersonDataMapper.navnMapper(personNavn.getNavn());
    }

    public HentPerson.Adressebeskyttelse hentAdressebeskyttelse(PersonRequest personRequest) {
        List<HentPerson.Adressebeskyttelse> adressebeskyttelse = Optional.ofNullable(pdlClient.hentAdressebeskyttelse(new PdlRequest(personRequest.getFnr(), personRequest.getBehandlingsnummer()))).orElse(List.of());
        return adressebeskyttelse.stream().findFirst().orElse(new HentPerson.Adressebeskyttelse().setGradering("UGRADERT"));
    }

    public Foedselsdato hentFoedselsdato(PersonRequest personRequest) {
        HentPerson.PersonFoedselsdato personFoedselsdato = pdlClient.hentFoedselsdato(
                new PdlRequest(personRequest.getFnr(), personRequest.getBehandlingsnummer())
        );

        if (personFoedselsdato.getFoedselsdato().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fant ikke fødselsdato for person");
        }

        HentPerson.Foedselsdato foedselsdato = personFoedselsdato.getFoedselsdato().getFirst();
        return new Foedselsdato(foedselsdato.getFoedselsdato(), foedselsdato.getFoedselsaar());

    }
}
