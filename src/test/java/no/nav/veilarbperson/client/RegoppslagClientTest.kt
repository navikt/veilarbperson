package no.nav.veilarbperson.client

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit.WireMockRule
import no.nav.common.auth.context.AuthContextHolderThreadLocal
import no.nav.common.auth.context.UserRole
import no.nav.common.test.auth.AuthTestUtils.createAuthContext
import no.nav.common.utils.fn.UnsafeSupplier
import no.nav.veilarbperson.client.regoppslag.RegoppslagClient
import no.nav.veilarbperson.client.regoppslag.RegoppslagClientImpl
import no.nav.veilarbperson.client.regoppslag.RegoppslagResponseDTO
import no.nav.veilarbperson.utils.TestData.TEST_FNR
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RegoppslagClientTest {

    lateinit var regoppslagClient: RegoppslagClient

    private val wireMockRule = WireMockRule()

    @Rule
    fun getWireMockRule() = wireMockRule

    @Before
    fun setup() {
        val wiremockUrl = "http://localhost:" + getWireMockRule().port()
        regoppslagClient = RegoppslagClientImpl(wiremockUrl) { "M2M_TOKEN" }
    }

    @Test
    fun `regoppslag gir forventet innhold i request og response`() {
        val forventetRequest =
            """
                {
                  "ident": "$TEST_FNR",
                  "tema": "OPP"
                }
            """.trimIndent()

        val responsJson =
            """
                {
                  "navn": "Navn Navnesen",
                  "adresse": {
                    "type": "NORSKPOSTADRESSE",
                    "adresselinje1": "Adresselinje 1",
                    "adresselinje2": "Adresselinje 2",
                    "adresselinje3": "Adresselinje 3",
                    "postnummer": "0000",
                    "poststed": "Sted",
                    "landkode": "NO",
                    "land": "Norge"
                  }
                }
            """.trimIndent()

        WireMock.givenThat(
            WireMock.post(WireMock.urlEqualTo("/rest/postadresse"))
                .withRequestBody(WireMock.equalToJson(forventetRequest))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withBody(responsJson)
                )
        )

        val respons =
            AuthContextHolderThreadLocal
                .instance()
                .withContext(createAuthContext(UserRole.INTERN, "SUBJECT"), UnsafeSupplier {
                    regoppslagClient.hentPostadresse(
                        (TEST_FNR)
                    )
                })

        val forventetRespons = RegoppslagResponseDTO(
            navn = "Navn Navnesen",
            adresse = RegoppslagResponseDTO.Adresse(
                type = RegoppslagResponseDTO.AdresseType.NORSKPOSTADRESSE,
                adresselinje1 = "Adresselinje 1",
                adresselinje2 = "Adresselinje 2",
                adresselinje3 = "Adresselinje 3",
                postnummer = "0000",
                poststed = "Sted",
                landkode = "NO",
                land = "Norge"
            )
        )

        assertEquals(forventetRespons, respons)
    }
}
