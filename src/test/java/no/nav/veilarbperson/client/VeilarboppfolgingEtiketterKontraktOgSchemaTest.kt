package no.nav.veilarbperson.client

import graphql.ExecutionInput
import graphql.GraphQL
import no.nav.common.json.JsonUtils
import no.nav.veilarbperson.client.veilarboppfolging.VeilarboppfolgingClientImpl
import no.nav.veilarbperson.domain.visittkort.OppfolgingEtikettgrunnlag
import no.nav.veilarbperson.utils.TestData.TEST_FNR
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.IOException

class VeilarboppfolgingEtiketterKontraktOgSchemaTest : VeilarboppfolgingEtiketterClientTestBase() {

    @Test
    fun `manglende null eller feiltypet etterspurte verdier gir kontraktsfeil og ikke false`() {
        data class Tilfelle(val felt: String, val ugyldige: List<String>)
        val tilfeller = listOf(
            Tilfelle("kvp", listOf(
                "{}", """{"brukerStatus":null}""", """{"brukerStatus":[]}""", """{"brukerStatus":{}}""",
                """{"brukerStatus":{"erKontorsperret":null}}""", """{"brukerStatus":{"erKontorsperret":"false"}}""",
                """{"brukerStatus":{"erKontorsperret":0}}"""
            )),
            Tilfelle("manuellOppfolging", listOf(
                """{"brukerStatus":{}}""", """{"brukerStatus":{"manuell":null}}""",
                """{"brukerStatus":{"manuell":{}}}""", """{"brukerStatus":{"manuell":{"erManuell":null}}}""",
                """{"brukerStatus":{"manuell":{"erManuell":"true"}}}"""
            )),
            Tilfelle("reservertKrr", listOf(
                """{"brukerStatus":{}}""", """{"brukerStatus":{"krr":null}}""", """{"brukerStatus":{"krr":{}}}""",
                """{"brukerStatus":{"krr":{"reservertIKrr":null}}}""",
                """{"brukerStatus":{"krr":{"reservertIKrr":1}}}"""
            )),
            Tilfelle("ikkeRegistrertKrr", listOf(
                """{"brukerStatus":{"krr":{}}}""", """{"brukerStatus":{"krr":{"registrertIKrr":null}}}""",
                """{"brukerStatus":{"krr":{"registrertIKrr":"false"}}}"""
            )),
            Tilfelle("utdatertIKrr", listOf(
                """{"brukerStatus":{"manuell":{"erManuell":true},"krr":{"reservertIKrr":true,"registrertIKrr":true}}}""",
                """{"brukerStatus":{"manuell":{"erManuell":true},"krr":{"kanVarsles":null,"reservertIKrr":true,"registrertIKrr":true}}}""",
                """{"brukerStatus":{"manuell":{"erManuell":true},"krr":{"kanVarsles":true,"registrertIKrr":true}}}""",
                """{"brukerStatus":{"manuell":{"erManuell":true},"krr":{"kanVarsles":true,"reservertIKrr":true}}}""",
                """{"brukerStatus":{"krr":{"kanVarsles":true,"reservertIKrr":true,"registrertIKrr":true}}}"""
            )),
            Tilfelle("sykmeldt", listOf(
                """{"brukerStatus":{}}""", """{"brukerStatus":{"sykmeldtStatus":true}}""",
                """{"brukerStatus":{"sykmeldtStatus":1}}""", """{"brukerStatus":{"sykmeldtStatus":{}}}"""
            )),
            Tilfelle("utmeldingskandidat", listOf(
                "{}", """{"utmeldingskandidat":null}""", """{"utmeldingskandidat":[]}""",
                """{"utmeldingskandidat":{}}"""
            )),
            Tilfelle("ikkeUnderOppfolging", listOf(
                "{}", """{"oppfolging":null}""", """{"oppfolging":{}}""",
                """{"oppfolging":{"erUnderOppfolging":null}}""", """{"oppfolging":{"erUnderOppfolging":"true"}}"""
            )),
            Tilfelle("trengerOppfolgingsvedtak", listOf(
                """{"oppfolging":{"erUnderOppfolging":null}}""", """{"oppfolging":{"erUnderOppfolging":1}}"""
            ))
        )

        for (tilfelle in tilfeller) {
            for (data in tilfelle.ugyldige) {
                assertThrows<VisittkortKildeKontraktException>(tilfelle.felt + " " + data) {
                    hent(setOf(tilfelle.felt), data)
                }
            }
        }
    }

