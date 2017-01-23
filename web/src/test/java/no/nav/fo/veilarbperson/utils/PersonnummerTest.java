package no.nav.fo.veilarbperson.utils;

import org.junit.Test;

import static no.nav.fo.veilarbperson.utils.Personnummer.personnummerTilFodselsdato;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PersonnummerTest {

    public static final String PERSONNUMMER_2039 = "28023999899";
    public static final String PERSONNUMMER_2013 = "01011356733";
    public static final String PERSONNUMMER_2000 = "16030099978";
    public static final String PERSONNUMMER_1999 = "17059948768";
    public static final String PERSONNUMMER_1930 = "15083002678";
    public static final String PERSONNUMMER_1854 = "31125450019";

    public static final String D_NUMMER_2039 = "68023999882";
    public static final String D_NUMMER_2013 = "41011356727";
    public static final String D_NUMMER_2000 = "56030099961";
    public static final String D_NUMMER_1999 = "57059948751";
    public static final String D_NUMMER_1930 = "55083002823";
    public static final String D_NUMMER_1854 = "71125450002";

    public static final String H_NUMMER_2039 = "28423999871";
    public static final String H_NUMMER_2013 = "01411356716";
    public static final String H_NUMMER_2000 = "16430099950";
    public static final String H_NUMMER_1999 = "17459948902";
    public static final String H_NUMMER_1930 = "15483002812";
    public static final String H_NUMMER_1854 = "315254500101";

    public static final String FODSELSNUMMER_2039 = "2039-02-28";
    public static final String FODSELSNUMMER_2013 = "2013-01-01";
    public static final String FODSELSNUMMER_2000 = "2000-03-16";
    public static final String FODSELSNUMMER_1999 = "1999-05-17";
    public static final String FODSELSNUMMER_1930 = "1930-08-15";
    public static final String FODSELSNUMMER_1854 = "1854-12-31";
    
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
    public void fodtI1854() {
        assertThat(personnummerTilFodselsdato(PERSONNUMMER_1854), is(FODSELSNUMMER_1854));
        assertThat(personnummerTilFodselsdato(D_NUMMER_1854), is(FODSELSNUMMER_1854));
        assertThat(personnummerTilFodselsdato(H_NUMMER_1854), is(FODSELSNUMMER_1854));
    }
}
