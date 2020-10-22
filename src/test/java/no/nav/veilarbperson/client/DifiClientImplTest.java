package no.nav.veilarbperson.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern;
import no.nav.common.json.JsonUtils;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.difi.AccessTokenRepository;
import no.nav.veilarbperson.client.difi.DifiClientImpl;
import no.nav.veilarbperson.client.difi.HarLoggetInnRespons;
import no.nav.veilarbperson.client.difi.SbsServiceUser;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class DifiClientImplTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    @Test
    public void skal_hente_har_niva4() {
        String baseUrl = "http://localhost:" + wireMockRule.port();
        String nivaa4Url = baseUrl + "/nivaa4";
        String tokenUrl = baseUrl + "/token";
        SbsServiceUser credentials = new SbsServiceUser("username", "password");
        Fnr fnr = Fnr.of("12345678900");

        String jsonBody = JsonUtils.toJson(
                new HarLoggetInnRespons()
                    .setHarbruktnivaa4(true)
                    .setPersonidentifikator(fnr)
        );

        givenThat(post(urlMatching("/token"))
                .withBasicAuth("username", "password")
                .willReturn(aResponse().withStatus(200).withBody("{\"access_token\":\"superLangtTokenHer\"}")));


        givenThat(post(urlMatching("/nivaa4"))
                .withRequestBody(new EqualToJsonPattern("{ \"personidentifikator\": \"12345678900\"}", true, false))
                .withHeader(AUTHORIZATION, equalTo("Bearer superLangtTokenHer"))
                .withHeader("x-nav-apiKey", equalTo("apigw-key"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(jsonBody))
        );

        AccessTokenRepository accessTokenRepository = new AccessTokenRepository(credentials, tokenUrl);
        DifiClientImpl difiClient = new DifiClientImpl(accessTokenRepository, "apigw-key", nivaa4Url);
        HarLoggetInnRespons harLoggetInnRespons = difiClient.harLoggetInnSiste18mnd(fnr);
        assertTrue(harLoggetInnRespons.isHarbruktnivaa4());
    }
}
