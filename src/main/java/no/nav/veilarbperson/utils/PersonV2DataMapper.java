package no.nav.veilarbperson.utils;

import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.pdl.HentPerson;
import no.nav.veilarbperson.client.pdl.domain.*;
import no.nav.veilarbperson.domain.PersonNavnV2;
import no.nav.veilarbperson.domain.PersonV2Data;
import no.nav.veilarbperson.service.AuthService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Optional.ofNullable;
import static no.nav.veilarbperson.client.pdl.domain.RelasjonsBosted.*;

public class PersonV2DataMapper {
    public static final DateTimeFormatter frontendDatoformat = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static PersonV2Data toPersonV2Data(HentPerson.Person person) {
        Optional<HentPerson.Navn> navn = hentGjeldeneNavn(person.getNavn());
        return new PersonV2Data()
                .setFornavn(navn.map(HentPerson.Navn::getFornavn).orElse(null))
                .setMellomnavn(navn.map(HentPerson.Navn::getMellomnavn).orElse(null))
                .setEtternavn(navn.map(HentPerson.Navn::getEtternavn).orElse(null))
                .setForkortetNavn(navn.map(HentPerson.Navn::getForkortetNavn).orElse(null))
                .setKjonn(ofNullable(getFirstElement(person.getKjoenn())).map(HentPerson.Kjoenn::getKjoenn).orElse(null))
                .setFodselsdato(ofNullable(getFirstElement(person.getFoedselsdato())).map(HentPerson.Foedselsdato::getFoedselsdato).orElse(null))
                .setStatsborgerskapKoder(ofNullable(person.getStatsborgerskap()).map(statsborgerskap -> statsborgerskap.stream().map(HentPerson.Statsborgerskap::getLand).toList()).orElse(Collections.emptyList()))
                .setDodsdato(ofNullable(getFirstElement(person.getDoedsfall())).map(HentPerson.Doedsfall::getDoedsdato).orElse(null))
                .setFodselsnummer(ofNullable(getFirstElement(person.getFolkeregisteridentifikator()))
                        .map(HentPerson.Folkeregisteridentifikator::getIdentifikasjonsnummer)
                        .map(Fnr::of).orElse(null))
                .setDiskresjonskode(ofNullable(getFirstElement(person.getAdressebeskyttelse()))
                        .map(HentPerson.Adressebeskyttelse::getGradering)
                        .map(Diskresjonskode::mapKodeTilTall).orElse(null))
                .setTelefon(mapTelefonNrFraPdl(person.getTelefonnummer()))
                .setBostedsadresse(getFirstElement(person.getBostedsadresse()))
                .setOppholdsadresse(getFirstElement(person.getOppholdsadresse()))
                .setKontaktadresser(person.getKontaktadresse())
                .setSikkerhetstiltak(ofNullable(getFirstElement(person.getSikkerhetstiltak())).map(HentPerson.Sikkerhetstiltak::getBeskrivelse).orElse(null));
    }

    public static <T> T getFirstElement(List<T> list) {
        return list.stream().findFirst().orElse(null);
    }

    public static PersonNavnV2 navnMapper(List<HentPerson.Navn> personNavn) {
        Optional<HentPerson.Navn> navn = hentGjeldeneNavn(personNavn);

        return new PersonNavnV2()
                .setFornavn(navn.map(HentPerson.Navn::getFornavn).orElse(null))
                .setMellomnavn(navn.map(HentPerson.Navn::getMellomnavn).orElse(null))
                .setEtternavn(navn.map(HentPerson.Navn::getEtternavn).orElse(null))
                .setForkortetNavn(navn.map(HentPerson.Navn::getForkortetNavn).orElse(null));
    }

    public static Fnr hentFamiliemedlemFnr(HentPerson.Familiemedlem familiemedlem) {
        return ofNullable(getFirstElement(familiemedlem.getFolkeregisteridentifikator()))
                .map(HentPerson.Folkeregisteridentifikator::getIdentifikasjonsnummer).map(Fnr::of).orElse(null);
    }

