package no.nav.veilarbperson.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import no.nav.common.json.JsonUtils;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.difi.*;
import no.nav.veilarbperson.client.digdir.HarLoggetInnRespons;
import org.junit.Rule;
import org.junit.Test;

import java.util.Date;

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

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("subject")
                .audience("TEST_AUDIENCE")
                .expirationTime(new Date( new Date().getTime() +60000))
                .issuer("TEST_ISSUER")
                .build();

        PlainJWT plainJWT = new PlainJWT(claimsSet);
        String parsedString = plainJWT.serialize();

        String jsonBody = JsonUtils.toJson(
                new HarLoggetInnRespons()
                    .setHarbruktnivaa4(true)
                    .setPersonidentifikator(fnr)
        );

        givenThat(post(urlMatching("/token"))
                .withBasicAuth("username", "password")
                .willReturn(aResponse().withStatus(200).withBody("{\"access_token\":\""+ parsedString + "\"}")));


        givenThat(post(urlMatching("/nivaa4"))
                .withRequestBody(new EqualToJsonPattern("{ \"personidentifikator\": \"12345678900\"}", true, false))
                .withHeader(AUTHORIZATION, equalTo("Bearer " + parsedString))
                .withHeader("x-nav-apiKey", equalTo("apigw-key"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(jsonBody))
        );


        DifiAccessTokenProvider difiAccessTokenProvider = new DifiAccessTokenProviderImpl(credentials, tokenUrl);
        DifiClientImpl difiClient = new DifiClientImpl(difiAccessTokenProvider, "apigw-key", nivaa4Url);
        HarLoggetInnRespons harLoggetInnRespons = difiClient.harLoggetInnSiste18mnd(fnr);
        difiClient.harLoggetInnSiste18mnd(fnr);

        assertTrue(harLoggetInnRespons.isHarbruktnivaa4());
        verify(1, postRequestedFor(urlEqualTo("/token")));
        verify(2, postRequestedFor(urlEqualTo("/nivaa4")));
    }
}
