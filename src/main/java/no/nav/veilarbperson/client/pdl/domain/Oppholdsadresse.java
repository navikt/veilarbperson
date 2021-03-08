package no.nav.veilarbperson.client.pdl.domain;

import lombok.Data;

@Data
public class Oppholdsadresse extends Adresse {

    private String oppholdAnnetSted;
    private Vegadresse vegadresse;
    private Matrikkeladresse matrikkeladresse;
    private Utenlandskadresse utenlandskAdresse;

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
}
