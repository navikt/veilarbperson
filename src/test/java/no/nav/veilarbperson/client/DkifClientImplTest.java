package no.nav.veilarbperson.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.nav.veilarbperson.client.dkif.DkifClient;
import no.nav.veilarbperson.client.dkif.DkifClientImpl;
import no.nav.veilarbperson.client.dkif.DkifKontaktinfo;
import no.nav.veilarbperson.client.pdl.domain.Epost;
import no.nav.veilarbperson.utils.PersonV2DataMapper;
import no.nav.veilarbperson.utils.TestUtils;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static no.nav.veilarbperson.utils.TestData.TEST_FNR;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

            DkifKontaktinfo kontaktinfo = dkifClient.hentKontaktInfo(TEST_FNR);
            assertEquals(kontaktinfo.getPersonident(), TEST_FNR.get());
            assertTrue(kontaktinfo.isKanVarsles());
            assertFalse(kontaktinfo.isReservert());
            assertEquals(kontaktinfo.getEpostadresse(), "noreply@nav.no");
            assertEquals(kontaktinfo.getMobiltelefonnummer(), "11111111");
            assertEquals(kontaktinfo.getEpostSistOppdatert(), "2018-01-01T11:38:22,000+00:00");
            assertEquals(kontaktinfo.getMobilSistOppdatert(), "2018-01-01T11:38:22,000+00:00");
            assertEquals(kontaktinfo.getSpraak(), "NB");

        String epostSistOppdatert = kontaktinfo.getEpostSistOppdatert();
        String formatertEpostSistOppdatert = epostSistOppdatert!=null? PersonV2DataMapper.parseDateFromDateTime(epostSistOppdatert) : null;
        Epost epost = kontaktinfo.getEpostadresse() !=null ?
                new Epost()
                        .setEpostAdresse(kontaktinfo.getEpostadresse())
                        .setEpostSistOppdatert(formatertEpostSistOppdatert)
                        .setMaster("KRR")
                : null;


        assertEquals(kontaktinfo.getEpostadresse(), epost.getEpostAdresse());
        assertEquals("01.01.2018",epost.getEpostSistOppdatert());
        assertEquals("KRR", epost.getMaster());
    }

}
