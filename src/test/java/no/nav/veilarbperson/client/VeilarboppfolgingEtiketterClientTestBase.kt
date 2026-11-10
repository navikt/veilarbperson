package no.nav.veilarbperson.client

import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import no.nav.common.json.JsonUtils
import no.nav.veilarbperson.client.veilarboppfolging.VeilarboppfolgingClientImpl
import no.nav.veilarbperson.utils.TestData.TEST_FNR
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import tools.jackson.core.type.TypeReference

abstract class VeilarboppfolgingEtiketterClientTestBase {
    protected val requests = mutableListOf<Request>()

    protected fun client(vararg svar: Pair<Int, String>): VeilarboppfolgingClientImpl {
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

    protected fun hent(felter: Set<String>, data: String) =
        client(200 to """{"data":$data}""").hentEtikettgrunnlag(TEST_FNR, felter)

    protected fun variabler(request: Request = requests.last()): Map<String, Any> =
        JsonUtils.getMapper().convertValue(
            JsonUtils.getMapper().readTree(request.bodyTekst()).path("variables"),
            object : TypeReference<Map<String, Any>>() {}
        )

    protected fun forventetVariabler(vararg paa: String): Map<String, Any> {
        val alle = listOf(
            "hentBrukerStatus", "hentKvp", "hentManuell", "hentKrr", "hentKanVarsles",
            "hentReservertKrr", "hentRegistrertKrr", "hentSykmeldt", "hentOppfolging", "hentUtmeldingskandidat"
        )
        return mapOf<String, Any>("fnr" to TEST_FNR.get()) + alle.associateWith { it in paa }
    }

    protected fun Request.bodyTekst(): String {
        val buffer = Buffer()
        requireNotNull(body).writeTo(buffer)
        return buffer.readUtf8()
    }

    companion object {
        val ALLE_FELTER = setOf(
            "kvp", "manuellOppfolging", "reservertKrr", "ikkeRegistrertKrr", "utdatertIKrr", "sykmeldt",
            "utmeldingskandidat", "ikkeUnderOppfolging", "trengerOppfolgingsvedtak",
            "antattGodeMuligheter", "antattBehovForVeiledning", "oppgittHindringer"
        )
        val OPPFOLGING_SCHEMA = SchemaGenerator().makeExecutableSchema(
            SchemaParser().parse(
                """
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
                """.trimIndent()
            ),
            RuntimeWiring.newRuntimeWiring().build()
        )
    }
}
