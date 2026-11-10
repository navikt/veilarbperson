package no.nav.veilarbperson.client

import graphql.ExecutionInput
import graphql.GraphQL
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import no.nav.common.json.JsonUtils
import no.nav.veilarbperson.client.veilarboppfolging.VeilarboppfolgingClientImpl
import no.nav.veilarbperson.domain.visittkort.OppfolgingEtikettgrunnlag
import no.nav.veilarbperson.domain.visittkort.UtmeldingskandidatEtikett
import no.nav.veilarbperson.utils.TestData.TEST_FNR
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tools.jackson.core.type.TypeReference
import java.io.IOException

class VeilarboppfolgingEtiketterClientTest {
    private val requests = mutableListOf<Request>()

    private fun client(vararg svar: Pair<Int, String>): VeilarboppfolgingClientImpl {
        val iterator = svar.iterator()
        val okhttp = OkHttpClient.Builder().addInterceptor { chain ->
            requests.add(chain.request())
            val (status, body) = iterator.next()
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                .code(status).message("Test response")
                .body(body.toResponseBody("application/json".toMediaType())).build()
        }.build()
        return VeilarboppfolgingClientImpl("http://oppfolging.test/veilarboppfolging", { "test-token" }, okhttp)
    }

    private fun hent(felter: Set<String>, data: String) =
        client(200 to """{"data":$data}""").hentEtikettgrunnlag(TEST_FNR, felter)

    private fun variabler(request: Request = requests.last()): Map<String, Any> =
        JsonUtils.getMapper().convertValue(
            JsonUtils.getMapper().readTree(request.bodyTekst()).path("variables"),
            object : TypeReference<Map<String, Any>>() {}
        )

    private fun forventetVariabler(vararg paa: String): Map<String, Any> {
        val alle = listOf(
            "hentBrukerStatus", "hentKvp", "hentManuell", "hentKrr", "hentKanVarsles",
            "hentReservertKrr", "hentRegistrertKrr", "hentSykmeldt", "hentOppfolging", "hentUtmeldingskandidat"
        )
        return mapOf<String, Any>("fnr" to TEST_FNR.get()) + alle.associateWith { it in paa }
    }

    @Test
    fun `alle felter gir ett GraphQL kall og full mapping`() {
        val client = client(200 to """
            {"data":{
              "brukerStatus":{"erKontorsperret":true,"manuell":{"erManuell":false},
                "krr":{"kanVarsles":false,"reservertIKrr":true,"registrertIKrr":false},
                "sykmeldtStatus":"FREMTIDIG_VERDI"},
              "oppfolging":{"erUnderOppfolging":true},
              "utmeldingskandidat":{"tag":"FORLENGELSE_UTLOPT"}}}
        """.trimIndent())

        val resultat = client.hentEtikettgrunnlag(TEST_FNR, ALLE_FELTER)

        assertThat(resultat).isEqualTo(
            OppfolgingEtikettgrunnlag(
                underOppfolging = true,
                manuell = false,
                reservasjonKrr = true,
                registrertKrr = false,
                kanVarsles = false,
                sykmeldt = true,
                utmeldingskandidat = UtmeldingskandidatEtikett.FORLENGELSE_UTLOPT,
                kvp = true
            )
        )
        val request = requests.single()
        assertThat(request.method).isEqualTo("POST")
        assertThat(request.url.encodedPath).isEqualTo("/veilarboppfolging/api/graphql")
        assertThat(request.url.query).isNull()
        assertThat(request.header("Authorization")).isEqualTo("Bearer test-token")
        assertThat(request.header("behandlingsnummer")).isNull()
        assertThat(variabler()).isEqualTo(
            forventetVariabler(
                "hentBrukerStatus", "hentKvp", "hentManuell", "hentKrr", "hentKanVarsles",
                "hentReservertKrr", "hentRegistrertKrr", "hentSykmeldt", "hentOppfolging", "hentUtmeldingskandidat"
            )
        )
        val query = JsonUtils.getMapper().readTree(request.bodyTekst()).path("query").stringValue()
        assertThat(query).contains("@include(if: \$hentKvp)", "erKontorsperret", "sykmeldtStatus", "tag")
            .doesNotContain(TEST_FNR.get())
    }

