package no.nav.veilarbperson.utils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.bekk.bekkopen.person.FodselsnummerCalculator;
import no.nav.common.types.identer.Fnr;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class TestUtils {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @SneakyThrows
    public static Fnr fodselsnummerForDato(String dato) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Date date = dateFormat.parse(dato);
        String fnrStr = FodselsnummerCalculator.getFodselsnummerForDate(date).toString();
        return Fnr.of(fnrStr);
    }

    public static String calculateDNummer(Fnr fnr) {
        return incrementDigit(fnr, 0, 4);
    }

    public static String calculateHNumber(Fnr fnr) {
        return incrementDigit(fnr, 2, 4);
    }

    @SneakyThrows
    public static String readTestResourceFile(String fileName) {
        URL fileUrl = TestUtils.class.getClassLoader().getResource(fileName);
        Path resPath = Paths.get(fileUrl.toURI());
        return Files.readString(resPath);
    }

    private static String incrementDigit(Fnr fnr, int index, int increment) {
        String fnrStr = fnr.get();
        int digit = Integer.parseInt(fnrStr.substring(index, index + 1));
        digit = (digit + increment) % 10;

        return fnrStr.substring(0, index) + Integer.toString(digit) + fnrStr.substring(index + 1);
    }

}
