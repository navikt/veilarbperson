package no.nav.veilarbperson.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.nom.SkjermetClientImpl;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.junit.Assert.assertTrue;

public class SkjermetClientImplTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    @Test
    public void skal_skjermet() {
        String apiUrl = "http://localhost:" + wireMockRule.port();
        SkjermetClientImpl veilarboppfolgingClient = new SkjermetClientImpl(apiUrl, () -> "TEST");

        givenThat(post(anyUrl())
                .withRequestBody(
                                equalToJson("{ \"personident\": \"1234\" }")
                        ).willReturn(aResponse()
                                .withStatus(200)
                                .withBody("true"))
        );

        Boolean hentSkjermet = veilarboppfolgingClient.hentSkjermet(Fnr.of("1234"));
        assertTrue(hentSkjermet);
    }
}