    @Test
    fun `ugyldig rot data og GraphQL feil gir fast kontraktsfeil uten kildeinnhold`() {
        val gyldigData = """{"brukerStatus":{"erKontorsperret":true}}"""
        for (body in listOf(
            "", "null", "[]", "{}", """{"data":null}""", """{"data":[]}""", """{"data":"x"}""",
            """{"errors":{},"data":$gyldigData}""",
            """{"errors":"sensitive-source-message","data":$gyldigData}""",
            """{"errors":[{"message":"sensitive-source-message"}],"data":$gyldigData}""",
            """{"errors":[{"message":"sensitive-source-message"}],"data":null}""",
            """{"errors":[{"message":"sensitive-source-message"}]}"""
        )) {
            val feil = assertThrows<VisittkortKildeKontraktException>(body) {
                client(200 to body).hentEtikettgrunnlag(TEST_FNR, setOf("kvp"))
            }
            assertThat(feil.toString()).doesNotContain("sensitive-source-message")
            assertThat(feil.cause).isNull()
        }
        for (errors in listOf("", """"errors":null,""", """"errors":[],""")) {
            assertThat(client(200 to """{$errors"data":$gyldigData}""").hentEtikettgrunnlag(TEST_FNR, setOf("kvp")).kvp)
                .isTrue()
        }
    }

    @Test
    fun `tomt eller irrelevant utvalg avvises foer HTTP`() {
        val client = client()

        for (felter in listOf(emptySet(), setOf("ukjentFelt"), setOf("dod", "skjermet"))) {
            assertThrows<IllegalArgumentException> { client.hentEtikettgrunnlag(TEST_FNR, felter) }
        }
        assertThat(requests).isEmpty()
    }

    @Test
    fun `irrelevante felter ignoreres naar minst ett relevant felt er etterspurt`() {
        val resultat = hent(setOf("dod", "kvp"), """{"brukerStatus":{"erKontorsperret":true}}""")

        assertThat(resultat).isEqualTo(OppfolgingEtikettgrunnlag(kvp = true))
        assertThat(variabler()).isEqualTo(forventetVariabler("hentBrukerStatus", "hentKvp"))
    }

    @ParameterizedTest(name = "HTTP {0} gir VisittkortKildeHttpException")
    @ValueSource(ints = [400, 401, 403, 404, 429])
    fun `HTTP tilgangsfeil beholder status`(status: Int) {
        val feil = assertThrows<VisittkortKildeHttpException> {
            client(status to "sensitive-source-message").hentEtikettgrunnlag(TEST_FNR, setOf("kvp"))
        }
        assertThat(feil.statusCode.value()).isEqualTo(status)
        assertThat(feil.toString()).doesNotContain("sensitive-source-message")
    }

    @ParameterizedTest(name = "HTTP {0} gir utilgjengelig kilde")
    @ValueSource(ints = [500, 502, 503])
    fun `HTTP 5xx gir utilgjengelig kilde`(status: Int) {
        assertThrows<VisittkortKildeUtilgjengeligException> {
            client(status to "source unavailable").hentEtikettgrunnlag(TEST_FNR, setOf("kvp"))
        }
    }

    @Test
    fun `IOException videresendes som transportfeil`() {
        val transportfeil = IOException("connection reset")
        val ioClient = VeilarboppfolgingClientImpl(
            "http://oppfolging.test/veilarboppfolging", { "test-token" },
            OkHttpClient.Builder().addInterceptor { throw transportfeil }.build()
        )
        assertThat(assertThrows<IOException> { ioClient.hentEtikettgrunnlag(TEST_FNR, setOf("kvp")) })
            .isSameAs(transportfeil)
    }

