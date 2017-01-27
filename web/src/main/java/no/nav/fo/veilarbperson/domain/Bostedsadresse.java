package no.nav.fo.veilarbperson.domain;

public class Bostedsadresse {


    public StrukturertAdresse getStrukturertAdresse() {
        return strukturertAdresse;
    }

    public Bostedsadresse withStrukturertAdresse(StrukturertAdresse strukturertAdresse) {
        this.strukturertAdresse = strukturertAdresse;
        return this;
    }

    private StrukturertAdresse strukturertAdresse;
    private PostboksadresseNorsk postboksadresseNorsk;
    private Matrikkeladresse matrikkeladresse;


    public PostboksadresseNorsk getPostboksadresseNorsk() {
        return postboksadresseNorsk;
    }

    public Matrikkeladresse getMatrikkeladresse() {
        return matrikkeladresse;
    }


    public void withPostboksadresseNorsk(PostboksadresseNorsk postboksadresseNorsk) {
        this.postboksadresseNorsk = postboksadresseNorsk;
    }


    public Bostedsadresse withMatrikkeladresse(Matrikkeladresse matrikkeladresse) {
        this.matrikkeladresse = matrikkeladresse;
        return this;
    }


}
