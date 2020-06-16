package no.nav.veilarbperson.client.person.domain;

public class Sivilstand {
    private String sivilstand;
    private String fraDato;

    public String getSivilstand() {
        return sivilstand;
    }

    public String getFraDato() {
        return fraDato;
    }

    public Sivilstand withSivilstand(String siviltilstand) {
        this.sivilstand = siviltilstand;
        return this;
    }

    public Sivilstand withFraDato(String fraDato) {
        this.fraDato = fraDato;
        return this;
    }
}