    @ParameterizedTest(name = "hentKvpStatus med erKontorsperret={0}")
    @ValueSource(booleans = [true, false])
    fun `hentKvpStatus delegerer til samme GraphQL kall med bare kvp`(underKvp: Boolean) {
        requests.clear()
        val client = client(200 to """{"data":{"brukerStatus":{"erKontorsperret":$underKvp}}}""")

        assertThat(client.hentKvpStatus(TEST_FNR)).isEqualTo(underKvp)

        assertThat(requests).hasSize(1)
        assertThat(requests.single().url.encodedPath).isEqualTo("/veilarboppfolging/api/graphql")
        assertThat(variabler()).isEqualTo(forventetVariabler("hentBrukerStatus", "hentKvp"))
    }

    @Test
    fun `hentKvpStatus avviser null i erKontorsperret`() {
        assertThrows<VisittkortKildeKontraktException> {
            client(200 to """{"data":{"brukerStatus":{"erKontorsperret":null}}}""").hentKvpStatus(TEST_FNR)
        }
    }

    @Test
    fun `samlet spoerring er gyldig mot kildeskjemaet og respekterer include-flaggene`() {
        val kilde: Map<String, Any?> = mapOf(
            "brukerStatus" to mapOf(
                "erKontorsperret" to true,
                "manuell" to mapOf("erManuell" to false),
                "krr" to mapOf("kanVarsles" to true, "reservertIKrr" to false, "registrertIKrr" to true),
                "sykmeldtStatus" to "SYKMELDT"
            ),
            "oppfolging" to mapOf("erUnderOppfolging" to true),
            "utmeldingskandidat" to mapOf("tag" to "FORLENGELSE_UTLOPT")
        )
        for (felter in listOf(
            VeilarboppfolgingEtiketterClientTestBase.ALLE_FELTER,
            setOf("kvp"),
            setOf("reservertKrr"),
            setOf("oppgittHindringer")
        )) {
            requests.clear()
            client(200 to """{"data":{"brukerStatus":{"erKontorsperret":true,"manuell":{"erManuell":false},
                "krr":{"kanVarsles":true,"reservertIKrr":false,"registrertIKrr":true},"sykmeldtStatus":null},
                "oppfolging":{"erUnderOppfolging":true},"utmeldingskandidat":{"tag":null}}}""")
                .hentEtikettgrunnlag(TEST_FNR, felter)
            val body = JsonUtils.getMapper().readTree(requests.single().bodyTekst())

            val resultat = GraphQL.newGraphQL(VeilarboppfolgingEtiketterClientTestBase.OPPFOLGING_SCHEMA).build().execute(
                ExecutionInput.newExecutionInput()
                    .query(body.path("query").stringValue())
                    .variables(variabler())
                    .root(kilde)
                    .build()
            )

            assertThat(resultat.errors).isEmpty()
            val data: Map<String, Any?> = resultat.getData<Map<String, Any?>>()
            assertThat(data.containsKey("oppfolging")).isEqualTo(variabler()["hentOppfolging"])
            assertThat(data.containsKey("utmeldingskandidat")).isEqualTo(variabler()["hentUtmeldingskandidat"])
            assertThat(data.containsKey("brukerStatus")).isEqualTo(variabler()["hentBrukerStatus"])
            val forventet: Map<String, Any?> = when (felter) {
                VeilarboppfolgingEtiketterClientTestBase.ALLE_FELTER -> kilde
                setOf("kvp") -> mapOf("brukerStatus" to mapOf("erKontorsperret" to true))
                setOf("reservertKrr") -> mapOf("brukerStatus" to mapOf("krr" to mapOf("reservertIKrr" to false)))
                setOf("oppgittHindringer") -> mapOf("oppfolging" to mapOf("erUnderOppfolging" to true))
                else -> error("Missing test expectation")
            }
            assertThat(data).isEqualTo(forventet)
        }
    }

}
