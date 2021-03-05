package no.nav.veilarbperson.utils;

import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.pdl.HentPdlPerson;
import no.nav.veilarbperson.client.pdl.PersonV2Data;
import no.nav.veilarbperson.client.pdl.VergemaalEllerFullmaktConstants;
import no.nav.veilarbperson.client.pdl.domain.*;
import no.nav.veilarbperson.domain.PersonData;
import no.nav.veilarbperson.domain.Telefon;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

public class PersonV2DataMapper {

    public static PersonV2Data toPersonV2Data(HentPdlPerson.PdlPerson pdlPerson, PersonData personDataFraTps) {

        return new PersonV2Data()
                .setFornavn(ofNullable(getFirstElement(pdlPerson.getNavn())).map(HentPdlPerson.Navn::getFornavn).orElse(null))
                .setMellomnavn(ofNullable(getFirstElement(pdlPerson.getNavn())).map(HentPdlPerson.Navn::getMellomnavn).orElse(null))
                .setEtternavn(ofNullable(getFirstElement(pdlPerson.getNavn())).map(HentPdlPerson.Navn::getEtternavn).orElse(null))
                .setForkortetNavn(ofNullable(getFirstElement(pdlPerson.getNavn())).map(HentPdlPerson.Navn::getForkortetNavn).orElse(null))
                .setKjonn(ofNullable(getFirstElement(pdlPerson.getKjoenn())).map(HentPdlPerson.Kjoenn::getKjoenn).orElse(null))
                .setFodselsdato(ofNullable(getFirstElement(pdlPerson.getFoedsel())).map(HentPdlPerson.Foedsel::getFoedselsdato).orElse(null))
                .setStatsborgerskap(ofNullable(getFirstElement(pdlPerson.getStatsborgerskap())).map(HentPdlPerson.Statsborgerskap::getLand).orElse(null))
                .setDodsdato(ofNullable(getFirstElement(pdlPerson.getDoedsfall())).map(HentPdlPerson.Doedsfall::getDoedsdato).orElse(null))
                .setFodselsnummer(ofNullable(getFirstElement(pdlPerson.getFolkeregisteridentifikator()))
                        .map(HentPdlPerson.Folkeregisteridentifikator::getIdentifikasjonsnummer)
                        .map(Fnr::of).orElse(null))
                .setKontonummer(personDataFraTps.getKontonummer())
                .setDiskresjonskode(ofNullable(getFirstElement(pdlPerson.getAdressebeskyttelse()))
                        .map(HentPdlPerson.Adressebeskyttelse::getGradering)
                        .map(Diskresjonskoder::mapTilTallkode).orElse(null))
                .setTelefon(mapTelefonNrFraPdl(pdlPerson.getTelefonnummer()))
                .setSivilstand(ofNullable(sivilstandMapper(getFirstElement(pdlPerson.getSivilstand()))).orElse(null))
                .setBostedsadresse(ofNullable(getFirstElement(pdlPerson.getBostedsadresse())).orElse(null))
                .setOppholdsadresse(ofNullable(getFirstElement(pdlPerson.getOppholdsadresse())).orElse(null))
                .setKontaktadresser(ofNullable(pdlPerson.getKontaktadresse()).orElse(null))
                .setVergemaalEllerFremtidsfullmakt(vergemaalMapper(pdlPerson.getVergemaalEllerFremtidsfullmakt()))
                .setFullmakt(pdlPerson.getFullmakt());
    }

    public static <T> T getFirstElement(List<T> list) {
        return list.stream().findFirst().orElse(null);
    }

    public static HentPdlPerson.Navn hentNavn(List<HentPdlPerson.Navn> personNavn) {
        HentPdlPerson.Navn navn = getFirstElement(personNavn);

        return new HentPdlPerson.Navn()
                .setFornavn(ofNullable(navn).map(HentPdlPerson.Navn::getFornavn).orElse(null))
                .setMellomnavn(ofNullable(navn).map(HentPdlPerson.Navn::getMellomnavn).orElse(null))
                .setEtternavn(ofNullable(navn).map(HentPdlPerson.Navn::getEtternavn).orElse(null))
                .setForkortetNavn(ofNullable(navn).map(HentPdlPerson.Navn::getForkortetNavn).orElse(null));
    }

