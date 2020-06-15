package no.nav.veilarbperson.domain.person;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PersonData extends Person {

    private List<Familiemedlem> barn;
    private String diskresjonskode;
    private String kontonummer;
    private String geografiskTilknytning;
    private Enhet geografiskEnhet;
    private String telefon;
    private String epost;
    private String statsborgerskap;
    private String sikkerhetstiltak;
    private Sivilstand sivilstand;
    private Familiemedlem partner;
    private Bostedsadresse bostedsadresse;
    private MidlertidigAdresseNorge midlertidigAdresseNorge;
    private MidlertidigAdresseUtland midlertidigAdresseUtland;
    private PostAdresse postAdresse;
    private boolean egenAnsatt;
    private String malform;

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