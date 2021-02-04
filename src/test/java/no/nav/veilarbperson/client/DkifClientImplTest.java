package no.nav.veilarbperson.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.auth.context.UserRole;
import no.nav.common.test.auth.AuthTestUtils;
import no.nav.veilarbperson.client.dkif.DkifClient;
import no.nav.veilarbperson.client.dkif.DkifClientImpl;
import no.nav.veilarbperson.client.dkif.DkifKontaktinfo;
import no.nav.veilarbperson.utils.TestUtils;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static no.nav.veilarbperson.utils.TestData.TEST_FNR;
import static org.junit.Assert.*;

public class DkifClientImplTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    @Test
    public void skal_hente_kontaktinfo() {
        String kodeverkJson = TestUtils.readTestResourceFile("dkif-kontaktinfo.json");
        String apiUrl = "http://localhost:" + wireMockRule.port();
        DkifClient dkifClient = new DkifClientImpl(apiUrl, () -> "TOKEN");

        givenThat(get(anyUrl())
                .withHeader("Nav-Personidenter", equalTo(TEST_FNR.get()))
                .withHeader("Authorization", equalTo("Bearer TOKEN"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(kodeverkJson))
        );

        AuthContextHolder.withContext(AuthTestUtils.createAuthContext(UserRole.INTERN, "test"), () -> {
            DkifKontaktinfo kontaktinfo = dkifClient.hentKontaktInfo(TEST_FNR);
            assertEquals(kontaktinfo.getPersonident(), TEST_FNR.get());
            assertTrue(kontaktinfo.isKanVarsles());
            assertFalse(kontaktinfo.isReservert());
            assertEquals(kontaktinfo.getEpostadresse(), "noreply@nav.no");
            assertEquals(kontaktinfo.getMobiltelefonnummer(), "11111111");
        });
    }

}
