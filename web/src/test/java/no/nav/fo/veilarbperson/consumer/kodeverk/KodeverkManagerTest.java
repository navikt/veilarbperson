package no.nav.fo.veilarbperson.consumer.kodeverk;

import no.nav.tjeneste.virksomhet.kodeverk.v2.HentKodeverkHentKodeverkKodeverkIkkeFunnet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KodeverkManagerTest {

    @Mock
    private KodeverkService kodeverkService;

    @InjectMocks
    private KodeverkManager kodeverkManager;

    @Test
    public void getBeskrivelseForKodeSkalReturnereTomOptionalDersomKodeverketIkkeEksisterer() throws Exception {
        when(kodeverkService.hentKodeverk(any())).thenThrow(new HentKodeverkHentKodeverkKodeverkIkkeFunnet());

        final Optional<String> beskrivelseForLandkode = kodeverkManager.getBeskrivelseForLandkode("");

        assertThat(beskrivelseForLandkode.isPresent(), is(false));
    }
}