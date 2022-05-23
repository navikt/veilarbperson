package no.nav.veilarbperson.client.pdl.domain;

public enum AdressebeskyttelseGradering {
    UKJENT,
    UGRADERT,
    FORTROLIG,
    STRENGT_FORTROLIG,
    STRENGT_FORTROLIG_UTLAND;

    public static AdressebeskyttelseGradering mapGradering(String gradering) {
        if (gradering == null) {
            return UGRADERT;
        } else {
            try {
                return AdressebeskyttelseGradering.valueOf(gradering);
            } catch (IllegalArgumentException e) {
                return UKJENT;
            }
        }
    }
}
