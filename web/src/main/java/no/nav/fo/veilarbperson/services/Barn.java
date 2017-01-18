package no.nav.fo.veilarbperson.services;


public class Barn {

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

    public Barn withFornavn(String fornavn){
        this.fornavn = fornavn;
        return this;
    }

    public Barn withEtternavn(String etternavn){
        this.etternavn = etternavn;
        return this;
    }

    public Barn withSammensattnavn(String sammensattNavn){
        this.sammensattNavn = sammensattNavn;
        return this;
    }


    public Barn withHarSammeBosted(Boolean borHjemme){
        this.harSammeBosted = borHjemme;
        return this;
    }

    public Barn withPersonnummer(String personnummer){
        this.personnummer = personnummer;
        return this;
    }

}


