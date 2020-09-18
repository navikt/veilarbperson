package no.nav.veilarbperson.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern;
import no.nav.common.json.JsonUtils;
import no.nav.common.types.identer.Fnr;
import no.nav.common.utils.Credentials;
import no.nav.veilarbperson.client.difi.DifiClientImpl;
import no.nav.veilarbperson.client.difi.HarLoggetInnRespons;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertTrue;

public class DifiClientImplTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    @Test
    public void skal_hente_har_niva4() {
        String apiUrl = "http://localhost:" + wireMockRule.port();
        Credentials credentials = new Credentials("username", "password");
        Fnr fnr = Fnr.of("12345678900");

        String jsonBody = JsonUtils.toJson(
                new HarLoggetInnRespons()
                    .setHarbruktnivaa4(true)
                    .setPersonidentifikator(fnr)
        );

        givenThat(post(anyUrl())
                .withRequestBody(new EqualToJsonPattern("{ \"personidentifikator\": \"12345678900\"}", true, false))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(jsonBody))
        );

        DifiClientImpl difiClient = new DifiClientImpl(credentials, apiUrl);
        HarLoggetInnRespons harLoggetInnRespons = difiClient.harLoggetInnSiste18mnd(fnr);
        assertTrue(harLoggetInnRespons.isHarbruktnivaa4());
    }
}