    @Test
    fun `minimale utvalg setter bare noedvendige flagg og lar andre verdier vaere null`() {
        data class Tilfelle(
            val felt: String,
            val flagg: List<String>,
            val data: String,
            val forventet: OppfolgingEtikettgrunnlag
        )
        val tilfeller = listOf(
            Tilfelle(
                "kvp", listOf("hentBrukerStatus", "hentKvp"),
                """{"brukerStatus":{"erKontorsperret":false}}""",
                OppfolgingEtikettgrunnlag(kvp = false)
            ),
            Tilfelle(
                "manuellOppfolging", listOf("hentBrukerStatus", "hentManuell"),
                """{"brukerStatus":{"manuell":{"erManuell":true}}}""",
                OppfolgingEtikettgrunnlag(manuell = true)
            ),
            Tilfelle(
                "reservertKrr", listOf("hentBrukerStatus", "hentKrr", "hentReservertKrr"),
                """{"brukerStatus":{"krr":{"reservertIKrr":false}}}""",
                OppfolgingEtikettgrunnlag(reservasjonKrr = false)
            ),
            Tilfelle(
                "ikkeRegistrertKrr", listOf("hentBrukerStatus", "hentKrr", "hentRegistrertKrr"),
                """{"brukerStatus":{"krr":{"registrertIKrr":false}}}""",
                OppfolgingEtikettgrunnlag(registrertKrr = false)
            ),
            Tilfelle(
                "utdatertIKrr",
                listOf(
                    "hentBrukerStatus", "hentManuell", "hentKrr", "hentKanVarsles",
                    "hentReservertKrr", "hentRegistrertKrr"
                ),
                """{"brukerStatus":{"manuell":{"erManuell":false},
                    "krr":{"kanVarsles":true,"reservertIKrr":false,"registrertIKrr":true}}}""",
                OppfolgingEtikettgrunnlag(
                    manuell = false, reservasjonKrr = false, registrertKrr = true, kanVarsles = true
                )
            ),
            Tilfelle(
                "sykmeldt", listOf("hentBrukerStatus", "hentSykmeldt"),
                """{"brukerStatus":{"sykmeldtStatus":"SYKMELDT"}}""",
                OppfolgingEtikettgrunnlag(sykmeldt = true)
            ),
            Tilfelle(
                "utmeldingskandidat", listOf("hentUtmeldingskandidat"),
                """{"utmeldingskandidat":{"tag":"AVSLUTTET_AAP_SAK"}}""",
                OppfolgingEtikettgrunnlag(utmeldingskandidat = UtmeldingskandidatEtikett.AVSLUTTET_AAP_SAK)
            ),
            Tilfelle(
                "ikkeUnderOppfolging", listOf("hentOppfolging"),
                """{"oppfolging":{"erUnderOppfolging":false}}""",
                OppfolgingEtikettgrunnlag(underOppfolging = false)
            ),
            Tilfelle(
                "trengerOppfolgingsvedtak", listOf("hentOppfolging"),
                """{"oppfolging":{"erUnderOppfolging":true}}""",
                OppfolgingEtikettgrunnlag(underOppfolging = true)
            ),
            Tilfelle(
                "antattGodeMuligheter", listOf("hentOppfolging"),
                """{"oppfolging":{"erUnderOppfolging":true}}""",
                OppfolgingEtikettgrunnlag(underOppfolging = true)
            ),
            Tilfelle(
                "antattBehovForVeiledning", listOf("hentOppfolging"),
                """{"oppfolging":{"erUnderOppfolging":false}}""",
                OppfolgingEtikettgrunnlag(underOppfolging = false)
            ),
            Tilfelle(
                "oppgittHindringer", listOf("hentOppfolging"),
                """{"oppfolging":{"erUnderOppfolging":null}}""",
                OppfolgingEtikettgrunnlag(underOppfolging = null)
            )
        )

        for (tilfelle in tilfeller) {
            requests.clear()
            assertThat(hent(setOf(tilfelle.felt), tilfelle.data)).`as`(tilfelle.felt).isEqualTo(tilfelle.forventet)
            assertThat(requests).`as`(tilfelle.felt).hasSize(1)
            assertThat(variabler()).`as`(tilfelle.felt).isEqualTo(forventetVariabler(*tilfelle.flagg.toTypedArray()))
        }
    }

