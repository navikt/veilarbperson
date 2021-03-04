package no.nav.veilarbperson.client.pdl.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
public class Kontaktadresse extends Adresse {

    private String type;
    private Vegadresse vegadresse;
    private Utenlandskadresse utenlandskAdresse;
    private Postboksadresse postboksadresse;
    private PostadresseIFrittFormat postadresseIFrittFormat;
    private UtenlandskAdresseIFrittFormat utenlandskAdresseIFrittFormat;

    @Data
    public static class Postboksadresse {
        private String postbokseier;
        private String postboks;
        private String postnummer;
        private String poststed;

        public Postboksadresse withPoststed(String poststed) {
            this.poststed = poststed;
            return this;
        }
    }

    @Data
    public static class PostadresseIFrittFormat {
        private String adresselinje1;
        private String adresselinje2;
        private String adresselinje3;
        private String postnummer;
        private String poststed;

        public PostadresseIFrittFormat withPoststed(String poststed) {
            this.poststed = poststed;
            return this;
        }
    }

    @Data
    @Accessors(chain = true)
    public static class UtenlandskAdresseIFrittFormat {
        private String adresselinje1;
        private String adresselinje2;
        private String adresselinje3;
        private String postkode;
        private String byEllerStedsnavn;
        private String landkode;

        public UtenlandskAdresseIFrittFormat withLandkode(String landkode) {
            this.landkode = landkode;
            return this;
        }
    }

}
