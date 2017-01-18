package no.nav.fo.veilarbperson.services;

import java.util.List;

public class PersonData{
    public String fornavn;
    public String mellomnavn;
    public String etternavn;
    public String sammensattNavn;
    public String personnummer;
    public String fodselsdato;
    public String kjoenn;
    public List<Barn> barn;
    public String diskresjonskode;
    public String kontonummer;
    public String sikkerhetstiltak;


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

    public PersonData medFodselsdato(String fodselsdato){
        this.fodselsdato = fodselsdato;
        return this;
    }

    public PersonData medKjoenn(String kjoenn){
        this.kjoenn = kjoenn;
        return this;
    }

    public PersonData medBarn(List<Barn> barn){
        this.barn = barn;
        return this;
    }

    public PersonData medDiskresjonskode(String diskresjonskode) {
        this.diskresjonskode = diskresjonskode;
        return this;
    }

    public PersonData medKontonummer(String kontonummer) {
        this.kontonummer = kontonummer;
        return this;
    }

    public PersonData medSikkerhetstiltak(String sikkerhetstiltak) {
        this.sikkerhetstiltak = sikkerhetstiltak;
        return this;
    }
}