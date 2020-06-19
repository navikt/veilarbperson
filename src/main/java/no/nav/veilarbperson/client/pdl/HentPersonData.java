package no.nav.veilarbperson.client.pdl;

import lombok.Data;

import java.util.List;

@Data
public class HentPersonData {
    PdlPerson hentPerson;

    @Data
    public static class PdlPerson {
        List<Navn> navn;
        List<Foedsel> foedsel;
        List<Doedsfall> doedsfall;
        List<Sivilstand> sivilstand;
    }

    @Data
    public static class Navn {
        String fornavn;
        String mellomnavn;
        String etternavn;
    }

    @Data
    public static class Foedsel {
        String foedselsdato;
    }

    @Data
    public static class Doedsfall {
        String doedsdato;
    }

    @Data
    public static class Sivilstand {
        String type;
        String gyldigFraOgMed;
    }
}
