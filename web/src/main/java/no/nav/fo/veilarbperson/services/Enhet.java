package no.nav.fo.veilarbperson.services;

public class Enhet {
    public String enhetsnummer;
    public String navn;



    public String hentEnhetsnummer() {
        return enhetsnummer;
    }

    public void settEnhetsnummer(String enhetsnummer) {
        this.enhetsnummer = enhetsnummer;
    }

    public Enhet medEnhetsnummer(String enhetsnummer) {
        settEnhetsnummer(enhetsnummer);
        return this;
    }

    public String hentNavn() {
        return navn;
    }

    public void settNavn(String navn) {
        this.navn = navn;
    }

    public Enhet medNavn(String navn) {
        settNavn(navn);
        return this;
    }

}
