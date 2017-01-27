package no.nav.fo.veilarbperson.domain;

public class Gateadresse extends StrukturertAdresse{

    private Integer husnummer;
    private String husbokstav;
    private String kommunenummer;
    private String gatenavn;
    private Integer withGatenummer;

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
