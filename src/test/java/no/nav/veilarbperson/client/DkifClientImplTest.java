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

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Optional.ofNullable;
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
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Authorization", "Bearer TOKEN")
                        .withBody(kodeverkJson))
        );

        AuthContextHolder.withContext(AuthTestUtils.createAuthContext(UserRole.INTERN, "test"), () -> {
            DkifKontaktinfo kontaktinfo = dkifClient.hentKontaktInfo(TEST_FNR);
            assertEquals(kontaktinfo.getPersonident(), TEST_FNR.get());
            assertTrue(kontaktinfo.isKanVarsles());
            assertFalse(kontaktinfo.isReservert());
            assertEquals(kontaktinfo.getEpostadresse(), "noreply@nav.no");
            assertEquals(kontaktinfo.getMobiltelefonnummer(), "11111111");

            List<String> telefonList = new ArrayList<>(List.of("22222222"));
            telefonList = leggTelefonNummerIListe(kontaktinfo, telefonList);
            assertEquals("22222222", telefonList.get(0));
            assertEquals("11111111", telefonList.get(1));

            telefonList = leggTelefonNummerIListe(kontaktinfo, null);
            assertEquals(1, telefonList.size());
            assertEquals("11111111", telefonList.get(0));

            kontaktinfo.setMobiltelefonnummer(null);
            List<String> telefonList1 = new ArrayList<String>(List.of("22222222"));
            telefonList1 = leggTelefonNummerIListe(kontaktinfo, telefonList1);
            assertEquals(1, telefonList1.size());


        });
    }

    private List<String> leggTelefonNummerIListe(DkifKontaktinfo kontaktinfo, List<String> telefonList) {

        if(kontaktinfo.getMobiltelefonnummer() != null) {
            ofNullable(telefonList).ifPresent(telefon -> telefon.add(kontaktinfo.getMobiltelefonnummer()));

            ofNullable(telefonList).ifPresent(telefon -> telefon.add(kontaktinfo.getMobiltelefonnummer()));
            telefonList = ofNullable(telefonList).isPresent() ?  telefonList : new ArrayList<>(List.of(kontaktinfo.getMobiltelefonnummer()));
            return telefonList;
        }

        return telefonList;
    }

}
