package no.nav.veilarbperson.client.pdl.domain;

public enum  Diskresjonskoder {

    UGRADERT("UGRADERT", "0"),
    FORTROLIG("FORTROLIG", "7"),
    STRENGT_FORTROLIG("STRENGT_FORTROLIG", "6"),
    STRENGT_FORTROLIG_UTLAND("STRENGT_FORTROLIG_UTLAND", "19");

    public final String kode;
    public final String tallVerdi;

    Diskresjonskoder(String kode, String tallVerdi) {
        this.kode = kode;
        this.tallVerdi= tallVerdi;
    }

    public static String mapTilTallkode(String gradering) {
        if(UGRADERT.kode.equals(gradering)){
            return UGRADERT.tallVerdi;
        } else if (FORTROLIG.kode.equals(gradering)) {
            return FORTROLIG.tallVerdi;
        } else if (STRENGT_FORTROLIG.kode.equals(gradering)){
            return STRENGT_FORTROLIG.tallVerdi;
        } else if (STRENGT_FORTROLIG_UTLAND.kode.equals(gradering)) {
            return STRENGT_FORTROLIG_UTLAND.tallVerdi;
        } else {
            return null;
        }
    }
}
