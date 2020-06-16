package no.nav.veilarbperson.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.nav.veilarbperson.client.kodeverk.KodeverkClientImpl;
import no.nav.veilarbperson.utils.TestUtils;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.junit.Assert.assertEquals;

public class KodeverkClientImplTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    @Test
    public void skal_hente_beskrivelse_for_sivilstander() {
        String kodeverkJson = TestUtils.readTestResourceFile("kodeverk-sivilstander.json");
        String apiUrl = "http://localhost:" + wireMockRule.port();
        KodeverkClientImpl kodeverkClient = new KodeverkClientImpl(apiUrl);

        givenThat(get(anyUrl())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(kodeverkJson))
        );

        assertEquals("Registrert partner", kodeverkClient.getBeskrivelseForSivilstand("REPA"));
    }

}