    @Test
    fun `uetterspurte felter parses ikke og kreves ikke`() {
        val resultat = hent(
            setOf("kvp"),
            """{"brukerStatus":{"erKontorsperret":true,"manuell":null,"krr":"feil","sykmeldtStatus":7},
                "oppfolging":"feil","utmeldingskandidat":{"tag":"UKJENT_UTMELDINGSKANDIDAT"}}"""
        )

        assertThat(resultat).isEqualTo(OppfolgingEtikettgrunnlag(kvp = true))
    }

    @Test
    fun `flere felter med overlappende avhengigheter gir ett kall og delt manuell-flagg`() {
        val resultat = hent(
            setOf("manuellOppfolging", "utdatertIKrr", "kvp"),
            """{"brukerStatus":{"erKontorsperret":false,"manuell":{"erManuell":true},
                "krr":{"kanVarsles":false,"reservertIKrr":false,"registrertIKrr":true}}}"""
        )

        assertThat(resultat).isEqualTo(
            OppfolgingEtikettgrunnlag(
                manuell = true, reservasjonKrr = false, registrertKrr = true, kanVarsles = false, kvp = false
            )
        )
        assertThat(requests).hasSize(1)
        assertThat(variabler()).isEqualTo(
            forventetVariabler(
                "hentBrukerStatus", "hentKvp", "hentManuell", "hentKrr", "hentKanVarsles",
                "hentReservertKrr", "hentRegistrertKrr"
            )
        )
    }

    @Test
    fun `sant falsk og null er gyldige verdier der kontrakten tillater det`() {
        for (verdi in listOf(true, false)) {
            assertThat(hent(setOf("kvp"), """{"brukerStatus":{"erKontorsperret":$verdi}}""").kvp).isEqualTo(verdi)
            assertThat(hent(setOf("ikkeUnderOppfolging"), """{"oppfolging":{"erUnderOppfolging":$verdi}}""")
                .underOppfolging).isEqualTo(verdi)
        }
        assertThat(hent(setOf("sykmeldt"), """{"brukerStatus":{"sykmeldtStatus":null}}""").sykmeldt).isFalse()
        assertThat(hent(setOf("sykmeldt"), """{"brukerStatus":{"sykmeldtStatus":"NY_STATUS"}}""").sykmeldt).isTrue()
        assertThat(hent(setOf("utmeldingskandidat"), """{"utmeldingskandidat":{"tag":null}}""").utmeldingskandidat)
            .isNull()
    }

    @Test
    fun `profilfelter krever objekt og felt men ikke true`() {
        for (felt in listOf("antattGodeMuligheter", "antattBehovForVeiledning", "oppgittHindringer")) {
            assertThat(hent(setOf(felt), """{"oppfolging":{"erUnderOppfolging":false}}""").underOppfolging).isFalse()
            assertThat(hent(setOf(felt), """{"oppfolging":{"erUnderOppfolging":null}}""").underOppfolging).isNull()
            for (data in listOf(
                "{}", """{"oppfolging":null}""", """{"oppfolging":{}}""", """{"oppfolging":[]}""",
                """{"oppfolging":{"erUnderOppfolging":"true"}}"""
            )) {
                assertThrows<VisittkortKildeKontraktException>(felt + data) { hent(setOf(felt), data) }
            }
        }
    }

