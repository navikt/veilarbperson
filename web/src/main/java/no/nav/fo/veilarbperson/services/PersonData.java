package no.nav.fo.veilarbperson.services;

public class PersonData{
    public String fornavn;
    public String mellomnavn;
    public String etternavn;
    public String sammensattNavn;
    public String personnummer;

    public PersonData medFornavn(String fornavn){
        this.fornavn = fornavn;
        return this;
    }

    public PersonData medMellomnavn(String mellomnavn){
        this.mellomnavn = mellomnavn;
        return this;
    }

    public PersonData medEtternavn(String etternavn){
        this.etternavn = etternavn;
        return this;
    }

    public PersonData medSammensattNavn(String sammensattNavn) {
        this.sammensattNavn = sammensattNavn;
        return this;
    }

    public PersonData medPersonnummer(String personnummer){
        this.personnummer = personnummer;
        return this;
    }

}