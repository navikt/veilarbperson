package no.nav.fo.veilarbperson.services;

public class Enhet {
    private String enhetsnummer;
    private String navn;

    public String getEnhetsnummer() {
        return enhetsnummer;
    }

    public String getNavn() {
        return navn;
    }

    public Enhet withEnhetsnummer(String enhetsnummer) {
        this.enhetsnummer = enhetsnummer;
        return this;
    }

    public Enhet withNavn(String navn) {
        this.navn = navn;
        return this;
    }
}
