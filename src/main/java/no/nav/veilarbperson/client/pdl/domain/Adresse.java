package no.nav.veilarbperson.client.pdl.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
public class Adresse {

    @Data
    @Accessors(chain = true)
    public static class Vegadresse {
        private Long matrikkelId;
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