    public static Familiemedlem familiemedlemMapper(HentPdlPerson.Familiemedlem familiemedlem, Bostedsadresse personsBostedsadresse) {
        HentPdlPerson.Navn navn = getFirstElement(familiemedlem.getNavn());

        return new Familiemedlem()
                .setFornavn(ofNullable(navn).map(HentPdlPerson.Navn::getFornavn).orElse(null))
                .setMellomnavn(ofNullable(navn).map(HentPdlPerson.Navn::getMellomnavn).orElse(null))
                .setEtternavn(ofNullable(navn).map(HentPdlPerson.Navn::getEtternavn).orElse(null))
                .setForkortetNavn(ofNullable(navn).map(HentPdlPerson.Navn::getForkortetNavn).orElse(null))
                .setFodselsdato(ofNullable(getFirstElement(familiemedlem.getFoedsel())).map(HentPdlPerson.Foedsel::getFoedselsdato).orElse(null))
                .setFodselsnummer(ofNullable(getFirstElement(familiemedlem.getFolkeregisteridentifikator()))
                        .map(HentPdlPerson.Folkeregisteridentifikator::getIdentifikasjonsnummer).orElse(null))
                .setKjonn(ofNullable(getFirstElement(familiemedlem.getKjoenn())).map(HentPdlPerson.Kjoenn::getKjoenn).orElse(null))
                .setDodsdato(ofNullable(getFirstElement(familiemedlem.getDoedsfall())).map(HentPdlPerson.Doedsfall::getDoedsdato).orElse(null))
                .setHarSammeBosted(harFamiliamedlemSammeBostedSomPerson(getFirstElement(familiemedlem.getBostedsadresse()), personsBostedsadresse));
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

    public static Sivilstand sivilstandMapper(HentPdlPerson.Sivilstand sivilstand) {
        return (sivilstand != null)
               ? new Sivilstand()
                    .setSivilstand(sivilstand.getType())
                    .setFraDato(sivilstand.getGyldigFraOgMed())
               : null;
    }

    public static List<Telefon> mapTelefonNrFraPdl(List<HentPdlPerson.Telefonnummer> telefonnummer) {
        return (!telefonnummer.isEmpty())
                ? telefonnummer.stream().map(PersonV2DataMapper::telefonNummerMapper).filter(Objects::nonNull).collect(Collectors.toList())
                : new ArrayList<>();
    }

    /* Slår sammen landkode og nummer og så lager et telefonnummer */
    public static Telefon telefonNummerMapper(HentPdlPerson.Telefonnummer telefonnummer) {
         String telefonnr = telefonnummer.getNummer();
         String landkode = telefonnummer.getLandskode()!=null ?  telefonnummer.getLandskode() : "";

         return (telefonnr!=null)
                 ? new Telefon()
                        .setPrioritet(telefonnummer.getPrioritet())
                        .setTelefonNr(landkode + telefonnr)
                        .setMaster(telefonnummer.getMetadata().getMaster())
                 : null;
    }

    public static List<HentPdlPerson.VergemaalEllerFremtidsfullmakt> vergemaalMapper(List<HentPdlPerson.VergemaalEllerFremtidsfullmakt> vergemaalEllerFremtidsfullmaktListe) {
        if (!vergemaalEllerFremtidsfullmaktListe.isEmpty()) {
            for (HentPdlPerson.VergemaalEllerFremtidsfullmakt vergemaalEllerFremtidsfullmakt : vergemaalEllerFremtidsfullmaktListe) {
                String saksType = VergemaalEllerFullmaktConstants.saksType.get(vergemaalEllerFremtidsfullmakt.getType());
                String omfangType = VergemaalEllerFullmaktConstants.omfangType.get(vergemaalEllerFremtidsfullmakt.getVergeEllerFullmektig().getOmfang());

                vergemaalEllerFremtidsfullmakt.setType(saksType);
                vergemaalEllerFremtidsfullmakt.getVergeEllerFullmektig().setOmfang(omfangType);
            }
        }
        return vergemaalEllerFremtidsfullmaktListe;
    }
}
