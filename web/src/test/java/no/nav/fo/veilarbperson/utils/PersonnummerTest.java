package no.nav.fo.veilarbperson.utils;

import org.junit.Test;

import static no.nav.fo.veilarbperson.utils.Personnummer.personnummerTilFodselsdato;
import static no.nav.fo.veilarbperson.utils.Personnummer.personnummerTilKjoenn;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class PersonnummerTest {

    private static final String PERSONNUMMER_2039 = "28023999899";
    private static final String PERSONNUMMER_2013 = "01011356733";
    private static final String PERSONNUMMER_2000 = "16030099978";
    private static final String PERSONNUMMER_1999 = "17059948768";
    private static final String PERSONNUMMER_1930 = "15083002678";
    private static final String PERSONNUMMER_1940 = "15084092678";
    private static final String PERSONNUMMER_1854 = "31125450019";

    private static final String D_NUMMER_2039 = "68023999882";
    private static final String D_NUMMER_2013 = "41011356727";
    private static final String D_NUMMER_2000 = "56030099961";
    private static final String D_NUMMER_1999 = "57059948751";
    private static final String D_NUMMER_1930 = "55083002823";
    private static final String D_NUMMER_1940 = "55084092823";
    private static final String D_NUMMER_1854 = "71125450002";

    private static final String H_NUMMER_2039 = "28423999871";
    private static final String H_NUMMER_2013 = "01411356716";
    private static final String H_NUMMER_2000 = "16430099950";
    private static final String H_NUMMER_1999 = "17459948902";
    private static final String H_NUMMER_1930 = "15483002812";
    private static final String H_NUMMER_1940 = "15484092812";
    private static final String H_NUMMER_1854 = "315254500101";

    private static final String FODSELSNUMMER_2039 = "2039-02-28";
    private static final String FODSELSNUMMER_2013 = "2013-01-01";
    private static final String FODSELSNUMMER_2000 = "2000-03-16";
    private static final String FODSELSNUMMER_1999 = "1999-05-17";
    private static final String FODSELSNUMMER_1930 = "1930-08-15";
    private static final String FODSELSNUMMER_1940 = "1940-08-15";
    private static final String FODSELSNUMMER_1854 = "1854-12-31";

    private static final String UGYLDIG_PERSONNUMMER_1 = "01025075087";
    private static final String UGYLDIG_PERSONNUMMER_2 = "01014055033";

    @Test
    public void fodtI2039() {
        assertThat(personnummerTilFodselsdato(PERSONNUMMER_2039), is(FODSELSNUMMER_2039));
        assertThat(personnummerTilFodselsdato(D_NUMMER_2039), is(FODSELSNUMMER_2039));
        assertThat(personnummerTilFodselsdato(H_NUMMER_2039), is(FODSELSNUMMER_2039));
    }

    @Test
    public void fodtI2013() {
        assertThat(personnummerTilFodselsdato(PERSONNUMMER_2013), is(FODSELSNUMMER_2013));
        assertThat(personnummerTilFodselsdato(D_NUMMER_2013), is(FODSELSNUMMER_2013));
        assertThat(personnummerTilFodselsdato(H_NUMMER_2013), is(FODSELSNUMMER_2013));
    }

    @Test
    public void fodtI2000() {
        assertThat(personnummerTilFodselsdato(PERSONNUMMER_2000), is(FODSELSNUMMER_2000));
        assertThat(personnummerTilFodselsdato(D_NUMMER_2000), is(FODSELSNUMMER_2000));
        assertThat(personnummerTilFodselsdato(H_NUMMER_2000), is(FODSELSNUMMER_2000));
    }

    @Test
    public void fodtI1999() {
        assertThat(personnummerTilFodselsdato(PERSONNUMMER_1999), is(FODSELSNUMMER_1999));
        assertThat(personnummerTilFodselsdato(D_NUMMER_1999), is(FODSELSNUMMER_1999));
        assertThat(personnummerTilFodselsdato(H_NUMMER_1999), is(FODSELSNUMMER_1999));
    }

    @Test
    public void fodtI1930() {
        assertThat(personnummerTilFodselsdato(PERSONNUMMER_1930), is(FODSELSNUMMER_1930));
        assertThat(personnummerTilFodselsdato(D_NUMMER_1930), is(FODSELSNUMMER_1930));
        assertThat(personnummerTilFodselsdato(H_NUMMER_1930), is(FODSELSNUMMER_1930));
    }

    @Test
    public void fodtI1940() {
        assertThat(personnummerTilFodselsdato(PERSONNUMMER_1940), is(FODSELSNUMMER_1940));
        assertThat(personnummerTilFodselsdato(D_NUMMER_1940), is(FODSELSNUMMER_1940));
        assertThat(personnummerTilFodselsdato(H_NUMMER_1940), is(FODSELSNUMMER_1940));
    }

    @Test
    public void fodtI1854() {
        assertThat(personnummerTilFodselsdato(PERSONNUMMER_1854), is(FODSELSNUMMER_1854));
        assertThat(personnummerTilFodselsdato(D_NUMMER_1854), is(FODSELSNUMMER_1854));
        assertThat(personnummerTilFodselsdato(H_NUMMER_1854), is(FODSELSNUMMER_1854));
    }

    @Test
    public void ugyldigPersonnummerFeiler() {
        assertNull(personnummerTilFodselsdato(UGYLDIG_PERSONNUMMER_1));
        assertNull(personnummerTilFodselsdato(UGYLDIG_PERSONNUMMER_2));
    }

    @Test
    public void personNummerTilKjoennSkalOversetteMannRiktig() throws Exception {
        final String kjonn = personnummerTilKjoenn("10108000398");

        assertThat(kjonn, is("M"));
    }

    @Test
    public void personNummerTilKjoennSkalOversetteKvinneRiktig() throws Exception {
        final String kjonn = personnummerTilKjoenn("10108000498");

        assertThat(kjonn, is("K"));
    }
}
