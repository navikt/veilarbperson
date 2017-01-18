package no.nav.fo.veilarbperson.digitalkontaktinformasjon;

public class DigitalKontaktinformasjon {

    private String epost;
    private String telefon;

    public DigitalKontaktinformasjon medEpost(String epost) {
        this.epost = epost;
        return this;
    }

    public DigitalKontaktinformasjon medTelefon(String telefon) {
        this.telefon = telefon;
        return this;
    }

    public String getEpost() {
        return epost;
    }

    public String getTelefon() {
        return telefon;
    }

}
