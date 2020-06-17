package no.nav.veilarbperson.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.veilarbperson.client.person.domain.*;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Data
@Accessors(chain = true)
public class PersonData {
    String fornavn;
    String mellomnavn;
    String etternavn;
    String sammensattNavn;
    String fodselsnummer;
    String fodselsdato;

    String kjonn;
    String dodsdato;
    
    List<Familiemedlem> barn;
    String diskresjonskode;
    String kontonummer;
    String geografiskTilknytning;
    Enhet geografiskEnhet;
    String telefon;
    String epost;
    String statsborgerskap;
    String sikkerhetstiltak;
    Sivilstand sivilstand;
    Familiemedlem partner;
    Bostedsadresse bostedsadresse;
    MidlertidigAdresseNorge midlertidigAdresseNorge;
    MidlertidigAdresseUtland midlertidigAdresseUtland;
    PostAdresse postAdresse;
    boolean egenAnsatt;
    String malform;

    @JsonIgnore
    public Optional<String> getPostnummerForBostedsadresse() {
        return ofNullable(bostedsadresse)
                .map(Bostedsadresse::getStrukturertAdresse)
                .map(StrukturertAdresse::getPostnummer);
    }

    public void setPoststedForBostedsadresse(String poststed) {
        ofNullable(bostedsadresse)
                .map(Bostedsadresse::getStrukturertAdresse)
                .ifPresent(strukturertAdresse -> strukturertAdresse.withPoststed(poststed));
    }

    @JsonIgnore
    public Optional<String> getPostnummerForMidlertidigAdresseNorge() {
        return ofNullable(midlertidigAdresseNorge)
                .map(MidlertidigAdresseNorge::getStrukturertAdresse)
                .map(StrukturertAdresse::getPostnummer);

    }

    public void setPoststedForMidlertidigAdresseNorge(String poststed) {
        ofNullable(midlertidigAdresseNorge)
                .map(MidlertidigAdresseNorge::getStrukturertAdresse)
                .ifPresent(strukturertAdresse -> strukturertAdresse.withPoststed(poststed));
    }
}