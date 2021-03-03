package no.nav.veilarbperson.client.pdl.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
public class Adresse {

     String gyldigFraOgMed;
     String gyldigTilOgMed;
     String coAdressenavn;
     Metadata metadata;

    @Data
    @Accessors(chain = true)
    public static class Vegadresse {
        private Long matrikkelId;
        private String postnummer;
        private String husnummer;
        private String husbokstav;
        private String adressenavn;
        private String tilleggsnavn;
        private String poststed;
        private String kommunenummer;
        private String kommune;

        public Vegadresse withPoststed(String poststed) {
            this.poststed = poststed;
            return this;
        }

        public Vegadresse withKommune(String kommune) {
            this.kommune = kommune;
            return this;
        }
    }

    @Data
    public static class Utenlandskadresse {
        private String adressenavnNummer;
        private String bygningEtasjeLeilighet;
        private String postboksNummerNavn;
        private String postkode;
        private String bySted;
        private String regionDistriktOmraade;
        private String landkode;

        public Utenlandskadresse withLandkode(String landkode) {
            this.landkode = landkode;
            return this;
        }
    }

}
