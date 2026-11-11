package no.nav.veilarbperson.service

import no.nav.veilarbperson.client.VisittkortKildeIkkeKonfigurertException
import no.nav.veilarbperson.client.VisittkortKildeHttpException
import no.nav.veilarbperson.client.VisittkortKildeKontraktException
import no.nav.veilarbperson.client.VisittkortKildeUtilgjengeligException
import no.nav.veilarbperson.client.nom.SkjermetClient
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.ArbeidssokerVisittkortData
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.ProfilertTil
import no.nav.veilarbperson.client.pdl.PdlClient
import no.nav.veilarbperson.client.representasjon.RepresentasjonClient
import no.nav.veilarbperson.client.veilarboppfolging.VeilarboppfolgingClient
import no.nav.veilarbperson.domain.visittkort.*
import no.nav.veilarbperson.utils.TestData.TEST_FNR
import no.nav.veilarbperson.utils.VisittkortEtiketterMapperTest.Companion.ALLE_FELTER
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.io.IOException
import java.util.function.Supplier

class VisittkortEtiketterServiceVedtakProfileringOgFeilTest : VisittkortEtiketterServiceTestBase() {

    @Test
    fun `ukonfigurert kilde gir eksplisitt feil og ikke ukjent eller fravaer`() {
        val utenNyeKilder = VisittkortEtiketterService(
            pdl, representasjon, skjerming, oppfolgingClient, arbeidssoker, null, null, registry
        )

        assertThat(utenNyeKilder.hent(TEST_FNR, null, setOf("manuellOppfolging")).manuellOppfolging).isNull()
        assertThrows<VisittkortKildeIkkeKonfigurertException> {
            utenNyeKilder.hent(TEST_FNR, null, setOf("varsel"))
        }
        assertThrows<VisittkortKildeIkkeKonfigurertException> {
            utenNyeKilder.hent(TEST_FNR, null, setOf("trengerOppfolgingsvedtak"))
        }
        verifyNoInteractions(pdl, representasjon, skjerming, arbeidssoker)
        assertThat(utenNyeKilder.hent(TEST_FNR, null, setOf("skjerming")).skjerming).isNull()
        assertThat(registry.get("visittkort_etiketter_varighet").timer().count()).isEqualTo(4)
    }

    @Test
    fun `manglende nye kildebeaner hindrer ikke oppstart av eksisterende API`() {
        ApplicationContextRunner()
            .withUserConfiguration(VisittkortEtiketterService::class.java)
            .withBean(PdlClient::class.java, Supplier { pdl })
            .withBean(RepresentasjonClient::class.java, Supplier { representasjon })
            .withBean(SkjermetClient::class.java, Supplier { skjerming })
            .withBean(OppslagArbeidssoekerregisteretService::class.java, Supplier { arbeidssoker })
            .withBean(VeilarboppfolgingClient::class.java, Supplier { oppfolgingClient })
            .withBean(io.micrometer.core.instrument.MeterRegistry::class.java, Supplier { registry })
            .run { context ->
                assertThat(context).hasNotFailed().hasSingleBean(VisittkortEtiketterService::class.java)
                val serviceFraContext = context.getBean(VisittkortEtiketterService::class.java)
                assertThrows<VisittkortKildeIkkeKonfigurertException> {
                    serviceFraContext.hent(TEST_FNR, null, setOf("trengerOppfolgingsvedtak"))
                }
                assertThat(serviceFraContext.hent(TEST_FNR, null, setOf("manuellOppfolging")).manuellOppfolging).isNull()
                assertThat(serviceFraContext.hent(TEST_FNR, null, setOf("kvp")).kvp).isNull()
            }
    }

    @Test
    fun `KVP alene trenger ikke andre oppfolgingsfelter eller behandlingsnummer`() {
        whenever(oppfolgingClient.hentEtikettgrunnlag(TEST_FNR, setOf("kvp"))).thenReturn(
            OppfolgingEtikettgrunnlag(kvp = true), OppfolgingEtikettgrunnlag(kvp = false)
        )
        val kunKvp = VisittkortEtiketterService(
            pdl, representasjon, skjerming, oppfolgingClient, arbeidssoker, null, null, registry
        )

        assertThat(kunKvp.hent(TEST_FNR, null, setOf("kvp")).kvp).isEqualTo(KvpEtikett.KVP)
        assertThat(kunKvp.hent(TEST_FNR, null, setOf("kvp")).kvp).isNull()
        verify(oppfolgingClient, times(2)).hentEtikettgrunnlag(TEST_FNR, setOf("kvp"))
        verifyNoMoreInteractions(oppfolgingClient)
        verifyNoInteractions(pdl, representasjon, skjerming, arbeidssoker, vedtak, varsel)
    }

