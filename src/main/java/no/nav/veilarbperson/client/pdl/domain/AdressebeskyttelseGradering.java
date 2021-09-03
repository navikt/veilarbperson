package no.nav.veilarbperson.client.pdl.domain;

public enum AdressebeskyttelseGradering {
    UGRADERT,
    FORTROLIG,
    STRENGT_FORTROLIG,
    STRENGT_FORTROLIG_UTLAND;

    public static AdressebeskyttelseGradering mapGradering(String gradering) {
        switch (gradering) {
            case "UGRADERT":
                return UGRADERT;
            case "FORTROLIG":
                return FORTROLIG;
            case "STRENGT_FORTROLIG":
                return STRENGT_FORTROLIG;
            case "STRENGT_FORTROLIG_UTLAND":
                return STRENGT_FORTROLIG_UTLAND;
            default:
                return UGRADERT;
        }
    }
}
