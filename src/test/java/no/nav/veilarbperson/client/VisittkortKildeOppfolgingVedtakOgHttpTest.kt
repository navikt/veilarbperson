package no.nav.veilarbperson.client

import graphql.ExecutionInput
import graphql.GraphQL
import no.nav.common.json.JsonUtils
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.ProfilertTil
import no.nav.veilarbperson.client.veilarbdialog.VisittkortVarselClientImpl
import no.nav.veilarbperson.client.veilarbvedtaksstotte.VisittkortVedtakClientImpl
import no.nav.veilarbperson.utils.TestData.TEST_FNR
import okhttp3.OkHttpClient
import okhttp3.Request
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import tools.jackson.core.type.TypeReference
import java.io.IOException

class VisittkortKildeOppfolgingVedtakOgHttpTest : VisittkortKildeClientTestBase() {

    @Test
    fun `eskaleringsvarsel 204 er fravaer men 404 er ikke det`() {
        val utenVarsel = VisittkortVarselClientImpl("http://dialog.test/veilarbdialog", { "test-token" },
            http(204 to ""))
        assertThat(utenVarsel.harGjeldendeEskaleringsvarsel(TEST_FNR)).isFalse()

        val feilRute = VisittkortVarselClientImpl("http://dialog.test/veilarbdialog", { "test-token" },
            http(404 to "sensitive-source-message"))
        assertThat(assertThrows<VisittkortKildeHttpException> {
            feilRute.harGjeldendeEskaleringsvarsel(TEST_FNR)
        }.statusCode.value()).isEqualTo(404)
    }

    @ParameterizedTest
    @MethodSource("ugyldigeEskaleringsvarselBody")
    fun `ugyldig eskaleringsvarsel gir ikke positiv etikett`(body: String) {
        val client = VisittkortVarselClientImpl("http://dialog.test/veilarbdialog", { "test-token" },
            http(200 to body))
        assertThrows<VisittkortKildeKontraktException> {
            client.harGjeldendeEskaleringsvarsel(TEST_FNR)
        }
    }

    @ParameterizedTest(name = "status {0} gir utilgjengelig")
    @ValueSource(ints = [500, 502, 503])
    fun `ekte HTTP 5xx gir utilgjengelig uten aa inkludere feilbody`(status: Int) {
        val request = Request.Builder().url("http://source.test").build()
        val feil = assertThrows<VisittkortKildeUtilgjengeligException> {
            http(status to "sensitive-body").hent(request)
        }
        assertThat(feil.toString()).doesNotContain("sensitive")
    }

    @ParameterizedTest(name = "status {0} gir HTTP-feil")
    @ValueSource(ints = [400, 401, 403, 404, 429])
    fun `ikke-5xx feilstatus gir HTTP-feil uten aa inkludere feilbody`(status: Int) {
        val request = Request.Builder().url("http://source.test").build()
        val feil = assertThrows<VisittkortKildeHttpException> { http(status to "sensitive-body").hent(request) }
        assertThat(feil.statusCode.value()).isEqualTo(status)
        assertThat(feil.toString()).doesNotContain("sensitive")
    }

    @ParameterizedTest(name = "status {0} er kontraktsfeil")
    @ValueSource(ints = [201, 202, 204, 206])
    fun `uventet 2xx-status er kontraktsfeil`(status: Int) {
        val request = Request.Builder().url("http://source.test").build()
        assertThrows<VisittkortKildeKontraktException> {
            http(status to "{}").hent(request)
        }
    }

    @Test
    fun `transportfeil beholdes som transportfeil og parsingsfeil lekker ikke data`() {
        val request = Request.Builder().url("http://source.test").build()
        val transportfeil = IOException("source unavailable")
        val client = VisittkortKildeHttpClient(
            OkHttpClient.Builder().addInterceptor { throw transportfeil }.build()
        )
        assertThat(assertThrows<IOException> { client.hent(request) }).isSameAs(transportfeil)
        val feil = assertThrows<VisittkortKildeKontraktException> {
            http(200 to "sensitive-invalid-json").hent(request)
        }
        assertThat(feil.toString()).doesNotContain("sensitive")
        assertThat(feil.cause).isNull()
    }

