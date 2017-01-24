package no.nav.fo.veilarbperson.domain;

public class Bostedsadresse {

    Gateadresse gateadresse;
    PostboksadresseNorsk postboksadresseNorsk;
    String landkode;
    public String getLandkode() {
        return landkode;
    }

    public Gateadresse getGateadresse() {
        return gateadresse;
    }

    public PostboksadresseNorsk getPostboksadresseNorsk() {
        return postboksadresseNorsk;
    }

    public Bostedsadresse withLandkode(String landkode) {
        this.landkode = landkode;
        return this;
    }

    public Bostedsadresse withGateadresse(Gateadresse gateadresse) {
        this.gateadresse = gateadresse;
        return this;
    }

    public void withPostboksadresseNorsk(PostboksadresseNorsk postboksadresseNorsk) {
        this.postboksadresseNorsk = postboksadresseNorsk;
    }

}
