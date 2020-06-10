package no.nav.veilarbperson.domain.person;

public class MidlertidigAdresseNorge {
    private StrukturertAdresse strukturertAdresse;

    public StrukturertAdresse getStrukturertAdresse() {
        return strukturertAdresse;
    }

    public MidlertidigAdresseNorge withStrukturertAdresse(StrukturertAdresse strukturertAdresse) {
        this.strukturertAdresse = strukturertAdresse;
        return this;
    }

}
