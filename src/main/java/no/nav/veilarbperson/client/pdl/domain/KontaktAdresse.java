package no.nav.veilarbperson.client.pdl.domain;

import lombok.Data;

@Data
public class KontaktAdresse extends Adresse {

    private String type;
    private Vegadresse vegadresse;
    private Utenlandskadresse utenlandskadresse;
    private Postboksadresse postboksadresse;
    private PostadresseIFrittFormat postadresseIFrittFormat;
    private UtenlandskAdresseIFrittFormat utenlandskAdresseIFrittFormat;


    @Data
    public static class Postboksadresse {
        private String postbokseier;
        private String postboks;
        private String postnummer;
    }

    @Data
    public static class PostadresseIFrittFormat {
        private String adresselinje1;
        private String adresselinje2;
        private String adresselinje3;
        private String postnummer;
    }

    @Data
    public static class UtenlandskAdresseIFrittFormat {
        private String adresselinje1;
        private String adresselinje2;
        private String adresselinje3;
        private String postkode;
        private String byEllerStednavn;
        private String landkode;

        public KontaktAdresse.UtenlandskAdresseIFrittFormat withLandkode(String landkode) {
            this.landkode = landkode;
            return this;
        }
    }

}
