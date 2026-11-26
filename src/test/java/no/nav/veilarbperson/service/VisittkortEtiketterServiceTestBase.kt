package no.nav.veilarbperson.service

import io.getunleash.Unleash
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.veilarbperson.client.VisittkortKildeHttpClient
import no.nav.veilarbperson.client.nom.SkjermetClient
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.ArbeidssokerperiodeResponse
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.BrukerResponse
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.BrukerType
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.MetadataResponse
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.ProfilertTil
import no.nav.veilarbperson.client.pdl.PdlClient
import no.nav.veilarbperson.client.pdl.PdlVisittkortEtiketterData
import no.nav.veilarbperson.client.representasjon.RepresentasjonClient
import no.nav.veilarbperson.client.representasjon.VisittkortRepresentasjonData
import no.nav.veilarbperson.client.veilarbdialog.VisittkortVarselClient
import no.nav.veilarbperson.client.veilarboppfolging.VeilarboppfolgingClient
import no.nav.veilarbperson.client.veilarboppfolging.VeilarboppfolgingClientImpl
import no.nav.veilarbperson.client.veilarbvedtaksstotte.VisittkortVedtakClient
import no.nav.veilarbperson.client.veilarbvedtaksstotte.VisittkortVedtakClientImpl
import no.nav.veilarbperson.utils.VisittkortEtiketterMapperTest.Companion.oppfolging
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.ZonedDateTime
import java.util.UUID

abstract class VisittkortEtiketterServiceTestBase {
    protected val pdl = mock<PdlClient>()
    protected val representasjon = mock<RepresentasjonClient>()
    protected val skjerming = mock<SkjermetClient>()
    protected val oppfolgingClient = mock<VeilarboppfolgingClient>()
    protected val arbeidssoker = mock<OppslagArbeidssoekerregisteretService>()
    protected val vedtak = mock<VisittkortVedtakClient>()
    protected val varsel = mock<VisittkortVarselClient>()
    protected val registry = SimpleMeterRegistry()
    protected val unleash = mock<Unleash>()
    protected val service = VisittkortEtiketterService(
        pdl, representasjon, skjerming, oppfolgingClient, arbeidssoker, vedtak, varsel, registry, unleash)
    protected val aktivArbeidssokerperiode = ArbeidssokerperiodeResponse(
        UUID.fromString("00000000-0000-0000-0000-000000000001"),
        MetadataResponse(
            ZonedDateTime.parse("2026-01-01T00:00:00Z"),
            BrukerResponse(BrukerType.SLUTTBRUKER, null), "kilde", "aarsak"
        ),
        null
    )

    @BeforeEach
    fun gyldigeKildesvar() {
        whenever(pdl.hentVisittkortEtiketter(any(), any())).thenReturn(PdlVisittkortEtiketterData())
        whenever(representasjon.hentOppfolgingsrepresentasjon(any())).thenReturn(VisittkortRepresentasjonData(false, false))
        whenever(oppfolgingClient.hentEtikettgrunnlag(any(), any())).thenReturn(oppfolging().copy(kvp = false))
        whenever(arbeidssoker.hentSisteArbeidssoekerPeriode(any())).thenReturn(null)
        whenever(unleash.isEnabled("veilarbperson.visittkort_etiketter_test_feil")).thenReturn(false)
    }

    protected fun registrertMedProfilering(profilering: ProfilertTil) {
        whenever(arbeidssoker.hentSisteArbeidssoekerPeriode(any())).thenReturn(aktivArbeidssokerperiode)
        whenever(arbeidssoker.hentVisittkortProfilering(any(), any())).thenReturn(profilering)
    }

    protected fun oppfolgingFraHttp(status: Int, body: String, vedKall: () -> Unit = {}): VeilarboppfolgingClient =
        VeilarboppfolgingClientImpl("http://oppfolging.test/veilarboppfolging", { "test-token" },
            kildeHttp(status, body, vedKall))

    protected fun vedtakFraHttp(status: Int, body: String, vedKall: () -> Unit = {}): VisittkortVedtakClient =
        VisittkortVedtakClientImpl("http://vedtak.test/veilarbvedtaksstotte", { "test-token" },
            VisittkortKildeHttpClient(kildeHttp(status, body, vedKall)))

    protected fun kildeHttp(status: Int, body: String, vedKall: () -> Unit): OkHttpClient =
        OkHttpClient.Builder().addInterceptor { chain ->
            vedKall()
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                .code(status).message("Test response").body(body.toResponseBody()).build()
        }.build()

    companion object {
        val FULL_OPPFOLGING = """
            {"data":{
              "brukerStatus":{
                "erKontorsperret":true,
                "manuell":{"erManuell":true},
                "krr":{"kanVarsles":false,"reservertIKrr":false,"registrertIKrr":true},
                "sykmeldtStatus":"SYKMELDT_MED_ARBEIDSGIVER"
              },
              "oppfolging":{"erUnderOppfolging":true},
              "utmeldingskandidat":{"tag":"FORLENGELSE_UTLOPT"}
            }}
        """.trimIndent()
    }
}
