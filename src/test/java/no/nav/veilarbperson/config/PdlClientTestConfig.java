package no.nav.veilarbperson.config;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.nav.veilarbperson.client.pdl.PdlClientImpl;
import no.nav.veilarbperson.utils.TestUtils;
import org.junit.Rule;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

public class PdlClientTestConfig {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    public String configurApiResponse(String responseFilename) {
        String hentPersonResponseJson = TestUtils.readTestResourceFile(responseFilename);
        String apiUrl = "http://localhost:" + wireMockRule.port();

        givenThat(post(anyUrl())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(hentPersonResponseJson))
        );

        return apiUrl;
    }

    public PdlClientImpl configurPdlClient(String responseFile) {
        String apiUrl = configurApiResponse(responseFile);
        return new PdlClientImpl(apiUrl);
    }

}
