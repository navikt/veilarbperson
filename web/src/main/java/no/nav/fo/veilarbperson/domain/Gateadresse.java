package no.nav.fo.veilarbperson.domain;

public class Gateadresse {

    private String poststed;
    private String adressetype;
    private String postnummer;
    private Integer husnummer;
    private String husbokstav;
    private String kommunenummer;
    private String gatenavn;
    private String bolignummer;
    private Integer withGatenummer;


    public String getPoststed() {
        return poststed;
    }

    public String getAdressetype() {
        return adressetype;
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

    public String getBolignummer() {
        return bolignummer;
    }


    public Gateadresse withPoststed(String poststed) {
        this.poststed = poststed;
        return this;
    }

    public Gateadresse withType(String type) {
        this.adressetype = type;
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

    public Gateadresse withBolignummer(String bolignummer) {
        this.bolignummer = bolignummer;
        return this;
    }

    public Gateadresse withGatenummer(Integer gatenummer) {
        this.withGatenummer = gatenummer;
        return this;
    }
}
