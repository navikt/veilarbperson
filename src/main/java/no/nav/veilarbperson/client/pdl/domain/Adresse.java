package no.nav.veilarbperson.client.pdl.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Adresse {

    @Data
    public static class Vegadresse {
        private String postnummer;
        private String husnummer;
        private String husbokstav;
        private String kommunenummer;
        private String adressenavn;
        private String tilleggsnavn;
        private String poststed;

        public Vegadresse withPoststed(String poststed) {
            this.poststed = poststed;
            return this;
        }
    }

    @Data
    public static class UtenlandskAdresse {
        private String adressenavnNummer;
        private String bygningEtasjeLeilighet;
        private String postboksNummerNavn;
        private String postkode;
        private String bySted;
        private String regionDistriktOmraade;
        private String landkode;
    }

}