    @Test
    fun `profilfelt sammen med etikett som krever oppfolgingsstatus godtar ikke null`() {
        val felter = setOf("oppgittHindringer", "ikkeUnderOppfolging")

        assertThat(hent(felter, """{"oppfolging":{"erUnderOppfolging":false}}""").underOppfolging).isFalse()
        assertThrows<VisittkortKildeKontraktException> {
            hent(felter, """{"oppfolging":{"erUnderOppfolging":null}}""")
        }
        assertThat(requests).hasSize(2)
        assertThat(requests.map { variabler(it)["hentOppfolging"] }).containsOnly(true)
    }

    @Test
    fun `alle ni gyldige utmeldingskandidat tagger mappes`() {
        val gyldige = UtmeldingskandidatEtikett.entries.filterNot { it.erUkjent }
        assertThat(gyldige).hasSize(9)
        assertThat(gyldige).contains(
            UtmeldingskandidatEtikett.FOLKEREGISTERSTATUS_UKJENT,
            UtmeldingskandidatEtikett.FOLKEREGISTERSTATUS_INGEN_STATUS
        )

        for (etikett in gyldige) {
            val resultat = hent(setOf("utmeldingskandidat"), """{"utmeldingskandidat":{"tag":"${etikett.name}"}}""")
            assertThat(resultat.utmeldingskandidat).isEqualTo(etikett)
        }
    }

    @Test
    fun `utilgjengelighetsmarkoer og ukjente tagger avvises`() {
        for (tag in listOf(
            "\"UKJENT_UTMELDINGSKANDIDAT\"", "\"NY_TAG\"", "\"forlengelse_utlopt\"", "\"\"", "1", "true", "[]", "{}"
        )) {
            assertThrows<VisittkortKildeKontraktException>(tag) {
                hent(setOf("utmeldingskandidat"), """{"utmeldingskandidat":{"tag":$tag}}""")
            }
        }
    }

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

    @Test
    fun `HTTP feil beholder status og 5xx og IOException er utilgjengelig`() {
        for (status in listOf(400, 401, 403, 404, 429)) {
            val feil = assertThrows<VisittkortKildeHttpException> {
                client(status to "sensitive-source-message").hentEtikettgrunnlag(TEST_FNR, setOf("kvp"))
            }
            assertThat(feil.statusCode.value()).isEqualTo(status)
            assertThat(feil.toString()).doesNotContain("sensitive-source-message")
        }
        for (status in listOf(500, 502, 503)) {
            assertThrows<VisittkortKildeUtilgjengeligException> {
                client(status to "source unavailable").hentEtikettgrunnlag(TEST_FNR, setOf("kvp"))
            }
        }

        val transportfeil = IOException("connection reset")
        val ioClient = VeilarboppfolgingClientImpl(
            "http://oppfolging.test/veilarboppfolging", { "test-token" },
            OkHttpClient.Builder().addInterceptor { throw transportfeil }.build()
        )
        assertThat(assertThrows<IOException> { ioClient.hentEtikettgrunnlag(TEST_FNR, setOf("kvp")) })
            .isSameAs(transportfeil)
    }

    @Test
    fun `hentKvpStatus delegerer til samme GraphQL kall med bare kvp`() {
        for (underKvp in listOf(true, false)) {
            requests.clear()
            val client = client(200 to """{"data":{"brukerStatus":{"erKontorsperret":$underKvp}}}""")

            assertThat(client.hentKvpStatus(TEST_FNR)).isEqualTo(underKvp)

            assertThat(requests).hasSize(1)
            assertThat(requests.single().url.encodedPath).isEqualTo("/veilarboppfolging/api/graphql")
            assertThat(variabler()).isEqualTo(forventetVariabler("hentBrukerStatus", "hentKvp"))
        }
        assertThrows<VisittkortKildeKontraktException> {
            client(200 to """{"data":{"brukerStatus":{"erKontorsperret":null}}}""").hentKvpStatus(TEST_FNR)
        }
    }

