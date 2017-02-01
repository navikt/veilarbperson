package no.nav.fo.veilarbperson.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Familiemedlem {

    private String fornavn;
    private String etternavn;
    private String sammensattNavn;
    private String personnummer;
    private String fodselsdato;

    @JsonProperty("kjonn")
    private String kjoenn;
    private Boolean harSammeBosted;
    private String dodsdato;

    public String getFornavn() {
        return fornavn;
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

    public String getDodsdato() {
        return dodsdato;
    }

    public String getKjoenn() {
        return kjoenn;
    }

    public Boolean getHarSammeBosted() {
        return harSammeBosted;
    }

    public Familiemedlem withFornavn(String fornavn){
        this.fornavn = fornavn;
        return this;
    }

    public Familiemedlem withEtternavn(String etternavn){
        this.etternavn = etternavn;
        return this;
    }

    public Familiemedlem withSammensattnavn(String sammensattNavn){
        this.sammensattNavn = sammensattNavn;
        return this;
    }


    public Familiemedlem withHarSammeBosted(Boolean borHjemme){
        this.harSammeBosted = borHjemme;
        return this;
    }

    public Familiemedlem withPersonnummer(String personnummer){
        this.personnummer = personnummer;
        return this;
    }

    public Familiemedlem withFodselsdato(String fodselsdato){
        this.fodselsdato = fodselsdato;
        return this;
    }

    public Familiemedlem withKjoenn(String kjoenn){
        this.kjoenn = kjoenn;
        return this;
    }

    Familiemedlem withDodsdato(String dodsdato) {
        this.dodsdato = dodsdato;
        return this;
    }
}