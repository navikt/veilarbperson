package no.nav.veilarbperson.client.person.domain;

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
