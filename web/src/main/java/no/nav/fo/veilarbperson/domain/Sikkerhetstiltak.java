package no.nav.fo.veilarbperson.domain;

public class Sikkerhetstiltak {
    private String sikkerhetstiltaksbeskrivelse;

    public String getSikkerhetstiltaksbeskrivelse() {
        return sikkerhetstiltaksbeskrivelse;
    }

    public Sikkerhetstiltak medSikkerhetstiltaksbeskrivelse(String sikkerhetstiltaksbeskrivelse) {
        this.sikkerhetstiltaksbeskrivelse = sikkerhetstiltaksbeskrivelse;
        return this;
    }
}
