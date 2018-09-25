package no.nav.fo.veilarbperson;

import lombok.extern.slf4j.Slf4j;
import no.bekk.bekkopen.person.FodselsnummerCalculator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class TestUtil {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public static String fodselsnummerForDato(String dato) {
        String fodselsnummer = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        try {
            Date date = dateFormat.parse(dato);
            fodselsnummer = FodselsnummerCalculator.getFodselsnummerForDate(date).toString();
        } catch (ParseException e) {
            log.error("Error parsing date for fodselsnummer");
            e.printStackTrace();
        }

        return fodselsnummer;
    }

    public static String calculateDNummer(String fnr) {
        int firstDigit = Integer.parseInt(fnr.substring(0, 1));
        firstDigit = (firstDigit + 4) % 10;
        return Integer.toString(firstDigit) + fnr.substring(1);
    }

    public static String calculateHNumber(String fnr) {
        int digit = Integer.parseInt(fnr.substring(2, 3));
        digit = (digit + 4) % 10;
        return fnr.substring(0, 2) + Integer.toString(digit) + fnr.substring(3);
    }

}
