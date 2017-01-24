package no.nav.fo.veilarbperson.domain;

public class Bostedsadresse {

    Gateadresse gateadresse;
    String landkode;
    public String getLandkode() {
        return landkode;
    }

    public Gateadresse getGateadresse() {
        return gateadresse;
    }

    public Bostedsadresse withLandkode(String landkode) {
        this.landkode = landkode;
        return this;
    }

    public Bostedsadresse withGateadresse(Gateadresse gateadresse) {
        this.gateadresse = gateadresse;
        return this;
    }

}
