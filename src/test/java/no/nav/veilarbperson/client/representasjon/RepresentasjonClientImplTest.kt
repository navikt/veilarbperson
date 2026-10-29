package no.nav.veilarbperson.client.representasjon

import no.nav.common.types.identer.Fnr
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

class RepresentasjonClientImplTest {
    private var status = 0
    private lateinit var body: String
    private lateinit var sisteRequest: Request

    private fun client(): RepresentasjonClientImpl {
        val httpClient = OkHttpClient.Builder().addInterceptor { chain ->
            sisteRequest = chain.request()
            Response.Builder()
                .request(sisteRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(status)
                .message("Test response")
                .body(body.toResponseBody("application/json".toMediaType()))
                .build()
        }.build()
        return RepresentasjonClientImpl("http://representasjon.test", { "TEST_TOKEN" }, httpClient)
    }

    private fun svar(status: Int, body: String) {
        this.status = status
        this.body = body
    }

    @Test
    fun senderPostMedIdentOgOboTokenOgParserV2() {
        svar(200, "[$FULLMAKT]")

        val fullmakter = client().hentFullmakt(FNR)

        assertEquals(1, fullmakter.size)
        val fullmakt = fullmakter.first()
        assertEquals(Fnr.of(FNR), fullmakt.fullmaktsgiver)
        assertEquals(Fnr.of("02020212345"), fullmakt.fullmektig)
        assertEquals(LocalDate.of(2026, 1, 1), fullmakt.gyldigFraOgMed)
        assertNull(fullmakt.gyldigTilOgMed)
        assertEquals(listOf("AAP", "DAG"), fullmakt.leserettigheter)
        assertEquals(listOf("DAG"), fullmakt.skriverettigheter)
        assertFalse(fullmakt.toString().contains(FNR))
        assertFalse(fullmakt.toString().contains("02020212345"))
        assertEquals(PATH, sisteRequest.url.encodedPath)
        assertNull(sisteRequest.url.query)
        assertEquals("POST", sisteRequest.method)
        assertEquals("Bearer TEST_TOKEN", sisteRequest.header("Authorization"))
        assertEquals("application/json", sisteRequest.header("Accept"))
        val requestBody = requireNotNull(sisteRequest.body)
        assertEquals("application/json; charset=utf-8", requestBody.contentType().toString())
        val buffer = Buffer()
        requestBody.writeTo(buffer)
        assertEquals("{\"ident\":\"$FNR\"}", buffer.readUtf8())
    }

    @Test
    fun parserSluttdatoOgTommeRettigheter() {
        svar(
            200, "[" + FULLMAKT.replace("\"gyldigTilOgMed\":null", "\"gyldigTilOgMed\":\"2027-02-01\"")
                .replace("[\"AAP\",\"DAG\"]", "[]").replace("[\"DAG\"]", "[]") + "]"
        )

        val fullmakt = client().hentFullmakt(FNR).first()

        assertEquals(LocalDate.of(2027, 2, 1), fullmakt.gyldigTilOgMed)
        assertTrue(fullmakt.leserettigheter.isEmpty())
        assertTrue(fullmakt.skriverettigheter.isEmpty())
    }

    @Test
    fun tomListeErGyldig() {
        svar(200, "[]")
        assertTrue(client().hentFullmakt(FNR).isEmpty())
    }

    @Test
    fun manglendeRequiredFeltFeilerOgsaaForNullableSluttdato() {
        for (felt in listOf(
            "\"fullmaktsgiver\":\"01010112345\",",
            "\"fullmektig\":\"02020212345\",",
            "\"gyldigFraOgMed\":\"2026-01-01\",",
            "\"gyldigTilOgMed\":null,",
            "\"leserettigheter\":[\"AAP\",\"DAG\"],",
            "\"skriverettigheter\":[\"DAG\"],"
        )) {
            svar(200, "[" + FULLMAKT.replace(felt, "") + "]")
            assertUgyldigRespons()
        }
    }

    @Test
    fun nullRequiredFeltOgUgyldigeVerdierFeilerUtenPersondata() {
        for (body in listOf(
            FULLMAKT.replace("\"fullmaktsgiver\":\"01010112345\"", "\"fullmaktsgiver\":null"),
            FULLMAKT.replace("\"fullmektig\":\"02020212345\"", "\"fullmektig\":null"),
            FULLMAKT.replace("\"2026-01-01\"", "null"),
            FULLMAKT.replace("\"2026-01-01\"", "\"ugyldig-sensitiv-verdi\""),
            FULLMAKT.replace("[\"AAP\",\"DAG\"]", "null"),
            FULLMAKT.replace("[\"AAP\",\"DAG\"]", "[null]"),
            FULLMAKT.replace("[\"AAP\",\"DAG\"]", "{}"),
            FULLMAKT.replace("[\"DAG\"]", "null"),
            FULLMAKT.replace("[\"DAG\"]", "[null]"),
            FULLMAKT.replace("[\"DAG\"]", "{}")
        )) {
            svar(200, "[$body]")
            assertUgyldigRespons()
        }
    }

    @Test
    fun feilResponsformOgManglendeBodyFeiler() {
        for (body in listOf("", "null", "{}", "[null]", "[", "[\"sensitiv-verdi\"]")) {
            svar(200, body)
            val feil = assertThrows(IllegalStateException::class.java) { client().hentFullmakt(FNR) }
            assertFalse(feil.toString().contains(FNR))
            assertNull(feil.cause)
        }
        svar(204, "")
        assertThrows(IllegalStateException::class.java) { client().hentFullmakt(FNR) }
    }

    @Test
    fun httpFeilGirIkkeTomListeEllerLekkasjerFraFeilbody() {
        for (status in listOf(400, 403, 500)) {
            svar(status, "sensitiv-feilbody")
            val feil = assertThrows(RuntimeException::class.java) { client().hentFullmakt(FNR) }
            assertFalse(feil.toString().contains("sensitiv-feilbody"))
            assertFalse(feil.toString().contains(FNR))
            assertNull(feil.cause)
        }
    }

    @Test
    fun transportfeilPropageres() {
        val feil = IOException("Representasjon utilgjengelig")
        val httpClient = OkHttpClient.Builder().addInterceptor { throw feil }.build()
        val client = RepresentasjonClientImpl("http://representasjon.test", { "TEST_TOKEN" }, httpClient)

        assertSame(feil, assertThrows(IOException::class.java) { client.hentFullmakt(FNR) })
    }

    @Test
    fun parserTommeOgBlankeTemaerUtenEgenValidering() {
        svar(
            200, "[" + FULLMAKT.replace("[\"AAP\",\"DAG\"]", "[\"\",\" \",\"\u00a0\"]")
                .replace("[\"DAG\"]", "[\"\",\" \",\"\u00a0\"]") + "]"
        )

        val fullmakt = client().hentFullmakt(FNR).first()

        assertEquals(listOf("", " ", "\u00a0"), fullmakt.leserettigheter)
        assertEquals(listOf("", " ", "\u00a0"), fullmakt.skriverettigheter)
    }

    @Test
    fun ioExceptionErDeklarertForJavaKallere() {
        for (type in listOf(RepresentasjonClient::class.java, RepresentasjonClientImpl::class.java)) {
            assertTrue(type.getMethod("hentFullmakt", String::class.java).exceptionTypes.contains(IOException::class.java))
        }
    }

    private fun assertUgyldigRespons() {
        val feil = assertThrows(IllegalStateException::class.java) { client().hentFullmakt(FNR) }
        assertEquals("Ugyldig fullmaktsrespons fra representasjon", feil.message)
        assertNull(feil.cause)
    }

    companion object {
        private const val PATH = "/api/v2/internbruker/fullmakt/bruker-som-fullmaktsgiver/alle-fullmakter"
        private const val FNR = "01010112345"
        private val FULLMAKT = """
            {
              "fullmaktId":"fd915799-88bf-4f19-b8c3-d2a547be6730",
              "fullmaktsgiver":"01010112345",
              "fullmektig":"02020212345",
              "gyldigFraOgMed":"2026-01-01",
              "gyldigTilOgMed":null,
              "leserettigheter":["AAP","DAG"],
              "skriverettigheter":["DAG"],
              "endringslogg":[{"ubrukt":"metadata"}]
            }
        """.trimIndent()
    }
}
