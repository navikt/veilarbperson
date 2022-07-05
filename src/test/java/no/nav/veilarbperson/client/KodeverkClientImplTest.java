package no.nav.veilarbperson.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.nav.veilarbperson.client.kodeverk.KodeverkClientImpl;
import no.nav.veilarbperson.utils.TestUtils;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static no.nav.veilarbperson.service.KodeverkService.*;
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

        Map<String, String> kodeverkBeskrivelser = kodeverkClient.hentKodeverkBeskrivelser(KODEVERK_SIVILSTANDER);
        assertEquals("Registrert partner", kodeverkBeskrivelser.get("REPA"));
    }

    @Test
    public void skal_hente_beskrivelse_for_landkoder() {
        String kodeverkJson = TestUtils.readTestResourceFile("kodeverk-landkoder.json");
        String apiUrl = "http://localhost:" + wireMockRule.port();
        KodeverkClientImpl kodeverkClient = new KodeverkClientImpl(apiUrl);

        givenThat(get(anyUrl())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(kodeverkJson))
        );

        Map<String, String> kodeverkBeskrivelser = kodeverkClient.hentKodeverkBeskrivelser(KODEVERK_LANDKODER);
        assertEquals("BULGARIA", kodeverkBeskrivelser.get("BGR"));
        assertEquals("JUGOSLAVIA", kodeverkBeskrivelser.get("YUG"));
    }

    @Test
    public void skal_hente_beskrivelse_for_postnummer() {
        String kodeverkJson = TestUtils.readTestResourceFile("kodeverk-postnummer.json");
        String apiUrl = "http://localhost:" + wireMockRule.port();
        KodeverkClientImpl kodeverkClient = new KodeverkClientImpl(apiUrl);

        givenThat(get(anyUrl())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(kodeverkJson))
        );

        Map<String, String> kodeverkBeskrivelser = kodeverkClient.hentKodeverkBeskrivelser(KODEVERK_POSTNUMMER);
        assertEquals("SUNDEBRU", kodeverkBeskrivelser.get("4971"));
    }

    @Test
    public void testFindingMostRecentValue() {
        String kodeverkJson = TestUtils.readTestResourceFile("kodeverk-spraak.json");
        String apiUrl = "http://localhost:" + wireMockRule.port();
        KodeverkClientImpl kodeverkClient = new KodeverkClientImpl(apiUrl);

        givenThat(get(anyUrl())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(kodeverkJson))
        );
        Map<String, String> kodeverkBeskrivelser = kodeverkClient.hentKodeverkBeskrivelser(KODEVERK_SPRAAK);
        assertEquals("Hindi", kodeverkBeskrivelser.get("HI"));
        assertEquals("Pushto", kodeverkBeskrivelser.get("PS"));
        assertEquals("Portugesisk", kodeverkBeskrivelser.get("PT"));
    }

}
