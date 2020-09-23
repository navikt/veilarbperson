package no.nav.veilarbperson.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.pam.PamClientImpl;
import no.nav.veilarbperson.utils.TestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

public class PamClientImplTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    @Test
    public void skal_hente_cv_jobbprofil_json() {
        String pamCvJobbprofilJson = TestUtils.readTestResourceFile("pam-cv-jobbprofil.json");
        String apiUrl = "http://localhost:" + wireMockRule.port();
        PamClientImpl pamClient = new PamClientImpl(apiUrl, () -> "TOKEN");

        givenThat(get("/rest/v1/arbeidssoker/1234")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Authorization", "Bearer TOKEN")
                        .withBody(pamCvJobbprofilJson))
        );

        String jsonResponse = pamClient.hentCvOgJobbprofilJson(Fnr.of("1234"));

        assertEquals(pamCvJobbprofilJson, jsonResponse);
    }

    @Test
    public void skal_kaste_status_for_diverse_koder() {
        String apiUrl = "http://localhost:" + wireMockRule.port();
        PamClientImpl pamClient = new PamClientImpl(apiUrl, () -> "TOKEN");

        try {
            givenThat(get("/rest/v1/arbeidssoker/1234").willReturn(aResponse().withStatus(401)));
            pamClient.hentCvOgJobbprofilJson(Fnr.of("1234"));
        } catch (ResponseStatusException rse) {
            assertEquals(HttpStatus.UNAUTHORIZED, rse.getStatus());
        }

        try {
            givenThat(get("/rest/v1/arbeidssoker/1234").willReturn(aResponse().withStatus(403)));
            pamClient.hentCvOgJobbprofilJson(Fnr.of("1234"));
        } catch (ResponseStatusException rse) {
            assertEquals(HttpStatus.FORBIDDEN, rse.getStatus());
        }

        try {
            givenThat(get("/rest/v1/arbeidssoker/1234").willReturn(aResponse().withStatus(404)));
            pamClient.hentCvOgJobbprofilJson(Fnr.of("1234"));
        } catch (ResponseStatusException rse) {
            assertEquals(HttpStatus.NOT_FOUND, rse.getStatus());
        }

        try {
            givenThat(get("/rest/v1/arbeidssoker/1234").willReturn(aResponse().withStatus(204)));
            pamClient.hentCvOgJobbprofilJson(Fnr.of("1234"));
        } catch (ResponseStatusException rse) {
            assertEquals(HttpStatus.NO_CONTENT, rse.getStatus());
        }
    }

    @Test
    public void skal_sjekke_helse() {
        String apiUrl = "http://localhost:" + wireMockRule.port();
        PamClientImpl pamClient = new PamClientImpl(apiUrl, () -> "TOKEN");

        givenThat(get("/rest/internal/isAlive").willReturn(aResponse().withStatus(200)));

        pamClient.checkHealth();
    }

}
