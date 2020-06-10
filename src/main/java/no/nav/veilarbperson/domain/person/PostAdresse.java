package no.nav.veilarbperson.domain.person;

public class PostAdresse {
    private UstrukturertAdresse ustrukturertAdresse;

    public UstrukturertAdresse getUstrukturertAdresse() {
        return ustrukturertAdresse;
    }

    public PostAdresse withUstrukturertAdresse(UstrukturertAdresse ustrukturertAdresse) {
        this.ustrukturertAdresse = ustrukturertAdresse;
        return this;
    }
}
