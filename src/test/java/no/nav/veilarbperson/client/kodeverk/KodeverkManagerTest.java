package no.nav.veilarbperson.client.kodeverk;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KodeverkManagerTest {

    @Mock
    private KodeverkService kodeverkService;

    @InjectMocks
    private KodeverkManager kodeverkManager;

    @Test
    public void getBeskrivelseForKodeSkalReturnereKodeDersomKodeverketIkkeEksisterer() throws Exception {
        final String NOR = "NOR";
        when(kodeverkService.getVerdi(anyString(), anyString(), anyString())).thenReturn(NOR);

        final String beskrivelseForLandkode = kodeverkManager.getBeskrivelseForLandkode(NOR);

        assertThat(beskrivelseForLandkode, is(NOR));
    }
}