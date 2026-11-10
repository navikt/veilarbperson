package no.nav.veilarbperson.client

import no.nav.veilarbperson.client.nom.SkjermetClientImpl
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.OppslagArbeidssoekerregisteretClientImpl
import no.nav.veilarbperson.client.pdl.PdlClientImpl
import no.nav.veilarbperson.client.representasjon.RepresentasjonClientImpl
import no.nav.veilarbperson.client.veilarboppfolging.VeilarboppfolgingClientImpl
import no.nav.veilarbperson.service.AuthService
import no.nav.veilarbperson.service.OppslagArbeidssoekerregisteretService
import no.nav.veilarbperson.utils.TestData.TEST_FNR
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.mockito.kotlin.mock

abstract class VisittkortKildeClientTestBase {
    protected val requests = mutableListOf<Request>()

    protected fun http(vararg svar: Pair<Int, String>) = VisittkortKildeHttpClient(okhttp(*svar))

    protected fun okhttp(vararg svar: Pair<Int, String>): OkHttpClient {
        val iterator = svar.iterator()
        return OkHttpClient.Builder().addInterceptor { chain ->
            requests.add(chain.request())
            val (status, body) = iterator.next()
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                .code(status).message("Test response")
                .body(body.toResponseBody("application/json".toMediaType())).build()
        }.build()
    }

    protected fun pdl(vararg svar: Pair<Int, String>) =
        PdlClientImpl("http://pdl.test", mock<AuthService>(), { "test-token" }, { "system-token" }, okhttp(*svar))

    protected fun representasjon(vararg svar: Pair<Int, String>) =
        RepresentasjonClientImpl("http://repr.test", { "test-token" }, okhttp(*svar))

    protected fun skjerming(vararg svar: Pair<Int, String>) =
        SkjermetClientImpl("http://nom.test", { "test-token" }, okhttp(*svar))

    protected fun arbeidssoker(vararg svar: Pair<Int, String>) =
        OppslagArbeidssoekerregisteretService(
            OppslagArbeidssoekerregisteretClientImpl("http://paw.test", { "test-token" }, "veilarbperson", okhttp(*svar))
        )

    protected fun kvp(vararg svar: Pair<Int, String>) =
        VeilarboppfolgingClientImpl("http://oppfolging.test/veilarboppfolging", { "test-token" }, okhttp(*svar))

    protected fun Request.bodyTekst(): String {
        val buffer = Buffer()
        requireNotNull(body).writeTo(buffer)
        return buffer.readUtf8()
    }

    companion object {
        val OPPFOLGING_SCHEMA = VeilarboppfolgingEtiketterClientTestBase.OPPFOLGING_SCHEMA

        const val PERIODE_ID = "00000000-0000-0000-0000-000000000001"
        const val OPPLYSNINGER = "00000000-0000-0000-0000-000000000002"
        const val METADATA = """{"tidspunkt":"2026-01-01T00:00:00Z",
            "utfoertAv":{"type":"SYSTEM","id":null},"kilde":"test","aarsak":"test"}"""
        val PERIODE = """
            [{"periodeId":"$PERIODE_ID","startet":$METADATA,"avsluttet":null}]
        """.trimIndent()
    }
}
