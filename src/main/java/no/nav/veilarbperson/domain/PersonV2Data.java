package no.nav.veilarbperson.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.pdl.domain.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;

@Data
@Accessors(chain = true)
public class PersonV2Data {
    String fornavn;
    String mellomnavn;
    String etternavn;
    String forkortetNavn;
    Fnr fodselsnummer;
    LocalDate fodselsdato;
    String kjonn;
    LocalDate dodsdato;
    String diskresjonskode;
    boolean egenAnsatt;
    String geografiskTilknytning;
    Enhet geografiskEnhet;
    List<Telefon> telefon;
    Epost epost;
    List<String> statsborgerskapKoder;
    List<String> statsborgerskap;
    String sikkerhetstiltak;
    List<Familiemedlem> barn;
    List<Sivilstand> sivilstandliste;
    Bostedsadresse bostedsadresse;
    Oppholdsadresse oppholdsadresse;
    List<Kontaktadresse> kontaktadresser;
    String malform;

    public PersonV2Data() {
        telefon = new ArrayList<>();
        barn = new ArrayList<>();
        kontaktadresser = new ArrayList<>();
        statsborgerskap = new ArrayList<>();
    }

    public void setPoststedIBostedsVegadresse(String poststed) {
        ofNullable(bostedsadresse)
                .map(Bostedsadresse::getVegadresse)
                .ifPresent(vegadresse -> vegadresse.withPoststed(poststed));
    }

    public void setPoststedIBostedsMatrikkeladresse(String poststed) {
        ofNullable(bostedsadresse)
                .map(Bostedsadresse::getMatrikkeladresse)
                .ifPresent(matrikkeladresse -> matrikkeladresse.withPoststed(poststed));
    }

    public void setKommuneIBostedsVegadresse(String kommunenummer) {
        ofNullable(bostedsadresse)
                .map(Bostedsadresse::getVegadresse)
                .ifPresent(vegadresse -> vegadresse.withKommune(kommunenummer));
    }

    public void setKommuneIBostedsMatrikkeladresse(String kommunenummer) {
        ofNullable(bostedsadresse)
                .map(Bostedsadresse::getMatrikkeladresse)
                .ifPresent(matrikkeladresse -> matrikkeladresse.withKommune(kommunenummer));
    }

    public void setKommuneIBostedsUkjentadresse(String kommunenummer) {
        ofNullable(bostedsadresse)
                .map(Bostedsadresse::getUkjentBosted)
                .ifPresent(ukjentBosted -> ukjentBosted.withKommune(kommunenummer));
    }

    public void setKommuneIOppholdssVegadresse(String kommunenummer) {
        ofNullable(oppholdsadresse)
                .map(Oppholdsadresse::getVegadresse)
                .ifPresent(vegadresse -> vegadresse.withKommune(kommunenummer));
    }

    public void setKommuneIOppholdsMatrikkeladresse(String kommunenummer) {
        ofNullable(oppholdsadresse)
                .map(Oppholdsadresse::getMatrikkeladresse)
                .ifPresent(matrikkeladresse -> matrikkeladresse.withKommune(kommunenummer));
    }

    public void setLandkodeIBostedsUtenlandskadresse(String landkode) {
        ofNullable(bostedsadresse)
                .map(Bostedsadresse::getUtenlandskAdresse)
                .ifPresent(utenlandskadresse -> utenlandskadresse.withLandkode(landkode));
    }

    public void setPoststedIOppholdsVegadresse(String poststed) {
        ofNullable(oppholdsadresse)
                .map(Oppholdsadresse::getVegadresse)
                .ifPresent(vegadresse -> vegadresse.withPoststed(poststed));
    }

    public void setPoststedIOppholdsMatrikkeladresse(String poststed) {
        ofNullable(oppholdsadresse)
                .map(Oppholdsadresse::getMatrikkeladresse)
                .ifPresent(matrikkeladresse -> matrikkeladresse.withPoststed(poststed));
    }

    public void setLandkodeIOppholdsUtenlandskadresse(String landkode) {
        ofNullable(oppholdsadresse)
                .map(Oppholdsadresse::getUtenlandskAdresse)
                .ifPresent(utenlandskadresse -> utenlandskadresse.withLandkode(landkode));
    }
}
