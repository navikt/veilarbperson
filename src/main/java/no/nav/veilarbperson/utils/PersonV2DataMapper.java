package no.nav.veilarbperson.utils;

import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.pdl.HentPdlPerson;
import no.nav.veilarbperson.client.pdl.PersonV2Data;
import no.nav.veilarbperson.client.pdl.domain.*;
import no.nav.veilarbperson.domain.PersonData;
import java.util.List;
import static java.util.Optional.ofNullable;

public class PersonV2DataMapper {

    public static PersonV2Data toPersonV2Data(HentPdlPerson.PdlPerson pdlPerson, PersonData personDataFraTps) {

        return new PersonV2Data()
                .setFornavn(ofNullable(getFirstElement(pdlPerson.getNavn())).map(HentPdlPerson.Navn::getFornavn).orElse(null))
                .setMellomnavn(ofNullable(getFirstElement(pdlPerson.getNavn())).map(HentPdlPerson.Navn::getMellomnavn).orElse(null))
                .setEtternavn(ofNullable(getFirstElement(pdlPerson.getNavn())).map(HentPdlPerson.Navn::getEtternavn).orElse(null))
                .setSammensattNavn(ofNullable(getFirstElement(pdlPerson.getNavn())).map(HentPdlPerson.Navn::getForkortetnavn).orElse(null))
                .setKjonn(ofNullable(getFirstElement(pdlPerson.getKjoenn())).map(HentPdlPerson.Kjoenn::getKjoenn).orElse(null))
                .setFodselsdato(ofNullable(getFirstElement(pdlPerson.getFoedsel())).map(HentPdlPerson.Foedsel::getFoedselsdato).orElse(null))
                .setStatsborgerskap(ofNullable(getFirstElement(pdlPerson.getStatsborgerskap())).map(HentPdlPerson.Statsborgerskap::getLand).orElse(null))
                .setDodsdato(ofNullable(getFirstElement(pdlPerson.getDoedsfall())).map(HentPdlPerson.Doedsfall::getDoedsdato).orElse(null))
                .setFodselsnummer(ofNullable(getFirstElement(pdlPerson.getFolkeregisteridentifikator()))
                        .map(HentPdlPerson.Folkeregisteridentifikator::getIdentifikasjonsnummer)
                        .map(Fnr::of).orElse(null))
                .setTelefon(ofNullable(getFirstElement(pdlPerson.getTelefonnummer())).map(HentPdlPerson.Telefonnummer::getNummer).orElse(null))
                .setKontonummer(personDataFraTps.getKontonummer())
                .setDiskresjonskode(ofNullable(getFirstElement(pdlPerson.getAdressebeskyttelse()))
                        .map(HentPdlPerson.Adressebeskyttelse::getGradering)
                        .map(Diskresjonskoder::mapTilTallkode).orElse(null))
                .setSivilstand(ofNullable(sivilstandMapper(pdlPerson.getSivilstand())).orElse(null))
                .setBostedsadresse(ofNullable(getFirstElement(pdlPerson.getBostedsadresse())).orElse(null))
                .setMidlertidigAdresseUtland(ofNullable(getFirstElement(pdlPerson.getKontaktadresse())).map(Kontaktadresse::getUtenlandskAdresseIFrittFormat).orElse(null))
                .setPostAdresse(ofNullable(getFirstElement(pdlPerson.getKontaktadresse())).map(Kontaktadresse::getPostadresseIFrittFormat).orElse(null));
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
                .setForkortetnavn(ofNullable(navn).map(HentPdlPerson.Navn::getForkortetnavn).orElse(null));
    }

    public static Familiemedlem familiemedlemMapper(HentPdlPerson.Familiemedlem familiemedlem) {
        HentPdlPerson.Navn navn = getFirstElement(familiemedlem.getNavn());

        return new Familiemedlem()
                .setFornavn(ofNullable(navn).map(HentPdlPerson.Navn::getFornavn).orElse(null))
                .setMellomnavn(ofNullable(navn).map(HentPdlPerson.Navn::getMellomnavn).orElse(null))
                .setEtternavn(ofNullable(navn).map(HentPdlPerson.Navn::getEtternavn).orElse(null))
                .setSammensattNavn(ofNullable(navn).map(HentPdlPerson.Navn::getForkortetnavn).orElse(null))
                .setFodselsdato(ofNullable(getFirstElement(familiemedlem.getFoedsel())).map(HentPdlPerson.Foedsel::getFoedselsdato).orElse(null))
                .setFodselsnummer(ofNullable(getFirstElement(familiemedlem.getFolkeregisteridentifikator()))
                        .map(HentPdlPerson.Folkeregisteridentifikator::getIdentifikasjonsnummer).orElse(null))
                .setKjonn(ofNullable(getFirstElement(familiemedlem.getKjoenn())).map(HentPdlPerson.Kjoenn::getKjoenn).orElse(null))
                .setDodsdato(ofNullable(getFirstElement(familiemedlem.getDoedsfall())).map(HentPdlPerson.Doedsfall::getDoedsdato).orElse(null));
    }

    public static Sivilstand sivilstandMapper(List<HentPdlPerson.Sivilstand> sivilstand) {
             HentPdlPerson.Sivilstand pdlSivilstand = getFirstElement(sivilstand);

             return new Sivilstand()
                     .setSivilstand(ofNullable(pdlSivilstand).map(HentPdlPerson.Sivilstand::getType).orElse(null))
                     .setFraDato(ofNullable(pdlSivilstand).map(HentPdlPerson.Sivilstand::getGyldigFraOgMed).orElse(null));
    }

    public static void toPersonV2Data(HentPdlPerson.PdlPerson pdlPerson) {
    }
}
