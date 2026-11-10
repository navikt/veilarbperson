package no.nav.veilarbperson.service

import no.nav.veilarbperson.client.VisittkortKildeIkkeKonfigurertException
import no.nav.veilarbperson.client.VisittkortKildeHttpException
import no.nav.veilarbperson.client.VisittkortKildeKontraktException
import no.nav.veilarbperson.client.VisittkortKildeUtilgjengeligException
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.ArbeidssokerVisittkortData
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.ProfilertTil
import no.nav.veilarbperson.client.pdl.domain.PdlRequest
import no.nav.veilarbperson.client.representasjon.VisittkortRepresentasjonData
import no.nav.veilarbperson.domain.visittkort.*
import no.nav.veilarbperson.utils.TestData.TEST_FNR
import no.nav.veilarbperson.utils.VisittkortEtiketterMapperTest.Companion.ALLE_FELTER
import no.nav.veilarbperson.utils.VisittkortEtiketterMapperTest.Companion.oppfolging
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.io.IOException
import java.util.function.Supplier

class VisittkortEtiketterServiceTest : VisittkortEtiketterServiceTestBase() {

    @Test
    fun `alle felter deler kildeoppslag og behandlingsnummer videresendes uendret`() {
        service.hent(TEST_FNR, "B643", ALLE_FELTER)

        verify(pdl).hentVisittkortEtiketter(PdlRequest(TEST_FNR, "B643"), VisittkortEtiketterService.PDL_FELTER)
        verify(representasjon).hentOppfolgingsrepresentasjon(TEST_FNR)
        verify(skjerming).hentSkjermet(TEST_FNR)
        verify(oppfolgingClient).hentEtikettgrunnlag(TEST_FNR,
            VisittkortEtiketterService.OPPFOLGING_FELTER +
                VisittkortEtiketterService.PROFILERING_FELTER + "trengerOppfolgingsvedtak")
        verify(arbeidssoker).hentVisittkortData(TEST_FNR, true)
        verify(vedtak).harGjeldendeVedtak(TEST_FNR)
        verify(varsel).harGjeldendeEskaleringsvarsel(TEST_FNR)
        verifyNoMoreInteractions(pdl, representasjon, skjerming, oppfolgingClient, arbeidssoker, vedtak, varsel)
    }

    @Test
    fun `enkeltfelt henter ikke andre etikettkilder`() {
        service.hent(TEST_FNR, null, setOf("skjerming"))

        verify(skjerming).hentSkjermet(TEST_FNR)
        verifyNoInteractions(pdl, representasjon, oppfolgingClient, arbeidssoker, vedtak, varsel)
    }

    @Test
    fun `bare registeretikett trenger ikke oppfolging profilering eller vedtak`() {
        service.hent(TEST_FNR, null, setOf("iArbeidssokerregisteret"))

        verify(arbeidssoker).hentVisittkortData(TEST_FNR, false)
        verifyNoInteractions(pdl, representasjon, skjerming, oppfolgingClient, vedtak, varsel)
    }

    @Test
    fun `transportfeil gir ukjent bare for den feilede kilden`() {
        whenever(pdl.hentVisittkortEtiketter(any(), any())).thenAnswer { throw IOException("source unavailable") }
        whenever(representasjon.hentOppfolgingsrepresentasjon(any())).thenReturn(VisittkortRepresentasjonData(false, true))

        val resultat = service.hent(TEST_FNR, "B643",
            setOf("dod", "sikkerhetstiltak", "fullmaktOppfolging"))

        assertThat(resultat.dod).isEqualTo(DodEtikett.UKJENT_PERSONALIA)
        assertThat(resultat.sikkerhetstiltak?.beskrivelse).isNull()
        assertThat(resultat.fullmaktOppfolging).isEqualTo(FullmaktOppfolgingEtikett.FULLMAKT_OPPFOLGING)
        assertThat(registry.get("visittkort_etikett_ukjent").tag("etikett", "dod").counter().count())
            .isEqualTo(1.0)
    }

