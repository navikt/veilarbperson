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


    public PostboksadresseNorsk getPostboksadresseNorsk() {
        return postboksadresseNorsk;
    }


    public void withPostboksadresseNorsk(PostboksadresseNorsk postboksadresseNorsk) {
        this.postboksadresseNorsk = postboksadresseNorsk;
    }




}
