package no.nav.fo.veilarbperson.services;


public class Barn {

    public String fornavn;
    public String etternavn;
    public String sammensattNavn;
    public String personnummer;
    public Boolean harSammeBosted;

    public Barn medFornavn(String fornavn){
        this.fornavn = fornavn;
        return this;
    }

    public Barn medEtternavn(String etternavn){
        this.etternavn = etternavn;
        return this;
    }

    public Barn medSammensattnavn(String sammensattNavn){
        this.sammensattNavn = sammensattNavn;
        return this;
    }



    public Barn medHarSammeBosted(Boolean borHjemme){
        this.harSammeBosted = borHjemme;
        return this;
    }

    public Barn medPersonnummer(String personnummer){
        this.personnummer = personnummer;
        return this;
    }

}


