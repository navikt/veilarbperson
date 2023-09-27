package no.nav.veilarbperson.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.veilarbperson.config.ApplicationTestConfig;
import no.nav.veilarbperson.controller.v3.PersonV3Controller;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PersonV3Controller.class)
@Import({ApplicationTestConfig.class})
@DirtiesContext
public class PersonV3ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void returnerer_registrering_for_registrert_bruker() throws Exception {
        String expectedJson = "{}";
        String fnr = "1234";

        stubFor(WireMock.get(urlEqualTo("/veilarbregistrering/api/registrering?fnr=" + fnr))
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody(expectedJson)));


        mockMvc.perform(post("/api/v3/person/registrering").contentType(APPLICATION_JSON).content("{\"fnr\":\"1234\"}"))
                .andExpect(content().json(expectedJson))
                .andExpect(status().is(200));
    }

    @Test
    public void returnerer_ikke_registrering_for_bruker_som_ikke_er_registrert() throws Exception {
        String fnr = "4321";

        stubFor(WireMock.get(urlEqualTo("/veilarbregistrering/api/registrering?fnr=" + fnr))
                .willReturn(
                        notFound()
                                .withHeader("Content-Type", "application/json")));


        mockMvc.perform(post("/api/v3/person/registrering").contentType(APPLICATION_JSON).content("{\"fnr\":\"4321\"}"))
                .andExpect(status().is(404));
    }
}
