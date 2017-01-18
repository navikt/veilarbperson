package no.nav.fo.veilarbperson.services;

public class Enhet {
    private String enhetsnummer;
    private String navn;

    public String getEnhetsnummer() {
        return enhetsnummer;
    }

    public void setEnhetsnummer(String enhetsnummer) {
        this.enhetsnummer = enhetsnummer;
    }

    public Enhet medEnhetsnummer(String enhetsnummer) {
        setEnhetsnummer(enhetsnummer);
        return this;
    }

    public String getNavn() {
        return navn;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public Enhet medNavn(String navn) {
        setNavn(navn);
        return this;
    }
}
