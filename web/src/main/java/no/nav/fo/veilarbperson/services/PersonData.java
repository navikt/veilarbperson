package no.nav.fo.veilarbperson.services;

import java.util.List;

public class PersonData{
    private String fornavn;
    private String mellomnavn;
    private String etternavn;
    private String sammensattNavn;
    private String personnummer;
    private String fodselsdato;
    private String kjoenn;
    private List<Barn> barn;
    private String diskresjonskode;
    private String kontonummer;
    private String telefon;
    private String epost;
    private String sikkerhetstiltak;

    public String getFornavn() {
        return fornavn;
    }

    public String getMellomnavn() {
        return mellomnavn;
    }

    public String getEtternavn() {
        return etternavn;
    }

    public String getSammensattNavn() {
        return sammensattNavn;
    }

    public String getPersonnummer() {
        return personnummer;
    }

    public String getFodselsdato() {
        return fodselsdato;
    }

    public String getKjoenn() {
        return kjoenn;
    }

    public List<Barn> getBarn() {
        return barn;
    }

    public String getDiskresjonskode() {
        return diskresjonskode;
    }

    public String getKontonummer() {
        return kontonummer;
    }

    public String getTelefon() {
        return telefon;
    }

    public String getEpost() {
        return epost;
    }

    public String getSikkerhetstiltak() {
        return sikkerhetstiltak;
    }

    public PersonData withFornavn(String fornavn){
        this.fornavn = fornavn;
        return this;
    }

    public PersonData withMellomnavn(String mellomnavn){
        this.mellomnavn = mellomnavn;
        return this;
    }

    public PersonData withEtternavn(String etternavn){
        this.etternavn = etternavn;
        return this;
    }

    public PersonData withSammensattNavn(String sammensattNavn) {
        this.sammensattNavn = sammensattNavn;
        return this;
    }

    public PersonData withPersonnummer(String personnummer){
        this.personnummer = personnummer;
        return this;
    }

    public PersonData withFodselsdato(String fodselsdato){
        this.fodselsdato = fodselsdato;
        return this;
    }

    public PersonData withKjoenn(String kjoenn){
        this.kjoenn = kjoenn;
        return this;
    }

    public PersonData withBarn(List<Barn> barn){
        this.barn = barn;
        return this;
    }

    public PersonData withDiskresjonskode(String diskresjonskode) {
        this.diskresjonskode = diskresjonskode;
        return this;
    }

    public PersonData withKontonummer(String kontonummer) {
        this.kontonummer = kontonummer;
        return this;
    }

    public PersonData withSikkerhetstiltak(String sikkerhetstiltak) {
        this.sikkerhetstiltak = sikkerhetstiltak;
        return this;
    }

    public PersonData withTelefon(String telefon) {
        this.telefon = telefon;
        return this;
    }

    public PersonData withEpost(String epost) {
        this.epost = epost;
        return this;
    }
}