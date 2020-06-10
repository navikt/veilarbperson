package no.nav.veilarbperson.domain.person;

public class MidlertidigAdresseUtland {
    private UstrukturertAdresse ustrukturertAdresse;

    public UstrukturertAdresse getUstrukturertAdresse() {
        return ustrukturertAdresse;
    }

    public MidlertidigAdresseUtland withUstrukturertAdresse(UstrukturertAdresse ustrukturertAdresse) {
        this.ustrukturertAdresse = ustrukturertAdresse;
        return this;
    }
}
