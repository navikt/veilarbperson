package no.nav.veilarbperson.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.nav.common.utils.Credentials;
import no.nav.veilarbperson.client.difi.DifiClientImpl;
import no.nav.veilarbperson.client.difi.HarLoggetInnRespons;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static no.nav.veilarbperson.utils.TestData.TEST_FNR;
import static org.junit.Assert.assertTrue;

public class DifiClientImplTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    @Test
    public void skal_hente_har_niva4() {
        String apiUrl = "http://localhost:" + wireMockRule.port();
        Credentials credentials = new Credentials("username", "password");

        JSONObject jo = new JSONObject();
        String body = jo
                .put("harbruktnivaa4", true)
                .put("personidentifikator", TEST_FNR)
                .toString();

        givenThat(post(anyUrl())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(body))
        );

        DifiClientImpl difiClient = new DifiClientImpl(credentials, apiUrl);
        HarLoggetInnRespons harLoggetInnRespons = difiClient.harLoggetInnSiste18mnd(TEST_FNR);
        assertTrue(harLoggetInnRespons.isHarbruktnivaa4());
    }
}
