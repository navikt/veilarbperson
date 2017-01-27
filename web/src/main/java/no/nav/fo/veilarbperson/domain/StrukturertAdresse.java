package no.nav.fo.veilarbperson.domain;

public abstract class StrukturertAdresse  {

    private String landkode;
    private String postnummer;
    private String poststed;

    public String getLandkode() {
        return landkode;
    }

    public String getPostnummer() {
        return postnummer;
    }

    public String getPoststed() {
        return poststed;
    }

    public StrukturertAdresse withLandkode(String landkode) {
        this.landkode = landkode;
        return this;
    }


    public StrukturertAdresse withPoststed(String poststed) {
        this.poststed = poststed;
        return this;
    }

    public StrukturertAdresse withPostnummer(String postnummer) {
        this.postnummer = postnummer;
        return this;
    }



}
