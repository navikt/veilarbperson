package no.nav.veilarbperson.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.nav.common.auth.subject.IdentType;
import no.nav.common.auth.subject.SsoToken;
import no.nav.common.auth.subject.Subject;
import no.nav.common.auth.subject.SubjectHandler;
import no.nav.veilarbperson.client.dkif.DkifClient;
import no.nav.veilarbperson.client.dkif.DkifClientImpl;
import no.nav.veilarbperson.client.dkif.DkifKontaktinfo;
import no.nav.veilarbperson.client.kodeverk.KodeverkClientImpl;
import no.nav.veilarbperson.utils.TestUtils;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static no.nav.veilarbperson.utils.TestData.TEST_FNR;
import static org.junit.Assert.*;

public class DkifClientImplTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    @Test
    public void skal_hente_kontaktinfo() {
        String kodeverkJson = TestUtils.readTestResourceFile("dkif-kontaktinfo.json");
        String apiUrl = "http://localhost:" + wireMockRule.port();
        DkifClient dkifClient = new DkifClientImpl(apiUrl);

        givenThat(get(anyUrl())
                .withHeader("Nav-Personidenter", equalTo(TEST_FNR))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(kodeverkJson))
        );

        SubjectHandler.withSubject(new Subject("test", IdentType.InternBruker, SsoToken.oidcToken("token", new HashMap<>())), () -> {
            DkifKontaktinfo kontaktinfo = dkifClient.hentKontaktInfo(TEST_FNR);
            assertEquals(kontaktinfo.getPersonident(), TEST_FNR);
            assertTrue(kontaktinfo.isKanVarsles());
            assertFalse(kontaktinfo.isReservert());
            assertEquals(kontaktinfo.getEpostadresse(), "noreply@nav.no");
            assertEquals(kontaktinfo.getMobiltelefonnummer(), "11111111");
        });
    }

}
