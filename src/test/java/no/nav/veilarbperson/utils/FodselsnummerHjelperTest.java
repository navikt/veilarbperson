package no.nav.veilarbperson.utils;

import no.nav.common.types.identer.Fnr;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class FodselsnummerHjelperTest {

    private static final String FODSELSDATO_2039 = "2039-02-28";
    private static final String FODSELSDATO_2013 = "2013-01-01";
    private static final String FODSELSDATO_2000 = "2000-03-16";
    private static final String FODSELSDATO_1999 = "1999-05-17";
    private static final String FODSELSDATO_1930 = "1930-08-15";
    private static final String FODSELSDATO_1940 = "1940-08-15";
    private static final String FODSELSDATO_1854 = "1854-12-31";

    private static final Fnr FODSELSNUMMER_2039 = TestUtils.fodselsnummerForDato(FODSELSDATO_2039);
    private static final Fnr FODSELSNUMMER_2013 = TestUtils.fodselsnummerForDato(FODSELSDATO_2013);
    private static final Fnr FODSELSNUMMER_2000 = TestUtils.fodselsnummerForDato(FODSELSDATO_2000);
    private static final Fnr FODSELSNUMMER_1999 = TestUtils.fodselsnummerForDato(FODSELSDATO_1999);
    private static final Fnr FODSELSNUMMER_1930 = TestUtils.fodselsnummerForDato(FODSELSDATO_1930);
    private static final Fnr FODSELSNUMMER_1940 = TestUtils.fodselsnummerForDato(FODSELSDATO_1940);
    private static final Fnr FODSELSNUMMER_1854 = TestUtils.fodselsnummerForDato(FODSELSDATO_1854);

    private static final String D_NUMMER_2039 = TestUtils.calculateDNummer(FODSELSNUMMER_2039);
    private static final String D_NUMMER_2013 = TestUtils.calculateDNummer(FODSELSNUMMER_2013);
    private static final String D_NUMMER_2000 = TestUtils.calculateDNummer(FODSELSNUMMER_2000);
    private static final String D_NUMMER_1999 = TestUtils.calculateDNummer(FODSELSNUMMER_1999);
    private static final String D_NUMMER_1930 = TestUtils.calculateDNummer(FODSELSNUMMER_1930);
    private static final String D_NUMMER_1940 = TestUtils.calculateDNummer(FODSELSNUMMER_1940);
    private static final String D_NUMMER_1854 = TestUtils.calculateDNummer(FODSELSNUMMER_1854);

    private static final String H_NUMMER_2039 = TestUtils.calculateHNumber(FODSELSNUMMER_2039);
    private static final String H_NUMMER_2013 = TestUtils.calculateHNumber(FODSELSNUMMER_2013);
    private static final String H_NUMMER_2000 = TestUtils.calculateHNumber(FODSELSNUMMER_2000);
    private static final String H_NUMMER_1999 = TestUtils.calculateHNumber(FODSELSNUMMER_1999);
    private static final String H_NUMMER_1930 = TestUtils.calculateHNumber(FODSELSNUMMER_1930);
    private static final String H_NUMMER_1940 = TestUtils.calculateHNumber(FODSELSNUMMER_1940);
    private static final String H_NUMMER_1854 = TestUtils.calculateHNumber(FODSELSNUMMER_1854);

    private static final String UGYLDIG_FODSELSNUMMER_1 = "010250750XX";
    private static final String UGYLDIG_FODSELSNUMMER_2 = "010140550XX";

    @Test
    public void fodtI2039() {
        assertThat(FodselsnummerHjelper.fodselsnummerTilFodselsdato(FODSELSNUMMER_2039), is(FODSELSDATO_2039));
        assertThat(FodselsnummerHjelper.fodselsnummerTilFodselsdato(D_NUMMER_2039), is(FODSELSDATO_2039));
        assertThat(FodselsnummerHjelper.fodselsnummerTilFodselsdato(H_NUMMER_2039), is(FODSELSDATO_2039));
    }

    @Test
    public void fodtI2013() {
        assertThat(FodselsnummerHjelper.fodselsnummerTilFodselsdato(FODSELSNUMMER_2013), is(FODSELSDATO_2013));
        assertThat(FodselsnummerHjelper.fodselsnummerTilFodselsdato(D_NUMMER_2013), is(FODSELSDATO_2013));
        assertThat(FodselsnummerHjelper.fodselsnummerTilFodselsdato(H_NUMMER_2013), is(FODSELSDATO_2013));
    }

    @Test
    public void fodtI2000() {
        assertThat(FodselsnummerHjelper.fodselsnummerTilFodselsdato(FODSELSNUMMER_2000), is(FODSELSDATO_2000));
        assertThat(FodselsnummerHjelper.fodselsnummerTilFodselsdato(D_NUMMER_2000), is(FODSELSDATO_2000));
        assertThat(FodselsnummerHjelper.fodselsnummerTilFodselsdato(H_NUMMER_2000), is(FODSELSDATO_2000));
    }

    @Test
    public void fodtI1999() {
        assertThat(FodselsnummerHjelper.fodselsnummerTilFodselsdato(FODSELSNUMMER_1999), is(FODSELSDATO_1999));
        assertThat(FodselsnummerHjelper.fodselsnummerTilFodselsdato(D_NUMMER_1999), is(FODSELSDATO_1999));
        assertThat(FodselsnummerHjelper.fodselsnummerTilFodselsdato(H_NUMMER_1999), is(FODSELSDATO_1999));
    }

    @Test
    public void fodtI1930() {
        assertThat(FodselsnummerHjelper.fodselsnummerTilFodselsdato(FODSELSNUMMER_1930), is(FODSELSDATO_1930));
        assertThat(FodselsnummerHjelper.fodselsnummerTilFodselsdato(D_NUMMER_1930), is(FODSELSDATO_1930));
        assertThat(FodselsnummerHjelper.fodselsnummerTilFodselsdato(H_NUMMER_1930), is(FODSELSDATO_1930));
    }

    @Test
    public void fodtI1940() {
        assertThat(FodselsnummerHjelper.fodselsnummerTilFodselsdato(FODSELSNUMMER_1940), is(FODSELSDATO_1940));
        assertThat(FodselsnummerHjelper.fodselsnummerTilFodselsdato(D_NUMMER_1940), is(FODSELSDATO_1940));
        assertThat(FodselsnummerHjelper.fodselsnummerTilFodselsdato(H_NUMMER_1940), is(FODSELSDATO_1940));
    }

    @Test
    public void fodtI1854() {
        assertThat(FodselsnummerHjelper.fodselsnummerTilFodselsdato(FODSELSNUMMER_1854), is(FODSELSDATO_1854));
        assertThat(FodselsnummerHjelper.fodselsnummerTilFodselsdato(D_NUMMER_1854), is(FODSELSDATO_1854));
        assertThat(FodselsnummerHjelper.fodselsnummerTilFodselsdato(H_NUMMER_1854), is(FODSELSDATO_1854));
    }

    @Test
    public void ugyldigFodselsnummerFeiler() {
        assertNull(FodselsnummerHjelper.fodselsnummerTilFodselsdato(UGYLDIG_FODSELSNUMMER_1));
        assertNull(FodselsnummerHjelper.fodselsnummerTilFodselsdato(UGYLDIG_FODSELSNUMMER_2));
    }

    @Test
    public void fodselsnummerTilKjoennSkalOversetteMannRiktig() {
        final String kjonn = FodselsnummerHjelper.fodselsnummerTilKjonn("XXXXXXXX3XX");

        assertThat(kjonn, is("M"));
    }

    @Test
    public void fodselsnummerTilKjoennSkalOversetteKvinneRiktig() {
        final String kjonn = FodselsnummerHjelper.fodselsnummerTilKjonn("XXXXXXXX4XX");

        assertThat(kjonn, is("K"));
    }
}
