package no.nav.veilarbperson.utils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.bekk.bekkopen.person.FodselsnummerCalculator;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

@Slf4j
public class TestUtils {

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
        return incrementDigit(fnr, 0, 4);
    }

    public static String calculateHNumber(String fnr) {
        return incrementDigit(fnr, 2, 4);
    }

    @SneakyThrows
    public static String readTestResourceFile(String fileName) {
        URL fileUrl = TestUtils.class.getClassLoader().getResource(fileName);
        Path resPath = Paths.get(fileUrl.toURI());
        return new String(Files.readAllBytes(resPath), StandardCharsets.UTF_8);
    }

    private static String incrementDigit(String fnr, int index, int increment) {
        int digit = Integer.parseInt(fnr.substring(index, index + 1));
        digit = (digit + increment) % 10;

        return fnr.substring(0, index) + Integer.toString(digit) + fnr.substring(index + 1);
    }

}
