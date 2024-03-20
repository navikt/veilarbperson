package no.nav.veilarbperson.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.veilarboppfolging.UnderOppfolging;
import no.nav.veilarbperson.client.veilarboppfolging.VeilarboppfolgingClientImpl;
import no.nav.veilarbperson.utils.TestUtils;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VeilarboppfolgingClientImplTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    @Test
    public void skal_hente_underoppfolging_json() {
        String underOppfolgingJson = TestUtils.readTestResourceFile("veilarboppfolging-underoppfolging.json");
        String apiUrl = "http://localhost:" + wireMockRule.port();
        VeilarboppfolgingClientImpl veilarboppfolgingClient = new VeilarboppfolgingClientImpl(apiUrl, () -> "USER_TOKEN");

        givenThat(post("/api/v2/hent-underOppfolging")
                .withRequestBody(equalToJson("{\"fnr\": \"1234\"}"))
                .withHeader("Authorization", equalTo("Bearer USER_TOKEN"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(underOppfolgingJson))
        );

        UnderOppfolging underOppfolging = veilarboppfolgingClient.hentUnderOppfolgingStatus(Fnr.of("1234"));

        assertTrue(underOppfolging.isUnderOppfolging());
        assertFalse(underOppfolging.isErManuell());
    }

    @Test
    public void skal_sjekke_helse() {
        String apiUrl = "http://localhost:" + wireMockRule.port();
        VeilarboppfolgingClientImpl veilarboppfolgingClient = new VeilarboppfolgingClientImpl(apiUrl, () -> "USER_TOKEN");

        givenThat(get("/internal/isAlive").willReturn(aResponse().withStatus(200)));

        veilarboppfolgingClient.checkHealth();
    }

}
