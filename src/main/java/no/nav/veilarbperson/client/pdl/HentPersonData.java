package no.nav.veilarbperson.client.pdl;

public class HentPersonData {
    PdlPerson hentPerson;

    public static class PdlPerson {
        Navn navn;
        Foedsel foedsel;
        Doedsfall doedsfall;
        Sivilstand sivilstand;
    }

    public static class Navn {
        String fornavn;
        String mellomnavn;
        String etternavn;
    }

    public static class Foedsel {
        String foedselsdato;
    }

    public static class Doedsfall {
        String doedsdato;
    }

    public static class Sivilstand {
        String type;
        String gyldigFraOgMed;
    }
}
