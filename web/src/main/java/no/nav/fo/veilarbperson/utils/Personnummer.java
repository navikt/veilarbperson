package no.nav.fo.veilarbperson.utils;

public class Personnummer {

    public static final int INDIVIDNUMMER_1900_TIL_1999 = 500;
    public static final int INDIVIDNUMMER_1854_TIL_1899 = 750;
    public static final int INDIVIDSIFFER_1940_TIL_1999 = 900;

    public static String personnummerTilKjoenn(String personnummer) {
        if (Integer.parseInt(personnummer.substring(8, 9)) % 2 == 0) {
            return "K";
        } else {
            return "M";
        }
    }

    public static String personnummerTilFodselsdato(String personnummer) {
        final String aar = personnummerTilAarstall(personnummer);
        final String maaned = personnummerTilMaaned(personnummer);
        final String dag = personnummerTilDag(personnummer);
        if (aar == null || maaned == null || dag == null) {
            return null;
        }
        return aar + "-" + maaned + "-" + dag;
    }

    private static String personnummerTilAarstall(String personnummer) {
        int aarsiffer = Integer.parseInt(personnummer.substring(4, 6));
        int individsiffer = Integer.parseInt(personnummer.substring(6, 9));
        int aarstall;
        if (individsiffer < INDIVIDNUMMER_1900_TIL_1999) {
            aarstall = 1900 + aarsiffer;
        } else if (aarsiffer < 40) {
            aarstall = 2000 + aarsiffer;
        } else if (individsiffer < INDIVIDNUMMER_1854_TIL_1899 && aarsiffer >= 54) {
            aarstall = 1800 + aarsiffer;
        } else if (individsiffer >= INDIVIDSIFFER_1940_TIL_1999 && aarsiffer >= 40){
            aarstall = 1900 + aarsiffer;
        } else {
            return null;
        }
        return Integer.toString(aarstall);
    }

    private static String personnummerTilMaaned(String personnummer) {
        int maanedsiffer = Integer.parseInt(personnummer.substring(2, 4));
        if (maanedsiffer > 12) {
            maanedsiffer -= 40;
        }
        return String.format("%02d", maanedsiffer);
    }

    private static String personnummerTilDag(String personnummer) {
        int dagsiffer = Integer.parseInt(personnummer.substring(0, 2));
        if (dagsiffer > 31) {
            dagsiffer -= 40;
        }
        return String.format("%02d", dagsiffer);
    }
}