    @Test
    fun `KVP bruker eksisterende GraphQL endpoint OBO og bare kontorsperrefeltet`() {
        for (underKvp in listOf(true, false)) {
            val client = kvp(200 to """{"data":{"brukerStatus":{"erKontorsperret":$underKvp}}}""")

            assertThat(client.hentKvpStatus(TEST_FNR)).isEqualTo(underKvp)

            val request = requests.last()
            assertThat(request.method).isEqualTo("POST")
            assertThat(request.url.encodedPath).isEqualTo("/veilarboppfolging/api/graphql")
            assertThat(request.url.query).isNull()
            assertThat(request.header("Authorization")).isEqualTo("Bearer test-token")
            assertThat(request.header("behandlingsnummer")).isNull()
            val body = JsonUtils.getMapper().readTree(request.bodyTekst())
            assertThat(body.path("variables").path("fnr").stringValue()).isEqualTo(TEST_FNR.get())
            assertThat(body.path("query").stringValue()).contains("erKontorsperret", "@include")
                .doesNotContain("veilederIdent", TEST_FNR.get())
            val variables = JsonUtils.getMapper().convertValue(
                body.path("variables"), object : TypeReference<Map<String, Any>>() {}
            )
            val kildeResultat = GraphQL.newGraphQL(VisittkortKildeClientTestBase.OPPFOLGING_SCHEMA).build().execute(
                ExecutionInput.newExecutionInput()
                    .query(body.path("query").stringValue())
                    .variables(variables)
                    .root(mapOf("brukerStatus" to mapOf("erKontorsperret" to underKvp)))
                    .build()
            )
            assertThat(kildeResultat.errors).isEmpty()
            assertThat(kildeResultat.getData<Map<String, Any>>())
                .isEqualTo(mapOf("brukerStatus" to mapOf("erKontorsperret" to underKvp)))
        }
        assertThat(requests).hasSize(2)
    }

    @Test
    fun `KVP tillater tom feilliste med gyldig data men ikke delvis GraphQL feil`() {
        val gyldig = kvp(200 to """{"errors":[],"data":{"brukerStatus":{"erKontorsperret":true}}}""")
        assertThat(gyldig.hentKvpStatus(TEST_FNR)).isTrue()

        val feil = kvp(200 to """
                {"errors":[{"message":"sensitive-source-message"}],
                 "data":{"brukerStatus":{"erKontorsperret":true}}}
            """.trimIndent())
        assertThat(assertThrows<VisittkortKildeKontraktException> { feil.hentKvpStatus(TEST_FNR) }.toString())
            .doesNotContain("sensitive-source-message")
    }

    @ParameterizedTest
    @MethodSource("ugyldigeKvpBodyer")
    fun `manglende eller ugyldig KVP boolsk verdi gir feil og ikke fravaer`(body: String) {
        val client = kvp(200 to body)
        assertThrows<VisittkortKildeKontraktException> { client.hentKvpStatus(TEST_FNR) }
    }

    @ParameterizedTest(name = "status {0} er ikke gyldig fravaer")
    @ValueSource(ints = [401, 403, 404])
    fun `KVP HTTP tilgangsfeil og 404 er ikke gyldig fravaer`(status: Int) {
        val client = kvp(status to "sensitive-source-message")
        val feil = assertThrows<VisittkortKildeHttpException> { client.hentKvpStatus(TEST_FNR) }
        assertThat(feil.statusCode.value()).isEqualTo(status)
        assertThat(feil.toString()).doesNotContain("sensitive-source-message")
    }

    @Test
    fun `KVP 503 er utilgjengelig kilde`() {
        val utilgjengelig = kvp(503 to "source unavailable")
        assertThrows<VisittkortKildeUtilgjengeligException> { utilgjengelig.hentKvpStatus(TEST_FNR) }
    }

