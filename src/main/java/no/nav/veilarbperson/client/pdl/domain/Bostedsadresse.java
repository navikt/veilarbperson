package no.nav.veilarbperson.client.pdl.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
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

        public void withPoststed(String poststed) {
            this.poststed = poststed;
        }

        public void withKommune(String kommune) {
            this.kommune = kommune;
        }
    }

    @Data
    public static class UkjentBosted {
        private String bostedskommune;
        private String kommune;

        public void withKommune(String kommune) {
            this.kommune = kommune;
        }
    }

}
