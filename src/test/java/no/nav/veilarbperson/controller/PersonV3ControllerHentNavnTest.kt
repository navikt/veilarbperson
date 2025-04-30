package no.nav.veilarbperson.controller

import no.nav.common.json.JsonUtils
import no.nav.poao_tilgang.poao_tilgang_test_core.NavContext
import no.nav.veilarbperson.config.ApplicationTestConfig
import no.nav.veilarbperson.controller.v3.PersonV3Controller
import no.nav.veilarbperson.domain.PersonFraPdlRequest
import no.nav.veilarbperson.domain.PersonNavnV2
import no.nav.veilarbperson.service.AuthService
import no.nav.veilarbperson.service.PersonV2Service
import no.nav.veilarbperson.utils.TestData
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers


@WebMvcTest(controllers = [PersonV3Controller::class])
@Import(
    ApplicationTestConfig::class
)
@DirtiesContext
class PersonV3ControllerHentNavnTest {

    @Autowired
    private val mockMvc: MockMvc? = null

    @Autowired
    private val navContext: NavContext? = null

    @MockitoBean
    private lateinit var authService: AuthService

    @MockitoBean
    private lateinit var personV2Service: PersonV2Service


    @Test
    @Throws(Exception::class)
    fun test_av_hent_navn() {

        val ny = navContext?.privatBrukere?.ny()
        val navAnsatt = ny?.let { navContext?.navAnsatt?.nyFor(it) }
        Mockito.`when`(authService.harLesetilgang(TestData.TEST_FNR)).thenReturn(true)
        Mockito.`when`(personV2Service.hentNavn(PersonFraPdlRequest(TestData.TEST_FNR, "B555"))).thenReturn(
            PersonNavnV2().setFornavn("Knut").setMellomnavn("Roger").setEtternavn("Knutsen")
                .setForkortetNavn("Knut Knutsen")
        )

        val expectedJson =
            "{\"fornavn\":\"Knut\",\"mellomnavn\":\"Roger\",\"etternavn\":\"Knutsen\",\"forkortetNavn\":\"Knut Knutsen\"}"

        if (navAnsatt != null) {
            mockMvc
                ?.perform(
                    MockMvcRequestBuilders.post("/api/v3/person/hent-navn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(PersonFraPdlRequest(TestData.TEST_FNR, "B555")))
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header("test_ident", navAnsatt.navIdent)
                        .header("test_ident_type", "INTERN")
                )
                ?.andExpect(MockMvcResultMatchers.content().json(expectedJson))
                ?.andExpect(MockMvcResultMatchers.status().`is`(200))
        }
    }
}
