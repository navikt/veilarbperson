package no.nav.fo.veilarbperson.domain;


import com.fasterxml.jackson.annotation.JsonProperty;

public class Person {

    private String fornavn;
    private String etternavn;
    private String sammensattNavn;
    private String personnummer;
    private String fodselsdato;

    @JsonProperty("kjonn")
    private String kjoenn;
    private Boolean harSammeBosted;
    private String dodsdato;


    public String getKjoenn() {
        return kjoenn;
    }

    public void setKjoenn(String kjoenn) {
        this.kjoenn = kjoenn;
    }

    public Boolean getHarSammeBosted() {
        return harSammeBosted;
    }

    public void setHarSammeBosted(Boolean harSammeBosted) {
        this.harSammeBosted = harSammeBosted;
    }

    public String getDodsdato() {
        return dodsdato;
    }

    public void setDodsdato(String dodsdato) {
        this.dodsdato = dodsdato;
    }

    public String getFornavn() {
        return fornavn;
    }

    public void setFornavn(String fornavn) {
        this.fornavn = fornavn;
    }

    public String getEtternavn() {
        return etternavn;
    }

    public void setEtternavn(String etternavn) {
        this.etternavn = etternavn;
    }

    public String getSammensattNavn() {
        return sammensattNavn;
    }

    public void setSammensattNavn(String sammensattNavn) {
        this.sammensattNavn = sammensattNavn;
    }

    public String getPersonnummer() {
        return personnummer;
    }

    public void setPersonnummer(String personnummer) {
        this.personnummer = personnummer;
    }

    public String getFodselsdato() {
        return fodselsdato;
    }

    public void setFodselsdato(String fodselsdato) {
        this.fodselsdato = fodselsdato;
    }

}