    @Test
    fun `samlet spoerring er gyldig mot kildeskjemaet og respekterer include-flaggene`() {
        val kilde = mapOf(
            "brukerStatus" to mapOf(
                "erKontorsperret" to true,
                "manuell" to mapOf("erManuell" to false),
                "krr" to mapOf("kanVarsles" to true, "reservertIKrr" to false, "registrertIKrr" to true),
                "sykmeldtStatus" to "SYKMELDT"
            ),
            "oppfolging" to mapOf("erUnderOppfolging" to true),
            "utmeldingskandidat" to mapOf("tag" to "FORLENGELSE_UTLOPT")
        )
        for (felter in listOf(ALLE_FELTER, setOf("kvp"), setOf("reservertKrr"), setOf("oppgittHindringer"))) {
            requests.clear()
            client(200 to """{"data":{"brukerStatus":{"erKontorsperret":true,"manuell":{"erManuell":false},
                "krr":{"kanVarsles":true,"reservertIKrr":false,"registrertIKrr":true},"sykmeldtStatus":null},
                "oppfolging":{"erUnderOppfolging":true},"utmeldingskandidat":{"tag":null}}}""")
                .hentEtikettgrunnlag(TEST_FNR, felter)
            val body = JsonUtils.getMapper().readTree(requests.single().bodyTekst())

            val resultat = GraphQL.newGraphQL(OPPFOLGING_SCHEMA).build().execute(
                ExecutionInput.newExecutionInput()
                    .query(body.path("query").stringValue())
                    .variables(variabler())
                    .root(kilde)
                    .build()
            )

            assertThat(resultat.errors).isEmpty()
            val data = resultat.getData<Map<String, Any>>()
            assertThat(data.containsKey("oppfolging")).isEqualTo(variabler()["hentOppfolging"])
            assertThat(data.containsKey("utmeldingskandidat")).isEqualTo(variabler()["hentUtmeldingskandidat"])
            assertThat(data.containsKey("brukerStatus")).isEqualTo(variabler()["hentBrukerStatus"])
            val forventet = when (felter) {
                ALLE_FELTER -> kilde
                setOf("kvp") -> mapOf("brukerStatus" to mapOf("erKontorsperret" to true))
                setOf("reservertKrr") -> mapOf("brukerStatus" to mapOf("krr" to mapOf("reservertIKrr" to false)))
                setOf("oppgittHindringer") -> mapOf("oppfolging" to mapOf("erUnderOppfolging" to true))
                else -> error("Missing test expectation")
            }
            assertThat(data).isEqualTo(forventet)
        }
    }

    private fun Request.bodyTekst(): String {
        val buffer = Buffer()
        requireNotNull(body).writeTo(buffer)
        return buffer.readUtf8()
    }

    companion object {
        private val ALLE_FELTER = setOf(
            "kvp", "manuellOppfolging", "reservertKrr", "ikkeRegistrertKrr", "utdatertIKrr", "sykmeldt",
            "utmeldingskandidat", "ikkeUnderOppfolging", "trengerOppfolgingsvedtak",
            "antattGodeMuligheter", "antattBehovForVeiledning", "oppgittHindringer"
        )
        val OPPFOLGING_SCHEMA = SchemaGenerator().makeExecutableSchema(
            SchemaParser().parse("""
                type Query {
                    brukerStatus(fnr: String!): BrukerStatus
                    oppfolging(fnr: String!): Oppfolging
                    utmeldingskandidat(fnr: String!): Utmeldingskandidat
                }
                type BrukerStatus {
                    erKontorsperret: Boolean
                    manuell: Manuell
                    krr: Krr
                    sykmeldtStatus: String
                }
                type Manuell { erManuell: Boolean }
                type Krr { kanVarsles: Boolean reservertIKrr: Boolean registrertIKrr: Boolean }
                type Oppfolging { erUnderOppfolging: Boolean }
                type Utmeldingskandidat { tag: String }
            """.trimIndent()),
            RuntimeWiring.newRuntimeWiring().build()
        )
    }
}
