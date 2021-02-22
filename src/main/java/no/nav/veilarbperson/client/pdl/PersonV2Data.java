package no.nav.veilarbperson.client.pdl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.pdl.domain.*;
import no.nav.veilarbperson.domain.Enhet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Data
@Accessors(chain = true)
public class PersonV2Data {
    String fornavn;
    String mellomnavn;
    String etternavn;
    String forkortetNavn;
    Fnr fodselsnummer;
    String fodselsdato;
    String kjonn;
    String dodsdato;

    String diskresjonskode;
    boolean egenAnsatt;
    String kontonummer;
    String geografiskTilknytning;
    Enhet geografiskEnhet;
    List<String> telefon;
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

    public PersonV2Data() {
        telefon = new ArrayList<>();
        barn = new ArrayList<>();
    }

    @JsonIgnore
    public Optional<String> getPostnummerFraBostedsadresse() {
        return ofNullable(bostedsadresse)
                .map(Bostedsadresse::getVegadresse)
                .map(Adresse.Vegadresse::getPostnummer);
    }

    public void setPoststedUnderBostedsAdresse(String poststed) {
        ofNullable(bostedsadresse)
                .map(Bostedsadresse::getVegadresse)
                .ifPresent(vegadresse -> vegadresse.withPoststed(poststed));
    }

    @JsonIgnore
    public Optional<String> getLandKodeFraKontaktadresse() {
        return ofNullable(midlertidigAdresseUtland)
                .map(Kontaktadresse.UtenlandskAdresseIFrittFormat::getLandkode);
    }

    public void setBeskrivelseForLandkodeIKontaktadresse(String landkode) {
        ofNullable(midlertidigAdresseUtland)
                .ifPresent(midlertidigAdresseUtland -> midlertidigAdresseUtland.withLandkode(landkode));
    }

}
