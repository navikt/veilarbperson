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
import no.nav.veilarbperson.utils.PersonV2DataMapper;
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
import static no.nav.veilarbperson.utils.PersonV2DataMapper.*;
import static no.nav.veilarbperson.utils.VergeOgFullmaktDataMapper.*;

@Slf4j
@Service
public class PersonV2Service {
    private final PdlClient pdlClient;
    private final AuthService authService;
    private final DigdirClient digdirClient;
    private final Norg2Client norg2Client;
    private final SkjermetClient skjermetClient;
    private final KodeverkService kodeverkService;
    private final RepresentasjonClient representasjonClient;


    @Autowired
    public PersonV2Service(PdlClient pdlClient,
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

    public HentPerson.Person hentPerson(PersonFraPdlRequest personFraPdlRequest) {
        return pdlClient.hentPerson(new PdlRequest(personFraPdlRequest.getFnr(), personFraPdlRequest.getBehandlingsnummer()));
    }

    public PersonV2Data hentFlettetPerson(PersonFraPdlRequest personFraPdlRequest) {
        HentPerson.Person personDataFraPdl = ofNullable(pdlClient.hentPerson(new PdlRequest(personFraPdlRequest.getFnr(), personFraPdlRequest.getBehandlingsnummer())))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Fant ikke person i hentPerson operasjonen i PDL"));

        PersonV2Data personV2Data = PersonV2DataMapper.toPersonV2Data(personDataFraPdl);
        flettInnEgenAnsatt(personV2Data, personFraPdlRequest.getFnr());
        flettBarn(personDataFraPdl.getForelderBarnRelasjon(), personV2Data, personFraPdlRequest.getBehandlingsnummer());
        flettSivilstand(personDataFraPdl.getSivilstand(), personV2Data, personFraPdlRequest.getBehandlingsnummer());
        flettDigitalKontaktinformasjon(personFraPdlRequest.getFnr(), personV2Data);
        flettGeografiskEnhet(personFraPdlRequest, personV2Data);
        flettKodeverk(personV2Data);

        return personV2Data;
    }

    public List<Familiemedlem> hentFamiliemedlemOpplysninger(List<Fnr> familemedlemFnr, Bostedsadresse bostedsadresse, String behandlingsnummer) {
        List<HentPerson.PersonFraBolk> familiemedlemInfo = pdlClient.hentPersonBolk(familemedlemFnr, behandlingsnummer);

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

    public List<Fnr> hentBarnaFnr(List<HentPerson.ForelderBarnRelasjon> familierelasjoner) {
        return familierelasjoner.stream()
                .filter(familierelasjon -> "BARN".equals(familierelasjon.getRelatertPersonsRolle()))
                .map(HentPerson.ForelderBarnRelasjon::getRelatertPersonsIdent)
                .filter(Objects::nonNull)
                .map(Fnr::of)
                .collect(Collectors.toList());
    }

    public void flettBarn(List<HentPerson.ForelderBarnRelasjon> forelderBarnRelasjoner, PersonV2Data personV2Data, String behandlingsnummer) {
        List<Fnr> barnFnrListe = hentBarnaFnr(forelderBarnRelasjoner);
        List<Familiemedlem> barnInfo = hentFamiliemedlemOpplysninger(barnFnrListe, personV2Data.getBostedsadresse(), behandlingsnummer);

        personV2Data.setBarn(barnInfo);
    }

    public void flettSivilstand(List<HentPerson.Sivilstand> sivilstands, PersonV2Data personV2Data, String behandlingsnummer) {
        List<Sivilstand> mappetSivilstand = sivilstands.stream().flatMap(sivilstand -> {
            Optional<Familiemedlem> relatert = Optional.ofNullable(sivilstand.getRelatertVedSivilstand())
                    .map(Fnr::of)
                    .map(fnr -> hentFamiliemedlemOpplysninger(List.of(fnr), personV2Data.getBostedsadresse(), behandlingsnummer))
                    .flatMap(list -> list.stream().findFirst());
            return Stream.of(sivilstandMapper(sivilstand, relatert));
        }).collect(Collectors.toList());

        personV2Data.setSivilstandliste(mappetSivilstand);
    }

    private void flettInnEgenAnsatt(PersonV2Data personV2Data, Fnr fodselsnummer) {
        Boolean egenAnsatt = skjermetClient.hentSkjermet(fodselsnummer);
        personV2Data.setEgenAnsatt(egenAnsatt);
    }

    public GeografiskTilknytning hentGeografiskTilknytning(PersonFraPdlRequest personFraPdlRequest) {
        HentPerson.GeografiskTilknytning geografiskTilknytning = pdlClient.hentGeografiskTilknytning(new PdlRequest(personFraPdlRequest.getFnr(), personFraPdlRequest.getBehandlingsnummer()));

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

    private void flettGeografiskEnhet(PersonFraPdlRequest personFraPdlRequest, PersonV2Data personV2Data) {
        String geografiskTilknytning = Optional.ofNullable(hentGeografiskTilknytning(personFraPdlRequest))
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

    private Enhet fraNorg2Enhet(no.nav.common.client.norg2.Enhet enhet) {
        return new Enhet(enhet.getEnhetNr(), enhet.getNavn());
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
        KRRPostPersonerRequest KRRPostPersonerRequest = new KRRPostPersonerRequest(Set.of(fnr.get()));
        try {
            KRRPostPersonerResponse kontaktinfo = digdirClient.hentKontaktInfo(KRRPostPersonerRequest);
            DigdirKontaktinfo digdirKontaktinfo = kontaktinfo != null ? kontaktinfo.getPersoner().get(fnr.get()): null;
            if (digdirKontaktinfo != null) {
                Optional<String> epostSisteOppdatert =  Optional.ofNullable(digdirKontaktinfo.getEpostadresseOppdatert()).map(dato -> ZonedDateTime.parse(dato).format(frontendDatoformat));
                Optional<String> mobilSisteOppdatert = Optional.ofNullable(digdirKontaktinfo.getMobiltelefonnummerOppdatert()).map(dato -> ZonedDateTime.parse(dato).format(frontendDatoformat));
                Epost epost = digdirKontaktinfo.getEpostadresse() != null
                        ? new Epost().setEpostAdresse(digdirKontaktinfo.getEpostadresse()).setEpostSistOppdatert(epostSisteOppdatert.orElse(null)).setMaster("KRR")
                        : null;
                personV2Data.setEpost(epost);
                personV2Data.setMalform(digdirKontaktinfo.getSpraak());
                leggKrrTelefonNrIListe(digdirKontaktinfo.getMobiltelefonnummer(), mobilSisteOppdatert.orElse(null), personV2Data.getTelefon());
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
                    .setMaster("KRR"));
            for (Telefon telefon : telefonListe) {
                if (!telefon.getMaster().equals("KRR")) {
                    prioritet = Integer.parseInt(telefon.getPrioritet()) + 1;
                    telefon.setPrioritet(prioritet + "");
                }
            }
        }
    }

    public TilrettelagtKommunikasjonData hentSpraakTolkInfo(PersonFraPdlRequest personFraPdlRequest) {
        HentPerson.HentSpraakTolk spraakTolkInfo = pdlClient.hentTilrettelagtKommunikasjon(new PdlRequest(personFraPdlRequest.getFnr(), personFraPdlRequest.getBehandlingsnummer()));

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

    public VergeData hentVerge(PersonFraPdlRequest personFraPdlRequest) {
        HentPerson.Verge vergeOgFullmaktFraPdl = pdlClient.hentVerge(new PdlRequest(personFraPdlRequest.getFnr(), personFraPdlRequest.getBehandlingsnummer()));
        return toVerge(vergeOgFullmaktFraPdl);
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
        KRRPostPersonerRequest KRRPostPersonerRequest = new KRRPostPersonerRequest(Set.of(fnr.get()));
        try {
            KRRPostPersonerResponse kontaktinfo = digdirClient.hentKontaktInfo(KRRPostPersonerRequest);
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

    public PersonNavnV2 hentNavn(PersonFraPdlRequest personFraPdlRequest) {
        HentPerson.PersonNavn personNavn = pdlClient.hentPersonNavn(new PdlRequest(personFraPdlRequest.getFnr(), personFraPdlRequest.getBehandlingsnummer()));

        if (personNavn.getNavn().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fant ikke navn til person");
        }
        log.info("Ferdig med hentNavn i PersonV2Service");
        return PersonV2DataMapper.navnMapper(personNavn.getNavn());
    }

    public HentPerson.Adressebeskyttelse hentAdressebeskyttelse(PersonFraPdlRequest personFraPdlRequest) {
        List<HentPerson.Adressebeskyttelse> adressebeskyttelse = Optional.ofNullable(pdlClient.hentAdressebeskyttelse(new PdlRequest(personFraPdlRequest.getFnr(), personFraPdlRequest.getBehandlingsnummer()))).orElse(List.of());
        return adressebeskyttelse.stream().findFirst().orElse(new HentPerson.Adressebeskyttelse().setGradering("UGRADERT"));
    }

    public Foedselsdato hentFoedselsdato(PersonFraPdlRequest personFraPdlRequest) {
        HentPerson.PersonFoedselsdato personFoedselsdato = pdlClient.hentFoedselsdato(
                new PdlRequest(personFraPdlRequest.getFnr(), personFraPdlRequest.getBehandlingsnummer())
        );

        if (personFoedselsdato.getFoedselsdato().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fant ikke fødselsdato for person");
        }

        HentPerson.Foedselsdato foedselsdato = personFoedselsdato.getFoedselsdato().getFirst();
        return new Foedselsdato(foedselsdato.getFoedselsdato(), foedselsdato.getFoedselsaar());

    }
}
