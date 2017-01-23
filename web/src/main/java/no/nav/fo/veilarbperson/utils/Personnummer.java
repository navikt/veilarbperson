package no.nav.fo.veilarbperson.utils;

public class Personnummer {

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
        return aar + "-" + maaned + "-" + dag;
    }

    private static String personnummerTilAarstall(String personnummer) {
        int aarsiffer = Integer.parseInt(personnummer.substring(4, 6));
        int individsiffer = Integer.parseInt(personnummer.substring(6, 9));
        int aarstall;
        if (individsiffer < 500) {
            aarstall = 1900 + aarsiffer;
        } else if (individsiffer < 750 && aarsiffer >= 54) {
            aarstall = 1800 + aarsiffer;
        } else if (individsiffer < 900 || aarsiffer < 40) {
            aarstall = 2000 + aarsiffer;
        } else {
            aarstall = 1900 + aarsiffer;
        }
        return Integer.toString(aarstall);
    }

    private static String personnummerTilMaaned(String personnummer) {
        int maanedsiffer = Integer.parseInt(personnummer.substring(2, 4));
        if (maanedsiffer > 12) {
            maanedsiffer -= 40;
        }
        return Integer.toString(maanedsiffer);
    }

    private static String personnummerTilDag(String personnummer) {
        int dagsiffer = Integer.parseInt(personnummer.substring(0, 2));
        if (dagsiffer > 31) {
            dagsiffer -= 40;
        }
        return Integer.toString(dagsiffer);
    }
}
