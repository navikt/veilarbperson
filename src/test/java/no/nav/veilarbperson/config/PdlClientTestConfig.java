package no.nav.veilarbperson.config;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.AnythingPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import no.nav.veilarbperson.client.pdl.PdlClientImpl;
import no.nav.veilarbperson.service.AuthService;
import no.nav.veilarbperson.utils.TestUtils;
import org.junit.Rule;

import java.util.Arrays;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.mockito.Mockito.mock;

public class PdlClientTestConfig {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    public String getPdlApiUrl() {
        return "http://localhost:" + wireMockRule.port();
    }

    public PdlClientImpl getPdlClient() {
        return new PdlClientImpl(getPdlApiUrl(), mock(AuthService.class), () -> "USER_TOKEN", () -> "SYSTEM_TOKEN");
    }

    public void configurePdlResponse(String responseFile, String... requestinnhold) {
        String hentPersonResponseJson = TestUtils.readTestResourceFile(responseFile);

        StringValuePattern pattern = Arrays.stream(requestinnhold).map(WireMock::containing).reduce(WireMock::or).orElse(
                new AnythingPattern());

        givenThat(post(WireMock.urlEqualTo("/graphql"))
                .withRequestBody(pattern)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(hentPersonResponseJson))
        );
    }
}
