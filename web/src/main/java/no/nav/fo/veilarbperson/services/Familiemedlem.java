package no.nav.fo.veilarbperson.services;


public class Familiemedlem {

    private String fornavn;
    private String etternavn;
    private String sammensattNavn;
    private String personnummer;
    private Boolean harSammeBosted;

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

}


