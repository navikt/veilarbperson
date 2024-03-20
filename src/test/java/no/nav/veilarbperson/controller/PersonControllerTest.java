package no.nav.veilarbperson.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.poao_tilgang.poao_tilgang_test_core.NavAnsatt;
import no.nav.poao_tilgang.poao_tilgang_test_core.NavContext;
import no.nav.poao_tilgang.poao_tilgang_test_core.PrivatBruker;
import no.nav.veilarbperson.config.ApplicationTestConfig;
import no.nav.veilarbperson.controller.v1.PersonController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PersonController.class)
@Import({ApplicationTestConfig.class})
@DirtiesContext
public class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NavContext navContext;

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
                        get("/api/person/registrering")
                                .queryParam("fnr", fnr)
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
                        get("/api/person/registrering")
                                .queryParam("fnr", fnr)
                                .header("test_ident", navAnsatt.getNavIdent())
                                .header("test_ident_type", "INTERN")
                )
                .andExpect(status().is(404));
    }
}

