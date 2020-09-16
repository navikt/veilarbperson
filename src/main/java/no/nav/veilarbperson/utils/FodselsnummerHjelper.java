package no.nav.veilarbperson.utils;

import no.nav.common.types.identer.Fnr;

public class FodselsnummerHjelper {

    public static final int INDIVIDSIFFER_1900_TIL_1999 = 500;
    public static final int INDIVIDSIFFER_1854_TIL_1899 = 750;
    public static final int INDIVIDSIFFER_1940_TIL_1999 = 900;
    public static final int SISTE_MAANED = 12;
    public static final int SISTE_DAG_I_MAANED = 31;
    public static final int D_OG_H_NUMMER_OFFSET = 40;

    public static String fodselsnummerTilKjonn(String fodselsnummer) {
        if (Integer.parseInt(fodselsnummer.substring(8, 9)) % 2 == 0) {
            return "K";
        } else {
            return "M";
        }
    }

    public static String fodselsnummerTilFodselsdato(Fnr fnr) {
        return fodselsnummerTilFodselsdato(fnr.get());
    }

    public static String fodselsnummerTilFodselsdato(String fodselsnummer) {
        Fnr fnr = Fnr.of(fodselsnummer);
        final String aar = fodselsnummerTilAarstall(fnr);
        final String maaned = fodselsnummerTilMaaned(fnr);
        final String dag = fodselsnummerTilDag(fnr);
        if (aar == null || maaned == null || dag == null) {
            return null;
        }
        return aar + "-" + maaned + "-" + dag;
    }

    private static String fodselsnummerTilAarstall(Fnr fodselsnummer) {
        int aarsiffer = Integer.parseInt(fodselsnummer.get().substring(4, 6));
        int individsiffer = Integer.parseInt(fodselsnummer.get().substring(6, 9));
        int aarstall;
        if (individsiffer < INDIVIDSIFFER_1900_TIL_1999) {
            aarstall = 1900 + aarsiffer;
        } else if (aarsiffer < 40) {
            aarstall = 2000 + aarsiffer;
        } else if (individsiffer < INDIVIDSIFFER_1854_TIL_1899 && aarsiffer >= 54) {
            aarstall = 1800 + aarsiffer;
        } else if (individsiffer >= INDIVIDSIFFER_1940_TIL_1999 && aarsiffer >= 40){
            aarstall = 1900 + aarsiffer;
        } else {
            return null;
        }
        return Integer.toString(aarstall);
    }

    private static String fodselsnummerTilMaaned(Fnr fodselsnummer) {
        int maanedsiffer = Integer.parseInt(fodselsnummer.get().substring(2, 4));
        if (maanedsiffer > SISTE_MAANED) {
            maanedsiffer -= D_OG_H_NUMMER_OFFSET;
        }
        return String.format("%02d", maanedsiffer);
    }

    private static String fodselsnummerTilDag(Fnr fodselsnummer) {
        int dagsiffer = Integer.parseInt(fodselsnummer.get().substring(0, 2));
        if (dagsiffer > SISTE_DAG_I_MAANED) {
            dagsiffer -= D_OG_H_NUMMER_OFFSET;
        }
        return String.format("%02d", dagsiffer);
    }
}