    @Test
    fun `KVP timeout gir ukjent men tilgangsfeil og ugyldige svar stopper oppslaget`() {
        doAnswer { throw IOException("source unavailable") }.whenever(oppfolgingClient).hentEtikettgrunnlag(any(), any())
        assertThat(service.hent(TEST_FNR, null, setOf("kvp")).kvp)
            .isEqualTo(KvpEtikett.UKJENT_OPPFOLGING)

        for (feil in listOf(
            ResponseStatusException(HttpStatus.FORBIDDEN),
            VisittkortKildeKontraktException()
        )) {
            doThrow(feil).whenever(oppfolgingClient).hentEtikettgrunnlag(any(), any())
            assertThat(assertThrows<RuntimeException> {
                service.hent(TEST_FNR, null, setOf("kvp"))
            }).isSameAs(feil)
        }
    }

    @Test
    fun `ekte vedtaksklient avklarer vedtaksbehov og profilering fra samme oppslag`() {
        whenever(arbeidssoker.hentVisittkortData(any(), any())).thenReturn(
            ArbeidssokerVisittkortData(true, ProfilertTil.ANTATT_GODE_MULIGHETER)
        )
        for ((body, harVedtak) in listOf(
            "" to false,
            "null" to false,
            """{"innsatsgruppe":"GODE_MULIGHETER"}""" to true
        )) {
            var antallKall = 0
            val klient = vedtakFraHttp(200, body) { antallKall++ }
            val medEkteVedtaksKlient = VisittkortEtiketterService(
                pdl, representasjon, skjerming, oppfolgingClient, arbeidssoker, klient, null, registry
            )

            val resultat = medEkteVedtaksKlient.hent(TEST_FNR, null,
                setOf("trengerOppfolgingsvedtak", "antattGodeMuligheter"))

            assertThat(resultat.trengerOppfolgingsvedtak).isEqualTo(
                TrengerOppfolgingsvedtakEtikett.TRENGER_OPPFOLGINGSVEDTAK.takeUnless { harVedtak }
            )
            assertThat(resultat.antattGodeMuligheter).isEqualTo(
                AntattGodeMuligheterEtikett.ANTATT_GODE_MULIGHETER.takeUnless { harVedtak }
            )
            assertThat(antallKall).isEqualTo(1)
        }
    }

    @Test
    fun `vedtak HTTP 500 blir ukjent behov og profilering aldri manglende vedtak`() {
        whenever(arbeidssoker.hentVisittkortData(any(), any())).thenReturn(
            ArbeidssokerVisittkortData(true, ProfilertTil.ANTATT_GODE_MULIGHETER)
        )
        val medFeilendeVedtak = VisittkortEtiketterService(
            pdl, representasjon, skjerming, oppfolgingClient, arbeidssoker,
            vedtakFraHttp(500, "source unavailable"), null, registry
        )

        val resultat = medFeilendeVedtak.hent(TEST_FNR, null,
            setOf("trengerOppfolgingsvedtak", "antattGodeMuligheter"))

        assertThat(resultat.trengerOppfolgingsvedtak)
            .isEqualTo(TrengerOppfolgingsvedtakEtikett.UKJENT_OPPFOLGINGSVEDTAK)
        assertThat(resultat.antattGodeMuligheter).isEqualTo(AntattGodeMuligheterEtikett.UKJENT_PROFILERING)
    }

    @Test
    fun `alle 22 felter bruker ekte oppfolgingsklient med ett felles GraphQL kall`() {
        var antallKall = 0
        val medEkteOppfolging = VisittkortEtiketterService(
            pdl, representasjon, skjerming,
            oppfolgingFraHttp(200, VisittkortEtiketterServiceTestBase.FULL_OPPFOLGING) { antallKall++ },
            arbeidssoker, vedtak, varsel, registry
        )

        val resultat = medEkteOppfolging.hent(TEST_FNR, "B643", ALLE_FELTER)

        assertThat(resultat.kvp).isEqualTo(KvpEtikett.KVP)
        assertThat(resultat.manuellOppfolging).isEqualTo(ManuellOppfolgingEtikett.MANUELL_OPPFOLGING)
        assertThat(resultat.sykmeldt).isEqualTo(SykmeldtEtikett.SYKMELDT)
        assertThat(resultat.utmeldingskandidat).isEqualTo(UtmeldingskandidatEtikett.FORLENGELSE_UTLOPT)
        assertThat(resultat.trengerOppfolgingsvedtak)
            .isEqualTo(TrengerOppfolgingsvedtakEtikett.TRENGER_OPPFOLGINGSVEDTAK)
        assertThat(resultat.ukjenteFelter()).isEmpty()
        assertThat(antallKall).isEqualTo(1)
        assertThat(registry.get("visittkort_kilde_varighet").tag("kilde", "oppfolging").timer().count())
            .isEqualTo(1)
        verify(vedtak).harGjeldendeVedtak(TEST_FNR)
        verify(arbeidssoker).hentVisittkortData(TEST_FNR, true)
    }

