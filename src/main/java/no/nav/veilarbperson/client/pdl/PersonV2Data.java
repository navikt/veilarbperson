package no.nav.veilarbperson.client.pdl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.pdl.domain.*;
import no.nav.veilarbperson.domain.Enhet;
import java.util.List;
import static java.util.Optional.ofNullable;

@Data
@Accessors(chain = true)
public class PersonV2Data {
    String fornavn;
    String mellomnavn;
    String etternavn;
    String sammensattNavn;
    Fnr fodselsnummer;
    String fodselsdato;
    String kjonn;
    String dodsdato;

    String diskresjonskode;
    boolean egenAnsatt;
    String kontonummer;
    String geografiskTilknytning;
    Enhet geografiskEnhet;
    String telefon;
    String epost;
    String statsborgerskap;
    String sikkerhetstiltak;

    List<Familiemedlem> barn;
    Sivilstand sivilstand;
    Familiemedlem partner;
    Bostedsadresse bostedsadresse;
    Kontaktadresse.UtenlandskAdresseIFrittFormat midlertidigAdresseUtland;
    Kontaktadresse.PostadresseIFrittFormat postAdresse;
    String malform;

    @JsonIgnore
    public String getPostnummerFraBostedsadresse() {
        return ofNullable(bostedsadresse)
                .map(Bostedsadresse::getVegadresse)
                .map(Adresse.Vegadresse::getPostnummer).get();
    }

    public void setPoststedUnderBostedsAdresse(String poststed) {
        ofNullable(bostedsadresse)
                .map(Bostedsadresse::getVegadresse)
                .ifPresent(vegadresse -> vegadresse.withPoststed(poststed));
    }

    public String getLandKodeFraKontaktadresse() {
        return ofNullable(midlertidigAdresseUtland)
                .map(Kontaktadresse.UtenlandskAdresseIFrittFormat::getLandkode).get();
    }

    public void setBeskrivelseForLandkodeIKontaktadresse(String landkode) {
        ofNullable(midlertidigAdresseUtland)
                .ifPresent(midlertidigAdresseUtland -> midlertidigAdresseUtland.withLandkode(landkode));
    }

}
