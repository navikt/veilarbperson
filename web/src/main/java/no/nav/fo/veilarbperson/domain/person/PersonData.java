package no.nav.fo.veilarbperson.domain.person;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Builder;
import no.nav.fo.veilarbperson.consumer.organisasjonenhet.Enhet;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PersonData extends Person {

    private List<Familiemedlem> barn;
    private String diskresjonskode;
    private String kontonummer;
    private String ansvarligEnhetsnummer;
    private Enhet behandlendeEnhet;
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

    public boolean isEgenAnsatt() {
        return egenAnsatt;
    }

    @Builder
    private PersonData(String fornavn,
                       String mellomnavn,
                       String etternavn,
                       String sammensattnavn,
                       String fodselsnummer,
                       String fodselsdato,
                       String kjonn,
                       String dodsdato,
                       List<Familiemedlem> barn,
                       String diskresjonskode,
                       String kontonummer,
                       String ansvarligEnhetsnummer,
                       Enhet behandlendeEnhet,
                       String telefon,
                       String epost,
                       String statsborgerskap,
                       String sikkerhetstiltak,
                       Sivilstand sivilstand,
                       Familiemedlem partner,
                       Bostedsadresse bostedsadresse,
                       MidlertidigAdresseNorge midlertidigAdresseNorge,
                       MidlertidigAdresseUtland midlertidigAdresseUtland,
                       PostAdresse postAdresse,
                       boolean egenAnsatt
    ) {
        super(fornavn, mellomnavn, etternavn, sammensattnavn, fodselsnummer, fodselsdato, kjonn, dodsdato);

        this.barn = barn;
        this.diskresjonskode = diskresjonskode;
        this.kontonummer = kontonummer;
        this.ansvarligEnhetsnummer = ansvarligEnhetsnummer;
        this.behandlendeEnhet = behandlendeEnhet;
        this.telefon = telefon;
        this.epost = epost;
        this.statsborgerskap = statsborgerskap;
        this.sikkerhetstiltak = sikkerhetstiltak;
        this.sivilstand = sivilstand;
        this.partner = partner;
        this.bostedsadresse = bostedsadresse;
        this.midlertidigAdresseNorge = midlertidigAdresseNorge;
        this.midlertidigAdresseUtland = midlertidigAdresseUtland;
        this.postAdresse = postAdresse;
        this.egenAnsatt = egenAnsatt;
    }


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