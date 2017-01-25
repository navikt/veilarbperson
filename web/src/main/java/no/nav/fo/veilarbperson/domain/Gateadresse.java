package no.nav.fo.veilarbperson.domain;

public class Gateadresse {

    private String poststed;
    private String postnummer;
    private Integer husnummer;
    private String husbokstav;
    private String kommunenummer;
    private String gatenavn;
    private Integer withGatenummer;


    public String getPoststed() {
        return poststed;
    }


    public String getPostnummer() {
        return postnummer;
    }

    public Integer getHusnummer() {
        return husnummer;
    }

    public String getHusbokstav() {
        return husbokstav;
    }

    public String getKommunenummer() {
        return kommunenummer;
    }

    public String getGatenavn() {
        return gatenavn;
    }

    public Gateadresse withPoststed(String poststed) {
        this.poststed = poststed;
        return this;
    }

    public Gateadresse withPostnummer(String postnummer) {
        this.postnummer = postnummer;
        return this;
    }

    public Gateadresse withHusnummer(Integer husnummer) {
        this.husnummer = husnummer;
        return this;
    }

    public Gateadresse withHusbokstav(String husbokstav) {
        this.husbokstav = husbokstav;
        return this;
    }

    public Gateadresse withKommunenummer(String kommunenummer) {
        this.kommunenummer = kommunenummer;
        return this;
    }

    public Gateadresse withGatenavn(String gatenavn) {
        this.gatenavn = gatenavn;
        return this;
    }

    public Gateadresse withGatenummer(Integer gatenummer) {
        this.withGatenummer = gatenummer;
        return this;
    }
}