    @Test
    fun `ekte oppfolgingsklient avkrefter vedtaksbehov uten konfigurert vedtaksklient`() {
        val medEkteOppfolging = VisittkortEtiketterService(
            pdl, representasjon, skjerming,
            oppfolgingFraHttp(200, """{"data":{"oppfolging":{"erUnderOppfolging":false}}}"""),
            arbeidssoker, null, null, registry
        )

        assertThat(medEkteOppfolging.hent(TEST_FNR, null, setOf("trengerOppfolgingsvedtak"))
            .trengerOppfolgingsvedtak).isNull()
        verifyNoInteractions(arbeidssoker, vedtak, varsel)
    }

    @Test
    fun `ekte oppfolgingsobjekt med ukjent oppfolgingsflagg kan gi profilering`() {
        whenever(arbeidssoker.hentVisittkortData(any(), any())).thenReturn(
            ArbeidssokerVisittkortData(true, ProfilertTil.ANTATT_GODE_MULIGHETER)
        )
        val medEkteOppfolging = VisittkortEtiketterService(
            pdl, representasjon, skjerming,
            oppfolgingFraHttp(200, """{"data":{"oppfolging":{"erUnderOppfolging":null}}}"""),
            arbeidssoker, vedtak, null, registry
        )

        assertThat(medEkteOppfolging.hent(TEST_FNR, null, setOf("antattGodeMuligheter")).antattGodeMuligheter)
            .isEqualTo(AntattGodeMuligheterEtikett.ANTATT_GODE_MULIGHETER)
    }

    @Test
    fun `ett feilet oppfolgingskall gir ukjent for alle avhengige felter og lar andre kilder lykkes`() {
        var antallKall = 0
        whenever(varsel.harGjeldendeEskaleringsvarsel(any())).thenReturn(true)
        val medEkteOppfolging = VisittkortEtiketterService(
            pdl, representasjon, skjerming,
            oppfolgingFraHttp(503, "sensitive-source-message") { antallKall++ },
            arbeidssoker, vedtak, varsel, registry
        )

        val resultat = medEkteOppfolging.hent(TEST_FNR, null,
            VisittkortEtiketterService.OPPFOLGING_FELTER + "varsel")

        assertThat(resultat.ukjenteFelter().toSet()).isEqualTo(VisittkortEtiketterService.OPPFOLGING_FELTER)
        assertThat(resultat.varsel).isEqualTo(VarselEtikett.VARSEL)
        assertThat(antallKall).isEqualTo(1)
        assertThat(registry.get("visittkort_kilde_utilgjengelig").tag("kilde", "oppfolging").counter().count())
            .isEqualTo(1.0)
    }

    @Test
    fun `ekte ugyldige oppfolgingssvar og tilgangsfeil stopper oppslaget uten ukjent`() {
        fun serviceMedSvar(status: Int, body: String) = VisittkortEtiketterService(
            pdl, representasjon, skjerming, oppfolgingFraHttp(status, body),
            arbeidssoker, vedtak, varsel, registry
        )
        val felter = setOf("ikkeUnderOppfolging", "varsel")
        for (body in listOf(
            """{"data":{"oppfolging":{"erUnderOppfolging":null}}}""",
            """{"errors":[{"message":"sensitive-source-message"}],"data":{"oppfolging":{"erUnderOppfolging":false}}}"""
        )) {
            val feil = assertThrows<VisittkortKildeKontraktException> {
                serviceMedSvar(200, body).hent(TEST_FNR, null, felter)
            }
            assertThat(feil.toString()).doesNotContain("sensitive-source-message", TEST_FNR.get())
        }
        val feil = assertThrows<VisittkortKildeHttpException> {
            serviceMedSvar(403, "sensitive-source-message").hent(TEST_FNR, null, felter)
        }
        assertThat(feil.statusCode.value()).isEqualTo(403)
        assertThat(registry.find("visittkort_kilde_utilgjengelig").counter()).isNull()
        verifyNoInteractions(vedtak, arbeidssoker, varsel)
    }

}
