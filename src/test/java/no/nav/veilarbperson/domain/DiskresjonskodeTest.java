package no.nav.veilarbperson.domain;

import no.nav.veilarbperson.client.pdl.domain.Diskresjonskode;
import org.junit.Test;

import static no.nav.veilarbperson.client.pdl.domain.Diskresjonskode.FORTROLIG;
import static no.nav.veilarbperson.client.pdl.domain.Diskresjonskode.STRENGT_FORTROLIG;
import static no.nav.veilarbperson.client.pdl.domain.Diskresjonskode.STRENGT_FORTROLIG_UTLAND;
import static org.junit.Assert.assertEquals;

public class DiskresjonskodeTest {
    @Test
    public void fraTall__mapperRiktigFraTallTilDiskresjonskode(){
        assertEquals(FORTROLIG, Diskresjonskode.fraTall("7"));
        assertEquals(STRENGT_FORTROLIG, Diskresjonskode.fraTall("6"));
        assertEquals(STRENGT_FORTROLIG_UTLAND, Diskresjonskode.fraTall("19"));
    }
}
