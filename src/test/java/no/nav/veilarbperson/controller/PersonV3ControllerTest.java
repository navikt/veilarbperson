package no.nav.veilarbperson.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.common.json.JsonUtils;
import no.nav.common.types.identer.Fnr;
import no.nav.poao_tilgang.poao_tilgang_test_core.NavAnsatt;
import no.nav.poao_tilgang.poao_tilgang_test_core.NavContext;
import no.nav.poao_tilgang.poao_tilgang_test_core.PrivatBruker;
import no.nav.veilarbperson.config.ApplicationTestConfig;
import no.nav.veilarbperson.controller.v3.PersonV3Controller;
import no.nav.veilarbperson.domain.Foedselsdato;
import no.nav.veilarbperson.domain.PersonFraPdlRequest;
import no.nav.veilarbperson.domain.PersonNavnV2;
import no.nav.veilarbperson.domain.PersonV2Data;
import no.nav.veilarbperson.service.AuthService;
import no.nav.veilarbperson.service.PersonV2Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static no.nav.veilarbperson.utils.TestData.TEST_FNR;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
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

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private PersonV2Service personV2Service;

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

    @Test
    void returnerer_opplysninger_om_arbeidssoeker_med_profilering_paa_bruker() throws Exception {
        PrivatBruker ny = navContext.getPrivatBrukere().ny();
        NavAnsatt navAnsatt = navContext.getNavAnsatt().nyFor(ny);
        String fnr = ny.getNorskIdent();

        String expectedJsonArbeidssoekerPeriode = """
                    [
                      {
                        "periodeId": "ea0ad984-8b99-4fff-afd6-07737ab19d16",
                        "startet": {
                          "tidspunkt": "2024-04-23T13:04:40.739Z",
                          "utfoertAv": {
                            "type": "SLUTTBRUKER"
                          },
                          "kilde": "europe-north1-docker.pkg.dev/nais-management-233d/paw/paw-arbeidssokerregisteret-api-inngang:24.04.23.118-1",
                          "aarsak": "Er over 18 år, er bosatt i Norge i hendhold Folkeregisterloven"
                        },
                        "avsluttet": null
                      }
                    ]
                """.trim();

        String expectedJsonOpplysningerOmArbeidssoeker = "[" +
                "  {" +
                "    \"opplysningerOmArbeidssoekerId\": \"913161a3-dde9-4448-abf8-2a01a043f8cd\"," +
                "    \"periodeId\": \"ea0ad984-8b99-4fff-afd6-07737ab19d16\"," +
                "    \"sendtInnAv\": {" +
                "      \"tidspunkt\": \"2024-04-23T13:22:58.089Z\"," +
                "      \"utfoertAv\": {" +
                "        \"type\": \"SLUTTBRUKER\"," +
                "        \"id\": \"Z123456\"" +
                "      }," +
                "      \"kilde\": \"paw-arbeidssoekerregisteret-inngang\"," +
                "      \"aarsak\": \"opplysning om arbeidssøker sendt inn\"" +
                "    }," +
                "    \"jobbsituasjon\": [" +
                "      {" +
                "        \"beskrivelse\": \"ALDRI_HATT_JOBB\"," +
                "        \"detaljer\": { \"prosent\":  \"25\" }" +
                "      }," +
                "      {" +
                "        \"beskrivelse\": \"ER_PERMITTERT\"," +
                "        \"detaljer\": { \"prosent\":  \"75\" }" +
                "      }" +
                "    ]," +
                "    \"utdanning\": {" +
                "      \"nus\": \"3\"," +
                "      \"bestaatt\": \"JA\"," +
                "      \"godkjent\": \"JA\"" +
                "    }," +
                "    \"helse\": {" +
                "      \"helsetilstandHindrerArbeid\": \"NEI\"" +
                "    }," +
                "    \"annet\": {" +
                "      \"andreForholdHindrerArbeid\": \"NEI\"" +
                "    }" +
                "  }" +
                "]";

        String expectedJsonProfilering = "[" +
                "  {" +
                "    \"profileringId\": \"7c9a2feb-0b31-4101-825a-0c4a3d465e01\"," +
                "    \"periodeId\": \"ea0ad984-8b99-4fff-afd6-07737ab19d16\"," +
                "    \"opplysningerOmArbeidssoekerId\": \"913161a3-dde9-4448-abf8-2a01a043f8cd\"," +
                "    \"sendtInnAv\": {" +
                "      \"tidspunkt\": \"2024-04-25T08:19:53.612Z\"," +
                "      \"utfoertAv\": {" +
                "        \"type\": \"SYSTEM\"" +
                "      }," +
                "      \"kilde\": \"null-null\"," +
                "      \"aarsak\": \"opplysninger-mottatt\"" +
                "    }," +
                "    \"profilertTil\": \"OPPGITT_HINDRINGER\"," +
                "    \"jobbetSammenhengendeSeksAvTolvSisteManeder\": false," +
                "    \"alder\": 33" +
                "  }" +
                "]";

        stubFor(WireMock.post(urlEqualTo("/api/v1/veileder/arbeidssoekerperioder"))
                .willReturn(ok()
                        .withHeader("Nav-Consumer-Id", "veilarbperson")
                        .withBody(expectedJsonArbeidssoekerPeriode)));

        stubFor(WireMock.post(urlEqualTo("/api/v1/veileder/opplysninger-om-arbeidssoeker"))
                .willReturn(ok()
                        .withHeader("Nav-Consumer-Id", "veilarbperson")
                        .withBody(expectedJsonOpplysningerOmArbeidssoeker)));

        stubFor(WireMock.post(urlEqualTo("/api/v1/veileder/profilering"))
                .willReturn(ok()
                        .withHeader("Nav-Consumer-Id", "veilarbperson")
                        .withBody(expectedJsonProfilering)));

        String expectedJson = "{\"arbeidssoekerperiodeStartet\":\"2024-04-23T13:04:40.739Z\",\"opplysningerOmArbeidssoeker\":{\"opplysningerOmArbeidssoekerId\":\"913161a3-dde9-4448-abf8-2a01a043f8cd\",\"periodeId\":\"ea0ad984-8b99-4fff-afd6-07737ab19d16\",\"sendtInnAv\":{\"tidspunkt\":\"2024-04-23T13:22:58.089Z\",\"utfoertAv\":{\"type\":\"SLUTTBRUKER\",\"id\":\"Z123456\"},\"kilde\":\"paw-arbeidssoekerregisteret-inngang\",\"aarsak\":\"opplysning om arbeidssøker sendt inn\"},\"utdanning\":{\"nus\":\"VIDEREGAENDE_GRUNNUTDANNING\",\"bestaatt\":\"JA\",\"godkjent\":\"JA\"},\"helse\":{\"helsetilstandHindrerArbeid\":\"NEI\"},\"annet\":{\"andreForholdHindrerArbeid\":\"NEI\"},\"jobbsituasjon\":[{\"beskrivelse\":\"ALDRI_HATT_JOBB\",\"detaljer\":{\"prosent\":\"25\"}},{\"beskrivelse\":\"ER_PERMITTERT\",\"detaljer\":{\"prosent\":\"75\"}}]},\"profilering\":{\"profileringId\":\"7c9a2feb-0b31-4101-825a-0c4a3d465e01\",\"periodeId\":\"ea0ad984-8b99-4fff-afd6-07737ab19d16\",\"opplysningerOmArbeidssoekerId\":\"913161a3-dde9-4448-abf8-2a01a043f8cd\",\"sendtInnAv\":{\"tidspunkt\":\"2024-04-25T08:19:53.612Z\",\"utfoertAv\":{\"type\":\"SYSTEM\",\"id\":null},\"kilde\":\"null-null\",\"aarsak\":\"opplysninger-mottatt\"},\"profilertTil\":\"OPPGITT_HINDRINGER\",\"jobbetSammenhengendeSeksAvTolvSisteManeder\":false,\"alder\":33}}";

        mockMvc
                .perform(
                        post("/api/v3/person/hent-siste-opplysninger-om-arbeidssoeker-med-profilering")
                                .contentType(APPLICATION_JSON)
                                .content(JsonUtils.toJson(new PersonFraPdlRequest(Fnr.of(fnr), null)))
                                .header("test_ident", navAnsatt.getNavIdent())
                                .header("test_ident_type", "INTERN")
                )
                .andExpect(status().is(200))
                .andExpect(content().json(expectedJson));

    }

    @Test
    void returnerer_null_dersom_person_ikke_har_aktiv_arbeidssoekerperiode() throws Exception {
        PrivatBruker ny = navContext.getPrivatBrukere().ny();
        NavAnsatt navAnsatt = navContext.getNavAnsatt().nyFor(ny);
        String fnr = ny.getNorskIdent();

        String expectedJsonOpplysningerOmArbeidssoeker = "[" +
                "  {" +
                "    \"opplysningerOmArbeidssoekerId\": \"913161a3-dde9-4448-abf8-2a01a043f8cd\"," +
                "    \"periodeId\": \"ea0ad984-8b99-4fff-afd6-07737ab19d16\"," +
                "    \"sendtInnAv\": {" +
                "      \"tidspunkt\": \"2024-04-23T13:22:58.089Z\"," +
                "      \"utfoertAv\": {" +
                "        \"type\": \"SLUTTBRUKER\"" +
                "      }," +
                "      \"kilde\": \"paw-arbeidssoekerregisteret-inngang\"," +
                "      \"aarsak\": \"opplysning om arbeidssøker sendt inn\"" +
                "    }," +
                "    \"jobbsituasjon\": [" +
                "      {" +
                "        \"beskrivelse\": \"ALDRI_HATT_JOBB\"," +
                "        \"detaljer\": { \"prosent\":  \"25\" }" +
                "      }," +
                "      {" +
                "        \"beskrivelse\": \"ER_PERMITTERT\"," +
                "        \"detaljer\": { \"prosent\":  \"75\" }" +
                "      }" +
                "    ]," +
                "    \"utdanning\": {" +
                "      \"nus\": \"3\"," +
                "      \"bestaatt\": \"JA\"," +
                "      \"godkjent\": \"JA\"" +
                "    }," +
                "    \"helse\": {" +
                "      \"helsetilstandHindrerArbeid\": \"NEI\"" +
                "    }," +
                "    \"annet\": {" +
                "      \"andreForholdHindrerArbeid\": \"NEI\"" +
                "    }" +
                "  }" +
                "]";

        String expectedJsonProfilering = "[" +
                "  {" +
                "    \"profileringId\": \"7c9a2feb-0b31-4101-825a-0c4a3d465e01\"," +
                "    \"periodeId\": \"ea0ad984-8b99-4fff-afd6-07737ab19d16\"," +
                "    \"opplysningerOmArbeidssoekerId\": \"913161a3-dde9-4448-abf8-2a01a043f8cd\"," +
                "    \"sendtInnAv\": {" +
                "      \"tidspunkt\": \"2024-04-25T08:19:53.612Z\"," +
                "      \"utfoertAv\": {" +
                "        \"type\": \"SYSTEM\"" +
                "      }," +
                "      \"kilde\": \"null-null\"," +
                "      \"aarsak\": \"opplysninger-mottatt\"" +
                "    }," +
                "    \"profilertTil\": \"OPPGITT_HINDRINGER\"," +
                "    \"jobbetSammenhengendeSeksAvTolvSisteManeder\": false," +
                "    \"alder\": 33" +
                "  }" +
                "]";

        stubFor(WireMock.post(urlEqualTo("/api/v1/veileder/arbeidssoekerperioder"))
                .willReturn(ok()
                        .withHeader("Nav-Consumer-Id", "veilarbperson")
                        .withBody("[]")));

        stubFor(WireMock.post(urlEqualTo("/api/v1/veileder/opplysninger-om-arbeidssoeker"))
                .willReturn(ok()
                        .withHeader("Nav-Consumer-Id", "veilarbperson")
                        .withBody(expectedJsonOpplysningerOmArbeidssoeker)));

        stubFor(WireMock.post(urlEqualTo("/api/v1/veileder/profilering"))
                .willReturn(ok()
                        .withHeader("Nav-Consumer-Id", "veilarbperson")
                        .withBody(expectedJsonProfilering)));

        mockMvc
                .perform(
                        post("/api/v3/person/hent-siste-opplysninger-om-arbeidssoeker-med-profilering")
                                .contentType(APPLICATION_JSON)
                                .content(JsonUtils.toJson(new PersonFraPdlRequest(Fnr.of(fnr), null)))
                                .header("test_ident", navAnsatt.getNavIdent())
                                .header("test_ident_type", "INTERN")
                )
                .andExpect(status().is(204))
                .andExpect(content().string(""));

    }

    @Test
    void returnerer_null_dersom_person_kun_har_avsluttede_arbeidssoekerperioder() throws Exception {
        PrivatBruker ny = navContext.getPrivatBrukere().ny();
        NavAnsatt navAnsatt = navContext.getNavAnsatt().nyFor(ny);
        String fnr = ny.getNorskIdent();

        String expectedJsonArbeidssoekerPeriode = """
                    [
                      {
                        "periodeId": "ea0ad984-8b99-4fff-afd6-07737ab19d16",
                        "startet": {
                          "tidspunkt": "2024-04-23T13:04:40.739Z",
                          "utfoertAv": {
                            "type": "SLUTTBRUKER"
                          },
                          "kilde": "europe-north1-docker.pkg.dev/nais-management-233d/paw/paw-arbeidssokerregisteret-api-inngang:24.04.23.118-1",
                          "aarsak": "Er over 18 år, er bosatt i Norge i hendhold Folkeregisterloven"
                        },
                        "avsluttet": {
                          "tidspunkt": "2024-05-23T13:04:40.739Z",
                          "utfoertAv": {
                            "type": "VEILEDER",
                            "id": "Z123456"
                          },
                          "kilde": "europe-north1-docker.pkg.dev/nais-management-233d/paw/paw-arbeidssokerregisteret-api-inngang:24.04.23.118-1",
                          "aarsak": "Er over 18 år, er bosatt i Norge i hendhold Folkeregisterloven"
                        }
                      }
                    ]
                """.trim();

        String expectedJsonOpplysningerOmArbeidssoeker = "[" +
                "  {" +
                "    \"opplysningerOmArbeidssoekerId\": \"913161a3-dde9-4448-abf8-2a01a043f8cd\"," +
                "    \"periodeId\": \"ea0ad984-8b99-4fff-afd6-07737ab19d16\"," +
                "    \"sendtInnAv\": {" +
                "      \"tidspunkt\": \"2024-04-23T13:22:58.089Z\"," +
                "      \"utfoertAv\": {" +
                "        \"type\": \"SLUTTBRUKER\"" +
                "      }," +
                "      \"kilde\": \"paw-arbeidssoekerregisteret-inngang\"," +
                "      \"aarsak\": \"opplysning om arbeidssøker sendt inn\"" +
                "    }," +
                "    \"jobbsituasjon\": [" +
                "      {" +
                "        \"beskrivelse\": \"ALDRI_HATT_JOBB\"," +
                "        \"detaljer\": { \"prosent\":  \"25\" }" +
                "      }," +
                "      {" +
                "        \"beskrivelse\": \"ER_PERMITTERT\"," +
                "        \"detaljer\": { \"prosent\":  \"75\" }" +
                "      }" +
                "    ]," +
                "    \"utdanning\": {" +
                "      \"nus\": \"3\"," +
                "      \"bestaatt\": \"JA\"," +
                "      \"godkjent\": \"JA\"" +
                "    }," +
                "    \"helse\": {" +
                "      \"helsetilstandHindrerArbeid\": \"NEI\"" +
                "    }," +
                "    \"annet\": {" +
                "      \"andreForholdHindrerArbeid\": \"NEI\"" +
                "    }" +
                "  }" +
                "]";

        String expectedJsonProfilering = "[" +
                "  {" +
                "    \"profileringId\": \"7c9a2feb-0b31-4101-825a-0c4a3d465e01\"," +
                "    \"periodeId\": \"ea0ad984-8b99-4fff-afd6-07737ab19d16\"," +
                "    \"opplysningerOmArbeidssoekerId\": \"913161a3-dde9-4448-abf8-2a01a043f8cd\"," +
                "    \"sendtInnAv\": {" +
                "      \"tidspunkt\": \"2024-04-25T08:19:53.612Z\"," +
                "      \"utfoertAv\": {" +
                "        \"type\": \"SYSTEM\"" +
                "      }," +
                "      \"kilde\": \"null-null\"," +
                "      \"aarsak\": \"opplysninger-mottatt\"" +
                "    }," +
                "    \"profilertTil\": \"OPPGITT_HINDRINGER\"," +
                "    \"jobbetSammenhengendeSeksAvTolvSisteManeder\": false," +
                "    \"alder\": 33" +
                "  }" +
                "]";

        stubFor(WireMock.post(urlEqualTo("/api/v1/veileder/arbeidssoekerperioder"))
                .willReturn(ok()
                        .withHeader("Nav-Consumer-Id", "veilarbperson")
                        .withBody(expectedJsonArbeidssoekerPeriode)));

        stubFor(WireMock.post(urlEqualTo("/api/v1/veileder/opplysninger-om-arbeidssoeker"))
                .willReturn(ok()
                        .withHeader("Nav-Consumer-Id", "veilarbperson")
                        .withBody(expectedJsonOpplysningerOmArbeidssoeker)));

        stubFor(WireMock.post(urlEqualTo("/api/v1/veileder/profilering"))
                .willReturn(ok()
                        .withHeader("Nav-Consumer-Id", "veilarbperson")
                        .withBody(expectedJsonProfilering)));

        mockMvc
                .perform(
                        post("/api/v3/person/hent-siste-opplysninger-om-arbeidssoeker-med-profilering")
                                .contentType(APPLICATION_JSON)
                                .content(JsonUtils.toJson(new PersonFraPdlRequest(Fnr.of(fnr), null)))
                                .header("test_ident", navAnsatt.getNavIdent())
                                .header("test_ident_type", "INTERN")
                )
                .andExpect(status().is(204))
                .andExpect(content().string(""));

    }

    @Test
    void returnerer_siste_aktive_arbeidssoekerperiode_paa_bruker() throws Exception {
        PrivatBruker ny = navContext.getPrivatBrukere().ny();
        NavAnsatt navAnsatt = navContext.getNavAnsatt().nyFor(ny);
        String fnr = ny.getNorskIdent();

        String expectedJsonArbeidssoekerPeriode = """
                    [
                      {
                        "periodeId": "ea0ad984-8b99-4fff-afd6-07737ab19d16",
                        "startet": {
                          "tidspunkt": "2024-04-23T13:04:40.739Z",
                          "utfoertAv": {
                            "type": "SLUTTBRUKER"
                          },
                          "kilde": "europe-north1-docker.pkg.dev/nais-management-233d/paw/paw-arbeidssokerregisteret-api-inngang:24.04.23.118-1",
                          "aarsak": "Er over 18 år, er bosatt i Norge i hendhold Folkeregisterloven"
                        },
                        "avsluttet": null
                      }
                    ]
                """.trim();

        stubFor(WireMock.post(urlEqualTo("/api/v1/veileder/arbeidssoekerperioder"))
                .willReturn(ok()
                        .withHeader("Nav-Consumer-Id", "veilarbperson")
                        .withBody(expectedJsonArbeidssoekerPeriode)));

        String expectedJson = """
                    {
                        "periodeId": "ea0ad984-8b99-4fff-afd6-07737ab19d16",
                        "startet": {
                            "tidspunkt":"2024-04-23T13:04:40.739Z",
                            "utfoertAv": {
                                "type": "SLUTTBRUKER",
                                "id": null
                            },
                            "kilde": "europe-north1-docker.pkg.dev/nais-management-233d/paw/paw-arbeidssokerregisteret-api-inngang:24.04.23.118-1",
                            "aarsak": "Er over 18 år, er bosatt i Norge i hendhold Folkeregisterloven"
                        },
                        "avsluttet":null
                    }
                """.trim();

        mockMvc
                .perform(
                        post("/api/v3/person/hent-siste-aktiv-arbeidssoekerperiode")
                                .contentType(APPLICATION_JSON)
                                .content(JsonUtils.toJson(new PersonFraPdlRequest(Fnr.of(fnr), null)))
                                .header("test_ident", navAnsatt.getNavIdent())
                                .header("test_ident_type", "INTERN")
                )
                .andExpect(status().is(200))
                .andExpect(content().json(expectedJson));
    }

    @Test
    void returnerer_204_om_bruker_ikke_har_aktiv_arbeidssoekerperiode() throws Exception {
        PrivatBruker ny = navContext.getPrivatBrukere().ny();
        NavAnsatt navAnsatt = navContext.getNavAnsatt().nyFor(ny);
        String fnr = ny.getNorskIdent();

        String expectedJsonArbeidssoekerPeriode = """
                    [
                      {
                        "periodeId": "ea0ad984-8b99-4fff-afd6-07737ab19d16",
                        "startet": {
                          "tidspunkt": "2023-04-23T13:04:40.739Z",
                          "utfoertAv": {
                            "type": "SLUTTBRUKER"
                          },
                          "kilde": "europe-north1-docker.pkg.dev/nais-management-233d/paw/paw-arbeidssokerregisteret-api-inngang:24.04.23.118-1",
                          "aarsak": "Er over 18 år, er bosatt i Norge i hendhold Folkeregisterloven"
                        },
                        "avsluttet": {
                          "tidspunkt": "2024-04-23T13:04:40.739Z",
                          "utfoertAv": {
                            "type": "SLUTTBRUKER"
                          },
                          "kilde": "europe-north1-docker.pkg.dev/nais-management-233d/paw/paw-arbeidssokerregisteret-api-inngang:24.04.23.118-1",
                          "aarsak": "Er over 18 år, er bosatt i Norge i hendhold Folkeregisterloven"
                        }
                      }
                    ]
                """.trim();

        stubFor(WireMock.post(urlEqualTo("/api/v1/veileder/arbeidssoekerperioder"))
                .willReturn(ok()
                        .withHeader("Nav-Consumer-Id", "veilarbperson")
                        .withBody(expectedJsonArbeidssoekerPeriode)));

        mockMvc
                .perform(
                        post("/api/v3/person/hent-siste-aktiv-arbeidssoekerperiode")
                                .contentType(APPLICATION_JSON)
                                .content(JsonUtils.toJson(new PersonFraPdlRequest(Fnr.of(fnr), null)))
                                .header("test_ident", navAnsatt.getNavIdent())
                                .header("test_ident_type", "INTERN")
                )
                .andExpect(status().is(204));
    }

    @Test
    void test_av_hent_navn() throws Exception {
        PrivatBruker ny = navContext.getPrivatBrukere().ny();
        NavAnsatt navAnsatt = navContext.getNavAnsatt().nyFor(ny);
        when(personV2Service.hentNavn(new PersonFraPdlRequest(TEST_FNR, "B555"))).thenReturn(new PersonNavnV2().setFornavn("Knut").setMellomnavn("Roger").setEtternavn("Knutsen").setForkortetNavn("Knut Knutsen"));

        String expectedJson = "{\"fornavn\":\"Knut\",\"mellomnavn\":\"Roger\",\"etternavn\":\"Knutsen\",\"forkortetNavn\":\"Knut Knutsen\"}";

        mockMvc
                .perform(
                        post("/api/v3/person/hent-navn")
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

    @Test
    void test_av_hent_foedselsdato() throws Exception {
        PrivatBruker ny = navContext.getPrivatBrukere().ny();
        NavAnsatt navAnsatt = navContext.getNavAnsatt().nyFor(ny);
        when(personV2Service.hentFoedselsdato(new PersonFraPdlRequest(TEST_FNR, "B555")))
                .thenReturn(new Foedselsdato(LocalDate.of(1990, 1, 1), 1990));

        String expectedJson = "{\"foedselsdato\":\"1990-01-01\",\"foedselsaar\":1990}";

        mockMvc
                .perform(
                        post("/api/v3/person/hent-foedselsdato")
                                .contentType(APPLICATION_JSON)
                                .content(JsonUtils.toJson(new PersonFraPdlRequest(TEST_FNR, "B555")))
                                .header(ACCEPT, APPLICATION_JSON_VALUE)
                                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                .header("test_ident", navAnsatt.getNavIdent())
                                .header("test_ident_type", "INTERN")
                )
                .andExpect(content().string(expectedJson))
                .andExpect(status().is(200));
    }

}