    @Test
    fun `vedtak bruker intern rute med fagsystemkontroll og OBO`() {
        val client = VisittkortVedtakClientImpl("http://vedtak.test/veilarbvedtaksstotte", { "test-token" },
            http(200 to """
                {"innsatsgruppe":"GODE_MULIGHETER","hovedmal":"SKAFFE_ARBEID","fattetDato":"2026-01-01"}
            """.trimIndent()))

        assertThat(client.harGjeldendeVedtak(TEST_FNR)).isTrue()
        val request = requests.single()
        assertThat(request.method).isEqualTo("POST")
        assertThat(request.url.encodedPath).isEqualTo("/veilarbvedtaksstotte/api/hent-gjeldende-14a-vedtak")
        assertThat(request.url.encodedPath).doesNotContain("/ekstern/")
        assertThat(request.url.query).isNull()
        assertThat(request.header("Authorization")).isEqualTo("Bearer test-token")
        assertThat(request.header("behandlingsnummer")).isNull()
        assertThat(request.bodyTekst()).isEqualTo("""{"fnr":"${TEST_FNR.get()}"}""")
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "null"])
    fun `vedtak HTTP 200 tom body eller JSON null er gyldig fravaer`(body: String) {
        val client = VisittkortVedtakClientImpl("http://vedtak.test/veilarbvedtaksstotte", { "test-token" },
            http(200 to body))
        assertThat(client.harGjeldendeVedtak(TEST_FNR)).isFalse()
    }

    @Test
    fun `vedtakseksistens er uavhengig av innsatsgruppekategori og ubrukte DTO felter`() {
        val client = VisittkortVedtakClientImpl("http://vedtak.test/veilarbvedtaksstotte", { "test-token" },
            http(200 to """{"innsatsgruppe":"EN_NY_KATEGORI","hovedmal":null}"""))

        assertThat(client.harGjeldendeVedtak(TEST_FNR)).isTrue()
    }

    @ParameterizedTest
    @MethodSource("ugyldigeVedtaksbodyer")
    fun `ugyldig vedtaksrespons er ikke fravaer`(body: String) {
        val client = VisittkortVedtakClientImpl("http://vedtak.test/veilarbvedtaksstotte", { "test-token" },
            http(200 to body))
        assertThrows<VisittkortKildeKontraktException> { client.harGjeldendeVedtak(TEST_FNR) }
    }

    @ParameterizedTest(name = "status {0} blir ikke tolket som ingen vedtak")
    @ValueSource(ints = [401, 403, 404])
    fun `feilede vedtakskall blir ikke tolket som ingen vedtak`(status: Int) {
        val client = VisittkortVedtakClientImpl("http://vedtak.test/veilarbvedtaksstotte", { "test-token" },
            http(status to "sensitive-source-message"))
        val feil = assertThrows<VisittkortKildeHttpException> { client.harGjeldendeVedtak(TEST_FNR) }
        assertThat(feil.statusCode.value()).isEqualTo(status)
        assertThat(feil.toString()).doesNotContain("sensitive-source-message")
    }

    @Test
    fun `503 og 204 vedtak blir ikke tolket som ingen vedtak`() {
        val utilgjengelig = VisittkortVedtakClientImpl("http://vedtak.test/veilarbvedtaksstotte", { "test-token" },
            http(503 to "source unavailable"))
        assertThrows<VisittkortKildeUtilgjengeligException> {
            utilgjengelig.harGjeldendeVedtak(TEST_FNR)
        }
        val uventetFravaersstatus = VisittkortVedtakClientImpl("http://vedtak.test/veilarbvedtaksstotte", { "test-token" },
            http(204 to ""))
        assertThrows<VisittkortKildeKontraktException> {
            uventetFravaersstatus.harGjeldendeVedtak(TEST_FNR)
        }
    }

    @Test
    fun `tom HTTP 200 body tillates bare ved eksplisitt kildekontrakt`() {
        val request = Request.Builder().url("http://source.test").build()
        assertThrows<VisittkortKildeKontraktException> { http(200 to "").hent(request) }
        assertThat(http(200 to "").hent(request, tomBodyErFravaer = true)).isNull()
    }

    companion object {
        @JvmStatic
        fun ugyldigeEskaleringsvarselBody() = listOf(
            "", "null", "[]", "{}", """{"id":1}""",
            """{"id":null,"tilhorendeDialogId":2}""",
            """{"id":1.5,"tilhorendeDialogId":2}""",
            """{"id":1,"tilhorendeDialogId":"2"}""",
            """{"id":9223372036854775808,"tilhorendeDialogId":2}"""
        )

        @JvmStatic
        fun ugyldigeKvpBodyer() = listOf(
            "", "null", "{}",
            """{"data":null}""",
            """{"data":{"brukerStatus":null}}""",
            """{"data":{"brukerStatus":{}}}""",
            """{"data":{"brukerStatus":{"erKontorsperret":null}}}""",
            """{"data":{"brukerStatus":{"erKontorsperret":"false"}}}""",
            """{"data":{"brukerStatus":{"erKontorsperret":0}}}""",
            """{"errors":{},"data":{"brukerStatus":{"erKontorsperret":true}}}"""
        )

        @JvmStatic
        fun ugyldigeVedtaksbodyer() = listOf(
            "invalid-json", " ", "[]", "true", "\"null\"", "{}",
            """{"innsatsgruppe":null}""",
            """{"innsatsgruppe":123}""",
            """{"innsatsgruppe":""}""",
            """{"innsatsgruppe":" "}"""
        )
    }

}
