package no.nav.veilarbperson.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.nav.veilarbperson.client.digdir.DigdirClient;
import no.nav.veilarbperson.client.digdir.DigdirClientImpl;
import no.nav.veilarbperson.client.digdir.DigdirKontaktinfo;
import no.nav.veilarbperson.client.pdl.domain.Epost;
import no.nav.veilarbperson.utils.TestUtils;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static no.nav.veilarbperson.utils.PersonV2DataMapper.parseZonedDateToDateString;
import static no.nav.veilarbperson.utils.TestData.TEST_FNR;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DigdirClientImplTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    @Test
    public void skal_hente_kontaktinfo() {
        String kodeverkJson = TestUtils.readTestResourceFile("digdir-kontaktinfo.json");
        String apiUrl = "http://localhost:" + wireMockRule.port();
        DigdirClient digdirClient = new DigdirClientImpl(apiUrl, () -> "TOKEN");

        givenThat(get(anyUrl())
                .withHeader("Nav-Personidenter", equalTo(TEST_FNR.get()))
                .withHeader("Authorization", equalTo("Bearer TOKEN"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(kodeverkJson))
        );

        DigdirKontaktinfo kontaktinfo = digdirClient.hentKontaktInfo(TEST_FNR);
        String epostSistOppdatert = parseZonedDateToDateString(kontaktinfo.getEpostadresseOppdatert());
        assertEquals(kontaktinfo.getPersonident(), TEST_FNR.get());
        assertTrue(kontaktinfo.getKanVarsles());
        assertFalse(kontaktinfo.getReservert());
        assertEquals(kontaktinfo.getEpostadresse(), "noreply@nav.no");
        assertEquals(kontaktinfo.getMobiltelefonnummer(), "11111111");
        assertEquals(epostSistOppdatert, "01.01.2018");
        assertEquals(parseZonedDateToDateString(kontaktinfo.getMobiltelefonnummerOppdatert()), "03.12.2011");
        assertEquals(kontaktinfo.getSpraak(), "NB");

        Epost epost = new Epost()
                .setEpostAdresse(kontaktinfo.getEpostadresse())
                .setEpostSistOppdatert(epostSistOppdatert)
                .setMaster("KRR");

        assertEquals(kontaktinfo.getEpostadresse(), epost.getEpostAdresse());
        assertEquals("01.01.2018", epost.getEpostSistOppdatert());
        assertEquals("KRR", epost.getMaster());
    }

}
