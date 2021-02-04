package no.nav.veilarbperson.client.pdl.domain;

import lombok.Data;

@Data
public class Bostedsadresse extends Adresse {

    private Vegadresse vegadresse;
    private UtenlandskAdresse utenlandskAdresse;
    private Matrikkeladresse matrikkeladresse;
    private UkjentBosted ukjentBosted;

    @Data
    public static class Matrikkeladresse {
        private Long matrikkelId;
        private String bruksenhetsnummer;
        private String tilleggsnavn;
        private String postnummer;
        private String kommunenummer;
    }

    @Data
    public static class UkjentBosted {
        private String bostedskommune;
    }

}
