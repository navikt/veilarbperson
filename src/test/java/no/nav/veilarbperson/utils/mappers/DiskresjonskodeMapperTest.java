package no.nav.veilarbperson.utils.mappers;

import no.nav.veilarbperson.domain.person.Diskresjonskoder;
import no.nav.veilarbperson.utils.Mappers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class DiskresjonskodeMapperTest {

    @Test
    public void strengtFortroligKodverkVerdiMappesTilTallVerdi() {
        String strengtFortrolig = Diskresjonskoder.STRENGT_FORTROLIG_ADRESSE.kodeverkVerdi;

        String tallkode = Mappers.mapTilTallkode(strengtFortrolig);

        assertThat(tallkode, is(Diskresjonskoder.STRENGT_FORTROLIG_ADRESSE.tallVerdi));
    }

    @Test
    public void fortroligKodverkVerdiMappesTilTallVerdi() {
        String fortrolig = Diskresjonskoder.FORTROLIG_ADRESSE.kodeverkVerdi;

        String tallkode = Mappers.mapTilTallkode(fortrolig);

        assertThat(tallkode, is(Diskresjonskoder.FORTROLIG_ADRESSE.tallVerdi));
    }

    @Test
    public void ingenDiskresjonskodeMappesTilNull() {
        String tallkode = Mappers.mapTilTallkode(null);

        assertThat(tallkode, is(nullValue()));
    }
}