    public static boolean harGyldigIdent(HentPerson.Familiemedlem familiemedlem) {
        return !"opphoert".equals(ofNullable(getFirstElement(familiemedlem.getFolkeregisterpersonstatus()))
                .map(HentPerson.Folkeregisterpersonstatus::getForenkletStatus)
                .orElse(null));
    }

    public static Familiemedlem familiemedlemMapper(HentPerson.Familiemedlem familiemedlem,
                                                    boolean erEgenAnsatt,
                                                    Bostedsadresse personsBostedsadresse,
                                                    AuthService authService) {
        Optional<HentPerson.Navn> navn = hentGjeldeneNavn(familiemedlem.getNavn());
        Fnr medlemFnr = hentFamiliemedlemFnr(familiemedlem);
        LocalDate fodselsdato = ofNullable(getFirstElement(familiemedlem.getFoedselsdato()))
                .map(HentPerson.Foedselsdato::getFoedselsdato).orElse(null);
        String graderingskode = ofNullable(getFirstElement(familiemedlem.getAdressebeskyttelse()))
                .map(HentPerson.Adressebeskyttelse::getGradering)
                .orElse(null);
        AdressebeskyttelseGradering gradering = AdressebeskyttelseGradering.mapGradering(graderingskode);
        boolean harAdressebeskyttelse = (AdressebeskyttelseGradering.FORTROLIG).equals(gradering)
                || (AdressebeskyttelseGradering.STRENGT_FORTROLIG).equals(gradering)
                || (AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND).equals(gradering);
        boolean ukjentGradering = AdressebeskyttelseGradering.UKJENT.equals(gradering);
        boolean harVeilederLeseTilgang = authService.harLesetilgang(medlemFnr);
        RelasjonsBosted harSammeBosted = erSammeAdresse(getFirstElement(familiemedlem.getBostedsadresse()),
                personsBostedsadresse);
        Familiemedlem medlem = new Familiemedlem().setFodselsdato(fodselsdato);

        if (harVeilederLeseTilgang) {
            return medlem
                    .setGradering(graderingskode)
                    .setErEgenAnsatt(erEgenAnsatt)
                    .setHarVeilederTilgang(harVeilederLeseTilgang)
                    .setRelasjonsBosted(harSammeBosted)
                    .setFornavn(navn.map(HentPerson.Navn::getFornavn).orElse(null))
                    .setDodsdato(ofNullable(getFirstElement(familiemedlem.getDoedsfall())).map(HentPerson.Doedsfall::getDoedsdato).orElse(null));
        } else {
            if ((harAdressebeskyttelse || ukjentGradering)) {
                return medlem
                        .setGradering(graderingskode);
            } else if (erEgenAnsatt) {
                return medlem
                        .setErEgenAnsatt(erEgenAnsatt)
                        .setRelasjonsBosted(harSammeBosted);
            } else {
                return medlem
                        .setHarVeilederTilgang(harVeilederLeseTilgang)
                        .setRelasjonsBosted(harSammeBosted)
                        .setFornavn(navn.map(HentPerson.Navn::getFornavn).orElse(null))
                        .setDodsdato(ofNullable(getFirstElement(familiemedlem.getDoedsfall())).map(HentPerson.Doedsfall::getDoedsdato).orElse(null));
            }
        }
    }

