package no.nav.veilarbperson.client.organisasjonenhet;

public class Enhet {
    private String enhetsnummer;
    private String navn;

    public String getEnhetsnummer() {
        return enhetsnummer;
    }

    public String getNavn() {
        return navn;
    }

    Enhet withEnhetsnummer(String enhetsnummer) {
        this.enhetsnummer = enhetsnummer;
        return this;
    }

    Enhet withNavn(String navn) {
        this.navn = navn;
        return this;
    }
}
