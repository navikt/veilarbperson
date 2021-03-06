package no.nav.veilarbperson.client.pdl.domain;

import lombok.Data;

@Data
public class Bostedsadresse extends Adresse {

    private Vegadresse vegadresse;
    private Matrikkeladresse matrikkeladresse;
    private Utenlandskadresse utenlandskAdresse;
    private UkjentBosted ukjentBosted;

    @Data
    public static class Matrikkeladresse {
        private Long matrikkelId;
        private String bruksenhetsnummer;
        private String tilleggsnavn;
        private String postnummer;
        private String poststed;
        private String kommunenummer;
        private String kommune;

        public Matrikkeladresse withPoststed(String poststed) {
            this.poststed = poststed;
            return this;
        }

        public Matrikkeladresse withKommune(String kommune) {
            this.kommune = kommune;
            return this;
        }
    }

    @Data
    public static class UkjentBosted {
        private String bostedskommune;
        private String kommune;

        public UkjentBosted withKommune(String kommune) {
            this.kommune = kommune;
            return this;
        }
    }

}
