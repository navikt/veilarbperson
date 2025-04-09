package no.nav.veilarbperson.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.nav.veilarbperson.client.digdir.*;
import no.nav.veilarbperson.client.pdl.domain.Epost;
import no.nav.veilarbperson.utils.TestUtils;
import org.junit.Rule;
import org.junit.Test;

import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static no.nav.veilarbperson.utils.TestData.TEST_FNR;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DigdirClientImplTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    @Test
    public void skal_hente_kontaktinfo() {
        String kodeverkJson = TestUtils.readTestResourceFile("digdir-kontaktinfo.json");
        String apiUrl = "http://localhost:" + wireMockRule.port();
        DigdirClient digdirClient = new DigdirClientImpl(apiUrl, () -> "TOKEN");

        PostPersonerRequest postPersonerRequest = new PostPersonerRequest(Set.of(TEST_FNR.get()));

        givenThat(get(anyUrl())
                .withHeader("Nav-Personident", equalTo(TEST_FNR.get()))
                .withHeader("Authorization", equalTo("Bearer TOKEN"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(kodeverkJson))
        );

        PostPersonerResponse kontaktinfo = digdirClient.hentKontaktInfo(postPersonerRequest);
        assert kontaktinfo != null;
        DigdirKontaktinfo digdirKontaktinfo = kontaktinfo.getPersoner().get(TEST_FNR.get());
        assertNotNull(digdirKontaktinfo);
        assertEquals("noreply@nav.no", digdirKontaktinfo.getEpostadresse());
        assertEquals("11111111", digdirKontaktinfo.getMobiltelefonnummer());
    //    assertFalse(digdirKontaktinfo.getSikkerDigitalPost());

        Epost epost = new Epost()
                .setEpostAdresse(digdirKontaktinfo.getEpostadresse())
                .setEpostSistOppdatert(digdirKontaktinfo.getEpostadresseOppdatert())
                .setMaster("KRR");

        assertEquals(digdirKontaktinfo.getEpostadresse(), epost.getEpostAdresse());
        assertEquals("01.01.2018", epost.getEpostSistOppdatert());
        assertEquals("KRR", epost.getMaster());
    }

}
