package no.nav.veilarbperson.domain;

public class Feilmelding {
    private final String brukervennligFeilmelding;
    private final String tekniskArsak;

    public Feilmelding(String brukervennligFeilmelding, String tekniskArsak) {
        this.brukervennligFeilmelding = brukervennligFeilmelding;
        this.tekniskArsak = tekniskArsak;
    }

    public String getBrukervennligFeilmelding() {
        return brukervennligFeilmelding;
    }

    public String getTekniskArsak() {
        return tekniskArsak;
    }
}
