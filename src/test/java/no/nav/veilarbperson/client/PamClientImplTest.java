package no.nav.veilarbperson.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.pam.PamClientImpl;
import no.nav.veilarbperson.utils.TestUtils;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class PamClientImplTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    @Test
    public void skal_hente_cv_jobbprofil() {
        String pamCvJobbprofilJson = TestUtils.readTestResourceFile("pam-cv-jobbprofil.json");
        String apiUrl = "http://localhost:" + wireMockRule.port();
        PamClientImpl pamClient = new PamClientImpl(apiUrl, () -> "SYSTEM_TOKEN");

        givenThat(get("/rest/v2/arbeidssoker/1234?erManuell=true")
                .withHeader("Authorization", equalTo("Bearer SYSTEM_TOKEN"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(pamCvJobbprofilJson))
        );

        pamClient.hentCvOgJobbprofil(Fnr.of("1234"), true);
    }

    @Test
    public void skal_sjekke_helse() {
        String apiUrl = "http://localhost:" + wireMockRule.port();
        PamClientImpl pamClient = new PamClientImpl(apiUrl, () -> "SYSTEM_TOKEN");

        givenThat(get("/rest/internal/isAlive").willReturn(aResponse().withStatus(200)));

        pamClient.checkHealth();
    }

}
