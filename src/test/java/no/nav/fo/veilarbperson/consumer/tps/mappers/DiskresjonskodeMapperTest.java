package no.nav.fo.veilarbperson.consumer.tps.mappers;

import no.nav.fo.veilarbperson.domain.person.Diskresjonskoder;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class DiskresjonskodeMapperTest {

    @Test
    public void strengtFortroligKodverkVerdiMappesTilTallVerdi() {
        String strengtFortrolig = Diskresjonskoder.STRENGT_FORTROLIG_ADRESSE.kodeverkVerdi;

        String tallkode = DiskresjonskodeMapper.mapTilTallkode(strengtFortrolig);

        assertThat(tallkode, is(Diskresjonskoder.STRENGT_FORTROLIG_ADRESSE.tallVerdi));
    }

    @Test
    public void fortroligKodverkVerdiMappesTilTallVerdi() {
        String fortrolig = Diskresjonskoder.FORTROLIG_ADRESSE.kodeverkVerdi;

        String tallkode = DiskresjonskodeMapper.mapTilTallkode(fortrolig);

        assertThat(tallkode, is(Diskresjonskoder.FORTROLIG_ADRESSE.tallVerdi));
    }

    @Test
    public void ingenDiskresjonskodeMappesTilNull() {
        String tallkode = DiskresjonskodeMapper.mapTilTallkode(null);

        assertThat(tallkode, is(nullValue()));
    }
}