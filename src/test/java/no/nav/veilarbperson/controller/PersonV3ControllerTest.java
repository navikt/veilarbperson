package no.nav.veilarbperson.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.common.json.JsonUtils;
import no.nav.poao_tilgang.poao_tilgang_test_core.NavAnsatt;
import no.nav.poao_tilgang.poao_tilgang_test_core.NavContext;
import no.nav.poao_tilgang.poao_tilgang_test_core.PrivatBruker;
import no.nav.veilarbperson.config.ApplicationTestConfig;
import no.nav.veilarbperson.controller.v3.PersonV3Controller;
import no.nav.veilarbperson.domain.PersonFraPdlRequest;
import no.nav.veilarbperson.domain.PersonV2Data;
import no.nav.veilarbperson.service.AuthService;
import no.nav.veilarbperson.service.PersonV2Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static no.nav.veilarbperson.utils.TestData.TEST_FNR;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PersonV3Controller.class)
@Import({ApplicationTestConfig.class})
@DirtiesContext
public class PersonV3ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NavContext navContext;

    @MockBean
    private AuthService authService;

    @MockBean
    private PersonV2Service personV2Service;

    @Test
    public void returnerer_registrering_for_registrert_bruker() throws Exception {
        PrivatBruker ny = navContext.getPrivatBrukere().ny();
        NavAnsatt navAnsatt = navContext.getNavAnsatt().nyFor(ny);

        String expectedJson = "{}";
        String fnr = ny.getNorskIdent();

        stubFor(WireMock.get(urlEqualTo("/veilarbregistrering/api/registrering?fnr=" + fnr))
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody(expectedJson)));


        mockMvc
                .perform(
                        post("/api/v3/person/hent-registrering")
                                .contentType(APPLICATION_JSON)
                                .content("{\"fnr\":\""+ fnr +"\"}")
                                .header("test_ident", navAnsatt.getNavIdent())
                                .header("test_ident_type", "INTERN")
                )
                .andExpect(content().json(expectedJson))
                .andExpect(status().is(200));
    }

    @Test
    public void returnerer_ikke_registrering_for_bruker_som_ikke_er_registrert() throws Exception {
        PrivatBruker ny = navContext.getPrivatBrukere().ny();
        NavAnsatt navAnsatt = navContext.getNavAnsatt().nyFor(ny);
        String fnr = ny.getNorskIdent();

        stubFor(WireMock.get(urlEqualTo("/veilarbregistrering/api/registrering?fnr=" + fnr))
                .willReturn(
                        notFound()
                                .withHeader("Content-Type", "application/json")));


        mockMvc
                .perform(
                        post("/api/v3/person/hent-registrering")
                                .contentType(APPLICATION_JSON)
                                .content("{\"fnr\":\""+ fnr +"\"}")
                                .header("test_ident", navAnsatt.getNavIdent())
                                .header("test_ident_type", "INTERN")
                )
                .andExpect(status().is(404));
    }

    @Test
    public void returnerer_person_uten_behandlingsnummer_hent_person() throws Exception {
        PrivatBruker ny = navContext.getPrivatBrukere().ny();
        NavAnsatt navAnsatt = navContext.getNavAnsatt().nyFor(ny);
        when(personV2Service.hentFlettetPerson(new PersonFraPdlRequest(TEST_FNR, null))).thenReturn(new PersonV2Data().setFodselsnummer(TEST_FNR).setFornavn("Knut").setMellomnavn("Knutsen"));

        String expectedJson = "{\"fornavn\":\"Knut\",\"mellomnavn\":\"Knutsen\",\"fodselsnummer\":\"12345678900\"}";

        mockMvc
                .perform(
                        post("/api/v3/hent-person")
                                .contentType(APPLICATION_JSON)
                                .content(JsonUtils.toJson(new PersonFraPdlRequest(TEST_FNR, null)))
                                .header(ACCEPT, APPLICATION_JSON_VALUE)
                                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                .header("test_ident", navAnsatt.getNavIdent())
                                .header("test_ident_type", "INTERN")
                )
                .andExpect(content().json(expectedJson))
                .andExpect(status().is(200));
    }

    @Test
    public void returnerer_person_med_behandlingsnummer_hent_person() throws Exception {
        PrivatBruker ny = navContext.getPrivatBrukere().ny();
        NavAnsatt navAnsatt = navContext.getNavAnsatt().nyFor(ny);
        when(personV2Service.hentFlettetPerson(new PersonFraPdlRequest(TEST_FNR, "B555"))).thenReturn(new PersonV2Data().setFodselsnummer(TEST_FNR).setFornavn("Knut").setMellomnavn("Knutsen"));

        String expectedJson = "{\"fornavn\":\"Knut\",\"mellomnavn\":\"Knutsen\",\"fodselsnummer\":\"12345678900\"}";

        mockMvc
                .perform(
                        post("/api/v3/hent-person")
                                .contentType(APPLICATION_JSON)
                                .content(JsonUtils.toJson(new PersonFraPdlRequest(TEST_FNR, "B555")))
                                .header(ACCEPT, APPLICATION_JSON_VALUE)
                                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                .header("test_ident", navAnsatt.getNavIdent())
                                .header("test_ident_type", "INTERN")
                )
                .andExpect(content().json(expectedJson))
                .andExpect(status().is(200));
    }

}
