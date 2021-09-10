package no.nav.veilarbperson.utils;

import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.pdl.HentPerson;
import no.nav.veilarbperson.domain.PersonV2Data;
import no.nav.veilarbperson.client.pdl.domain.*;
import no.nav.veilarbperson.domain.PersonData;
import no.nav.veilarbperson.domain.PersonNavnV2;
import no.nav.veilarbperson.client.pdl.domain.Telefon;
import no.nav.veilarbperson.service.AuthService;

import java.time.LocalDate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Optional.ofNullable;

public class PersonV2DataMapper {

    public static PersonV2Data toPersonV2Data(HentPerson.Person person, PersonData personDataFraTps) {

        return new PersonV2Data()
                .setFornavn(ofNullable(getFirstElement(person.getNavn())).map(HentPerson.Navn::getFornavn).orElse(null))
                .setMellomnavn(ofNullable(getFirstElement(person.getNavn())).map(HentPerson.Navn::getMellomnavn).orElse(null))
                .setEtternavn(ofNullable(getFirstElement(person.getNavn())).map(HentPerson.Navn::getEtternavn).orElse(null))
                .setForkortetNavn(ofNullable(getFirstElement(person.getNavn())).map(HentPerson.Navn::getForkortetNavn).orElse(null))
                .setKjonn(ofNullable(getFirstElement(person.getKjoenn())).map(HentPerson.Kjoenn::getKjoenn).orElse(null))
                .setFodselsdato(ofNullable(getFirstElement(person.getFoedsel())).map(HentPerson.Foedsel::getFoedselsdato).orElse(null))
                .setStatsborgerskap(ofNullable(getFirstElement(person.getStatsborgerskap())).map(HentPerson.Statsborgerskap::getLand).orElse(null))
                .setDodsdato(ofNullable(getFirstElement(person.getDoedsfall())).map(HentPerson.Doedsfall::getDoedsdato).orElse(null))
                .setFodselsnummer(ofNullable(getFirstElement(person.getFolkeregisteridentifikator()))
                        .map(HentPerson.Folkeregisteridentifikator::getIdentifikasjonsnummer)
                        .map(Fnr::of).orElse(null))
                .setKontonummer(personDataFraTps.getKontonummer())
                .setDiskresjonskode(ofNullable(getFirstElement(person.getAdressebeskyttelse()))
                        .map(HentPerson.Adressebeskyttelse::getGradering)
                        .map(Diskresjonskoder::mapTilTallkode).orElse(null))
                .setTelefon(mapTelefonNrFraPdl(person.getTelefonnummer()))
                .setSivilstand(ofNullable(sivilstandMapper(getFirstElement(person.getSivilstand()))).orElse(null))
                .setBostedsadresse(ofNullable(getFirstElement(person.getBostedsadresse())).orElse(null))
                .setOppholdsadresse(ofNullable(getFirstElement(person.getOppholdsadresse())).orElse(null))
                .setKontaktadresser(ofNullable(person.getKontaktadresse()).orElse(null));
    }

    public static <T> T getFirstElement(List<T> list) {
        return list.stream().findFirst().orElse(null);
    }

    public static PersonNavnV2 navnMapper(List<HentPerson.Navn> personNavn) {
        HentPerson.Navn navn = getFirstElement(personNavn);

        return new PersonNavnV2()
                .setFornavn(ofNullable(navn).map(HentPerson.Navn::getFornavn).orElse(null))
                .setMellomnavn(ofNullable(navn).map(HentPerson.Navn::getMellomnavn).orElse(null))
                .setEtternavn(ofNullable(navn).map(HentPerson.Navn::getEtternavn).orElse(null))
                .setForkortetNavn(ofNullable(navn).map(HentPerson.Navn::getForkortetNavn).orElse(null));
    }

    public static Fnr hentFamiliemedlemFnr(HentPerson.Familiemedlem familiemedlem) {
        return ofNullable(getFirstElement(familiemedlem.getFolkeregisteridentifikator()))
                .map(HentPerson.Folkeregisteridentifikator::getIdentifikasjonsnummer).map(Fnr::of).orElse(null);
    }

