package no.nav.fo.veilarbperson.domain;

public class Bostedsadresse {

    private Gateadresse gateadresse;
    private PostboksadresseNorsk postboksadresseNorsk;
    private Matrikkeladresse matrikkeladresse;
    private String landkode;

    public String getLandkode() {
        return landkode;
    }

    public Gateadresse getGateadresse() {
        return gateadresse;
    }

    public PostboksadresseNorsk getPostboksadresseNorsk() {
        return postboksadresseNorsk;
    }

    public Matrikkeladresse getMatrikkeladresse() {
        return matrikkeladresse;
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


    public Bostedsadresse withMatrikkeladresse(Matrikkeladresse matrikkeladresse) {
        this.matrikkeladresse = matrikkeladresse;
        return this;
    }



}
