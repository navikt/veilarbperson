package no.nav.fo.veilarbperson.consumer.kodeverk;

import no.nav.tjeneste.virksomhet.kodeverk.v2.HentKodeverkHentKodeverkKodeverkIkkeFunnet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KodeverkManagerTest {

    @Mock
    private KodeverkFetcher kodeverkFetcher;

    @InjectMocks
    private KodeverkManager kodeverkManager;

    @Test
    public void getBeskrivelseForKodeSkalReturnereTomOptionalDersomKodeverketIkkeEksisterer() throws Exception {
        when(kodeverkFetcher.hentKodeverk(any())).thenThrow(new HentKodeverkHentKodeverkKodeverkIkkeFunnet());

        final String beskrivelseForLandkode = kodeverkManager.getBeskrivelseForLandkode("NOR");

        assertThat(beskrivelseForLandkode, is("NOR"));
    }
}