    public static Familiemedlem familiemedlemMapper(HentPerson.Familiemedlem familiemedlem, boolean erEgenAnsatt, Bostedsadresse personsBostedsadresse, AuthService authService) {
        HentPerson.Navn navn = getFirstElement(familiemedlem.getNavn());
        Fnr medlemFnr = hentFamiliemedlemFnr(familiemedlem);
        String kjonn = ofNullable(getFirstElement(familiemedlem.getKjoenn())).map(HentPerson.Kjoenn::getKjoenn).orElse(null);
        LocalDate fodselsdato = ofNullable(getFirstElement(familiemedlem.getFoedsel())).map(HentPerson.Foedsel::getFoedselsdato).orElse(null);
        AdressebeskyttelseGradering gradering = ofNullable(getFirstElement(familiemedlem.getAdressebeskyttelse()))
                .map(HentPerson.Adressebeskyttelse::getGradering)
                .map(AdressebeskyttelseGradering::mapGradering)
                .orElse(AdressebeskyttelseGradering.UGRADERT);
        boolean harAdressebeskyttelse = (AdressebeskyttelseGradering.FORTROLIG).equals(gradering)
                || (AdressebeskyttelseGradering.STRENGT_FORTROLIG).equals(gradering)
                || (AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND).equals(gradering);
        boolean ukjentGradering = AdressebeskyttelseGradering.UKJENT.equals(gradering);
        boolean harVeilederLeseTilgang = authService.harLesetilgang(medlemFnr);
        boolean harSammeBosted = harFamiliamedlemSammeBostedSomPerson(getFirstElement(familiemedlem.getBostedsadresse()), personsBostedsadresse);
        Familiemedlem medlem = new Familiemedlem().setFodselsnummer(medlemFnr).setFodselsdato(fodselsdato);

        if ((harAdressebeskyttelse || ukjentGradering) && !harVeilederLeseTilgang) {
            return medlem
                    .setKjonn(kjonn)
                    .setGradering(gradering);
        } else if (erEgenAnsatt && !harVeilederLeseTilgang) {
            return medlem
                    .setKjonn(kjonn)
                    .setErEgenAnsatt(true)
                    .setHarSammeBosted(harSammeBosted);
        } else {
            return medlem
                    .setKjonn(kjonn)
                    .setGradering(gradering)
                    .setErEgenAnsatt(erEgenAnsatt)
                    .setHarVeilederTilgang(harVeilederLeseTilgang)
                    .setHarSammeBosted(harSammeBosted)
                    .setFornavn(ofNullable(navn).map(HentPerson.Navn::getFornavn).orElse(null))
                    .setMellomnavn(ofNullable(navn).map(HentPerson.Navn::getMellomnavn).orElse(null))
                    .setEtternavn(ofNullable(navn).map(HentPerson.Navn::getEtternavn).orElse(null))
                    .setForkortetNavn(ofNullable(navn).map(HentPerson.Navn::getForkortetNavn).orElse(null))
                    .setKjonn(ofNullable(getFirstElement(familiemedlem.getKjoenn())).map(HentPerson.Kjoenn::getKjoenn).orElse(null))
                    .setDodsdato(ofNullable(getFirstElement(familiemedlem.getDoedsfall())).map(HentPerson.Doedsfall::getDoedsdato).orElse(null));
        }
    }

    /* Sammeligner persons bostesadresse med familiemedlems bostedsadresse for å se om de har samme bosted */
    public static boolean harFamiliamedlemSammeBostedSomPerson(Bostedsadresse medlemsBostedsAdresse, Bostedsadresse personsBostedsAdresse) {
        Bostedsadresse.Vegadresse medlemsVegadresse = ofNullable(medlemsBostedsAdresse).map(Bostedsadresse::getVegadresse).orElse(null);
        Bostedsadresse.Matrikkeladresse medlemsMatrikkeladresse = ofNullable(medlemsBostedsAdresse).map(Bostedsadresse::getMatrikkeladresse).orElse(null);

        Bostedsadresse.Vegadresse personsVegadresse = ofNullable(personsBostedsAdresse).map(Bostedsadresse::getVegadresse).orElse(null);
        Bostedsadresse.Matrikkeladresse personsMatrikkeladresse = ofNullable(personsBostedsAdresse).map(Bostedsadresse::getMatrikkeladresse).orElse(null);

        if (personsVegadresse != null && medlemsVegadresse != null) {
            return Objects.equals(personsVegadresse, medlemsVegadresse);
        } else if (personsMatrikkeladresse != null && medlemsMatrikkeladresse != null) {
            return Objects.equals(personsMatrikkeladresse, medlemsMatrikkeladresse);
        }

        return false;
    }

    public static Sivilstand sivilstandMapper(HentPerson.Sivilstand sivilstand) {
        return (sivilstand != null)
               ? new Sivilstand()
                    .setSivilstand(sivilstand.getType())
                    .setFraDato(sivilstand.getGyldigFraOgMed())
               : null;
    }

    public static List<Telefon> mapTelefonNrFraPdl(List<HentPerson.Telefonnummer> telefonnummer) {
        return (!telefonnummer.isEmpty())
                ? telefonnummer.stream().map(PersonV2DataMapper::telefonNummerMapper).filter(Objects::nonNull).collect(Collectors.toList())
                : new ArrayList<>();
    }

    public static HentPerson.Metadata.Endringer filtrerGjelendeEndringsInfo(List<HentPerson.Metadata.Endringer> endringer) {
        return ofNullable(endringer)
                .stream()
                .flatMap(Collection::stream)
                .filter(endring -> endring.getType().equals("OPPRETT"))
                .findAny()
                .orElse(null);
    }

    /* Slår sammen landkode og nummer og så lager et telefonnummer */
    public static Telefon telefonNummerMapper(HentPerson.Telefonnummer telefonnummer) {
        String telefonnr = telefonnummer.getNummer();
        String master = telefonnummer.getMetadata().getMaster();
        String landkode = ofNullable(telefonnummer.getLandskode()).orElse("");
        HentPerson.Metadata.Endringer endringsInfo = filtrerGjelendeEndringsInfo(telefonnummer.getMetadata().getEndringer());
        String registrert = ofNullable(endringsInfo.getRegistrert())
                .map(dato -> LocalDateTime.parse(dato.toString(), ISO_LOCAL_DATE_TIME))
                .map(PersonV2DataMapper::formatererPaaNorskDato)
                .orElse(null);

        return (telefonnr!=null)
                 ? new Telefon()
                        .setPrioritet(telefonnummer.getPrioritet())
                        .setTelefonNr(landkode + telefonnr)
                        .setRegistrertDato(registrert)
                        .setMaster(master)
                 : null;
    }

    public static String formatererPaaNorskDato(LocalDateTime localDateTime) {
        return DateTimeFormatter.ofPattern("dd.MM.yyyy").format(localDateTime);
    }

    public static String parseDateFromDateTime(String sistOppdatert) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss,000+00:00");
        LocalDateTime dateTime = LocalDateTime.parse(sistOppdatert, dateTimeFormatter);
        return PersonV2DataMapper.formatererPaaNorskDato(dateTime);
    }
}