    @Test
    fun `ekte utilgjengelig kilde gir ukjent og tillater andre kilder`() {
        whenever(oppfolgingClient.hentEtikettgrunnlag(any(), any())).thenThrow(VisittkortKildeUtilgjengeligException())
        whenever(varsel.harGjeldendeEskaleringsvarsel(any())).thenReturn(true)

        val resultat = service.hent(TEST_FNR, null, setOf("kvp", "varsel"))

        assertThat(resultat.kvp).isEqualTo(KvpEtikett.UKJENT_OPPFOLGING)
        assertThat(resultat.varsel).isEqualTo(VarselEtikett.VARSEL)
    }

    @Test
    fun `tilgangsfeil ugyldig data og uklassifiserte feil gir ikke ukjent`() {
        for (feil in listOf(
            ResponseStatusException(HttpStatus.UNAUTHORIZED),
            ResponseStatusException(HttpStatus.FORBIDDEN),
            ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS),
            ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR),
            VisittkortKildeKontraktException(),
            IllegalStateException("invalid source")
        )) {
            doThrow(feil).whenever(pdl).hentVisittkortEtiketter(any(), any())

            assertThat(assertThrows<RuntimeException> {
                service.hent(TEST_FNR, "B643", setOf("dod", "skjerming"))
            }).isSameAs(feil)
        }
        verifyNoInteractions(skjerming)
    }

    @Test
    fun `person uten oppfolging trenger ikke vedtaksoppslag`() {
        whenever(oppfolgingClient.hentEtikettgrunnlag(any(), any())).thenReturn(oppfolging().copy(underOppfolging = false))

        assertThat(service.hent(TEST_FNR, null, setOf("trengerOppfolgingsvedtak"))
            .trengerOppfolgingsvedtak).isNull()
        verifyNoInteractions(vedtak, arbeidssoker)
    }

    @Test
    fun `gjeldende vedtak undertrykker profilering uten registerkall`() {
        whenever(vedtak.harGjeldendeVedtak(any())).thenReturn(true)

        assertThat(service.hent(TEST_FNR, null, VisittkortEtiketterService.PROFILERING_FELTER))
            .isEqualTo(VisittkortEtiketter())
        verifyNoInteractions(arbeidssoker)
    }

    @Test
    fun `registerfelt henter periode men ikke profilering naar vedtak undertrykker profiletiketter`() {
        whenever(vedtak.harGjeldendeVedtak(any())).thenReturn(true)

        service.hent(TEST_FNR, null,
            VisittkortEtiketterService.PROFILERING_FELTER + "iArbeidssokerregisteret")

        verify(arbeidssoker).hentVisittkortData(TEST_FNR, false)
    }

    @Test
    fun `feilet vedtaksoppslag gir ikke etikett for manglende vedtak`() {
        whenever(vedtak.harGjeldendeVedtak(any())).thenThrow(VisittkortKildeUtilgjengeligException())

        val resultat = service.hent(TEST_FNR, null, setOf("trengerOppfolgingsvedtak"))

        assertThat(resultat.trengerOppfolgingsvedtak)
            .isEqualTo(TrengerOppfolgingsvedtakEtikett.UKJENT_OPPFOLGINGSVEDTAK)
    }

    @Test
    fun `profilering uten gjeldende vedtak krever ikke under oppfolging`() {
        whenever(oppfolgingClient.hentEtikettgrunnlag(any(), any())).thenReturn(oppfolging().copy(underOppfolging = false))
        whenever(arbeidssoker.hentVisittkortData(any(), any())).thenReturn(
            ArbeidssokerVisittkortData(true, ProfilertTil.ANTATT_GODE_MULIGHETER)
        )

        assertThat(service.hent(TEST_FNR, null, setOf("antattGodeMuligheter")).antattGodeMuligheter)
            .isEqualTo(AntattGodeMuligheterEtikett.ANTATT_GODE_MULIGHETER)
    }

    @Test
    fun `oppslag deler aldri personresultater mellom forespoersler`() {
        whenever(skjerming.hentSkjermet(TEST_FNR)).thenReturn(true, false)

        assertThat(service.hent(TEST_FNR, null, setOf("skjerming")).skjerming)
            .isEqualTo(SkjermingEtikett.SKJERMET)
        assertThat(service.hent(TEST_FNR, null, setOf("skjerming")).skjerming).isNull()
        verify(skjerming, times(2)).hentSkjermet(TEST_FNR)
    }

}