    /* Sammeligner persons bostesadresse med familiemedlems bostedsadresse for å se om de har samme bosted */
    public static RelasjonsBosted erSammeAdresse(Bostedsadresse adresse1,
                                                 Bostedsadresse adresse2) {
        Bostedsadresse.Vegadresse medlemsVegadresse = ofNullable(adresse1)
                .map(Bostedsadresse::getVegadresse).orElse(null);
        Bostedsadresse.Matrikkeladresse medlemsMatrikkeladresse = ofNullable(adresse1)
                .map(Bostedsadresse::getMatrikkeladresse).orElse(null);

        Bostedsadresse.Vegadresse personsVegadresse = ofNullable(adresse2)
                .map(Bostedsadresse::getVegadresse).orElse(null);
        Bostedsadresse.Matrikkeladresse personsMatrikkeladresse = ofNullable(adresse2)
                .map(Bostedsadresse::getMatrikkeladresse).orElse(null);

        if (personsVegadresse != null && medlemsVegadresse != null) {
            if (Objects.equals(personsVegadresse, medlemsVegadresse)) {
                return SAMME_BOSTED;
            } else {
                return ANNET_BOSTED;
            }
        } else if (personsMatrikkeladresse != null && medlemsMatrikkeladresse != null) {
            if (Objects.equals(personsMatrikkeladresse, medlemsMatrikkeladresse)) {
                return SAMME_BOSTED;
            } else {
                return ANNET_BOSTED;
            }
        }

        return UKJENT_BOSTED;
    }

    public static Sivilstand sivilstandMapper(HentPerson.Sivilstand sivilstand, Optional<Familiemedlem> relatertPerson) {
        Optional<HentPerson.Metadata.Endringer> endring = finnForsteEndring(sivilstand.getMetadata().getEndringer());
        LocalDateTime opprettetDato = endring.map(HentPerson.Metadata.Endringer::getRegistrert).orElse(null);

        return new Sivilstand()
                .setSivilstand(sivilstand.getType())
                .setFraDato(sivilstand.getGyldigFraOgMed())
                .setSkjermet(relatertPerson.map(Familiemedlem::getErEgenAnsatt).orElse(null))
                .setGradering(relatertPerson.map(Familiemedlem::getGradering).orElse(null))
                .setRelasjonsBosted(relatertPerson.map(Familiemedlem::getRelasjonsBosted).orElse(null))
                .setMaster(sivilstand.getMetadata().getMaster())
                .setRegistrertDato(opprettetDato);
    }

    public static List<Telefon> mapTelefonNrFraPdl(List<HentPerson.Telefonnummer> telefonnummer) {
        return (!telefonnummer.isEmpty())
                ? telefonnummer.stream()
                .map(PersonV2DataMapper::telefonNummerMapper)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
                : new ArrayList<>();
    }

    public static Optional<HentPerson.Metadata.Endringer> finnForsteEndring(List<HentPerson.Metadata.Endringer> endringer) {
        return endringer
                .stream()
                .filter(endring -> endring.getType().equals("OPPRETT"))
                .findAny();
    }

    /* Slår sammen landkode og nummer og så lager et telefonnummer */
    public static Telefon telefonNummerMapper(HentPerson.Telefonnummer telefonnummer) {
        String telefonnr = telefonnummer.getNummer();
        String master = telefonnummer.getMetadata().getMaster();
        String landkode = ofNullable(telefonnummer.getLandskode()).orElse("");
        Optional<HentPerson.Metadata.Endringer> endring = finnForsteEndring(telefonnummer.getMetadata().getEndringer());
        String registrert = endring.map(HentPerson.Metadata.Endringer::getRegistrert)
                .map(dato -> LocalDateTime.parse(dato.toString(), ISO_LOCAL_DATE_TIME))
                .map(dato -> dato.format(frontendDatoformat))
                .orElse(null);

        return (telefonnr != null)
                ? new Telefon()
                .setPrioritet(telefonnummer.getPrioritet())
                .setTelefonNr(landkode + telefonnr)
                .setRegistrertDato(registrert)
                .setMaster(master)
                : null;
    }


    public static String parseZonedDateToDateString(ZonedDateTime dato) {
        if(dato == null){
            return null;
        }
        return dato.format(frontendDatoformat);
    }

    public static Optional<HentPerson.Navn> hentGjeldeneNavn(List<HentPerson.Navn> response) {
        return response.stream().min(Comparator.comparing(n -> n.getMetadata().getMaster().prioritet));
    }
}
