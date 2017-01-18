package no.nav.fo.veilarbperson.domain;

public class Sivilstand {
    private String sivilstand;
    private String fraDato;

    public String getSivilstand() {
        return sivilstand;
    }

    public String getFraDato() {
        return fraDato;
    }

    public Sivilstand medSivilstand(String siviltilstand) {
        this.sivilstand = siviltilstand;
        return this;
    }

    public Sivilstand medFraDato(String fraDato) {
        this.fraDato = fraDato;
        return this;
    }
}
