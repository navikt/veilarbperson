package no.nav.veilarbperson.client.person.domain;

public class Bostedsadresse {

    private StrukturertAdresse strukturertAdresse;

    public StrukturertAdresse getStrukturertAdresse() {
        return strukturertAdresse;
    }

    public Bostedsadresse withStrukturertAdresse(StrukturertAdresse strukturertAdresse) {
        this.strukturertAdresse = strukturertAdresse;
        return this;
    }

    public Bostedsadresse copy() {
        return new Bostedsadresse().withStrukturertAdresse(strukturertAdresse.copy());
    }
}
