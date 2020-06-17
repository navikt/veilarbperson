package no.nav.veilarbperson.client.person.domain;

public class PostboksadresseNorsk extends StrukturertAdresse {

    private String postnummer;
    private String postboksanlegg;
    private String postboksnummer;

    public String getPostnummer() {
        return postnummer;
    }

    public String getPostboksanlegg() {
        return postboksanlegg;
    }

    public String getPostboksnummer() {
        return postboksnummer;
    }

    public PostboksadresseNorsk withPostnummer(String postnummer) {
        this.postnummer = postnummer;
        return this;
    }

    public PostboksadresseNorsk withPostboksanlegg(String postboksanlegg) {
        this.postboksanlegg = postboksanlegg;
        return this;
    }

    public PostboksadresseNorsk withPostboksnummer(String postboksnummer) {
        this.postboksnummer = postboksnummer;
        return this;
    }

    @Override
    public PostboksadresseNorsk copy() {
        return (PostboksadresseNorsk) new PostboksadresseNorsk()
                .withPostnummer(postnummer)
                .withPostboksanlegg(postboksanlegg)
                .withPostboksnummer(postboksnummer)
                .withLandkode(getLandkode())
                .withTilleggsadresse(getTilleggsadresse())
                .withPostnummer(getPostnummer())
                .withPoststed(getPoststed());
    }
}
