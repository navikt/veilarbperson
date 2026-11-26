package no.nav.veilarbperson.service

import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.ArbeidssokerperiodeResponse
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.BrukerResponse
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.BrukerType
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.MetadataResponse
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.OppslagArbeidssoekerregisteretClient
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.OpplysningerOmArbeidssoekerResponse
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.ProfileringResponse
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.ProfilertTil
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.UtdanningResponse
import no.nav.veilarbperson.utils.TestData.TEST_FNR
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.whenever
import org.springframework.web.server.ResponseStatusException
import java.time.ZonedDateTime
import java.util.UUID

class OppslagArbeidssoekerregisteretServiceTest {

    private val client = mock(OppslagArbeidssoekerregisteretClient::class.java)
    private val service = OppslagArbeidssoekerregisteretService(client)

    private val periodeId = UUID.fromString("00000000-0000-0000-0000-000000000001")
    private val eldreOpplysningerId = UUID.fromString("00000000-0000-0000-0000-000000000002")
    private val nyesteOpplysningerId = UUID.fromString("00000000-0000-0000-0000-000000000003")

    private fun metadata(tidspunkt: String) = MetadataResponse(
        ZonedDateTime.parse(tidspunkt), BrukerResponse(BrukerType.SLUTTBRUKER, null), "kilde", "aarsak"
    )

    private fun periode(avsluttet: MetadataResponse? = null) =
        ArbeidssokerperiodeResponse(periodeId, metadata("2026-01-01T00:00:00Z"), avsluttet)

    private fun opplysninger(id: UUID, tidspunkt: String) = OpplysningerOmArbeidssoekerResponse(
        id, periodeId, metadata(tidspunkt), null, null, null, emptyList()
    )

    private fun profilering(opplysningerId: UUID, tidspunkt: String, profilertTil: ProfilertTil) =
        ProfileringResponse(
            UUID.randomUUID(), periodeId, opplysningerId, metadata(tidspunkt), profilertTil, null, null
        )

    @Test
    fun `visittkort uten aktiv periode er ikke registrert og henter ikke mer`() {
        for (perioder in listOf(null, emptyList(), listOf(periode(metadata("2026-02-01T00:00:00Z"))))) {
            whenever(client.hentArbeidssokerPerioder(TEST_FNR.get())).thenReturn(perioder)

            val resultat = service.hentVisittkortData(TEST_FNR, true)

            assertThat(resultat.registrert).isFalse()
            assertThat(resultat.profilering).isNull()
        }
        verify(client, never()).hentOpplysningerOmArbeidssoeker(any(), any())
        verify(client, never()).hentProfilering(any(), any())
    }

    @Test
    fun `visittkort uten profilering henter bare perioder`() {
        whenever(client.hentArbeidssokerPerioder(TEST_FNR.get())).thenReturn(listOf(periode()))

        val resultat = service.hentVisittkortData(TEST_FNR, false)

        assertThat(resultat.registrert).isTrue()
        assertThat(resultat.profilering).isNull()
        verify(client).hentArbeidssokerPerioder(TEST_FNR.get())
        verifyNoMoreInteractions(client)
    }

    @Test
    fun `visittkort bruker profilering for siste opplysninger`() {
        whenever(client.hentArbeidssokerPerioder(TEST_FNR.get())).thenReturn(listOf(periode()))
        whenever(client.hentOpplysningerOmArbeidssoeker(TEST_FNR.get(), periodeId)).thenReturn(
            listOf(
                opplysninger(nyesteOpplysningerId, "2026-03-01T00:00:00Z"),
                opplysninger(eldreOpplysningerId, "2026-02-01T00:00:00Z")
            )
        )
        whenever(client.hentProfilering(TEST_FNR.get(), periodeId)).thenReturn(
            listOf(
                profilering(eldreOpplysningerId, "2026-05-01T00:00:00Z", ProfilertTil.OPPGITT_HINDRINGER),
                profilering(nyesteOpplysningerId, "2026-03-02T00:00:00Z", ProfilertTil.ANTATT_BEHOV_FOR_VEILEDNING),
                profilering(nyesteOpplysningerId, "2026-03-03T00:00:00Z", ProfilertTil.ANTATT_GODE_MULIGHETER)
            )
        )

        val resultat = service.hentVisittkortData(TEST_FNR, true)

        assertThat(resultat.registrert).isTrue()
        assertThat(resultat.profilering).isEqualTo(ProfilertTil.ANTATT_GODE_MULIGHETER)
    }

    @Test
    fun `visittkort uten opplysninger eller matchende profilering er registrert uten profilering`() {
        whenever(client.hentArbeidssokerPerioder(TEST_FNR.get())).thenReturn(listOf(periode()))
        whenever(client.hentOpplysningerOmArbeidssoeker(TEST_FNR.get(), periodeId)).thenReturn(null)

        assertThat(service.hentVisittkortData(TEST_FNR, true).profilering).isNull()
        verify(client, never()).hentProfilering(any(), any())

        whenever(client.hentOpplysningerOmArbeidssoeker(TEST_FNR.get(), periodeId))
            .thenReturn(listOf(opplysninger(nyesteOpplysningerId, "2026-03-01T00:00:00Z")))
        whenever(client.hentProfilering(TEST_FNR.get(), periodeId)).thenReturn(
            listOf(profilering(eldreOpplysningerId, "2026-03-02T00:00:00Z", ProfilertTil.OPPGITT_HINDRINGER))
        )

        val resultat = service.hentVisittkortData(TEST_FNR, true)

        assertThat(resultat.registrert).isTrue()
        assertThat(resultat.profilering).isNull()
    }

    @Test
    fun `opplysninger og profilering gir 204 uten aktiv periode`() {
        whenever(client.hentArbeidssokerPerioder(TEST_FNR.get())).thenReturn(emptyList())

        val feil = assertThrows<ResponseStatusException> {
            service.hentSisteOpplysningerOmArbeidssoekerMedProfilering(TEST_FNR)
        }

        assertThat(feil.statusCode.value()).isEqualTo(204)
        verify(client, never()).hentOpplysningerOmArbeidssoeker(any(), any())
    }

    @Test
    fun `opplysninger og profilering bruker samme siste opplysninger og matchende profilering`() {
        whenever(client.hentArbeidssokerPerioder(TEST_FNR.get())).thenReturn(listOf(periode()))
        val siste = opplysninger(nyesteOpplysningerId, "2026-03-01T00:00:00Z")
            .copy(utdanning = UtdanningResponse("6", null, null))
        val matchende = profilering(nyesteOpplysningerId, "2026-03-02T00:00:00Z", ProfilertTil.OPPGITT_HINDRINGER)
        whenever(client.hentOpplysningerOmArbeidssoeker(TEST_FNR.get(), periodeId))
            .thenReturn(listOf(opplysninger(eldreOpplysningerId, "2026-02-01T00:00:00Z"), siste))
        whenever(client.hentProfilering(TEST_FNR.get(), periodeId)).thenReturn(
            listOf(profilering(eldreOpplysningerId, "2026-04-01T00:00:00Z", ProfilertTil.ANTATT_GODE_MULIGHETER), matchende)
        )

        val resultat = requireNotNull(service.hentSisteOpplysningerOmArbeidssoekerMedProfilering(TEST_FNR))

        assertThat(resultat.arbeidssoekerperiodeStartet).isEqualTo(periode().startet.tidspunkt)
        assertThat(resultat.opplysningerOmArbeidssoeker?.opplysningerOmArbeidssoekerId).isEqualTo(nyesteOpplysningerId)
        assertThat(resultat.opplysningerOmArbeidssoeker?.utdanning?.nus).isEqualTo("HOYERE_UTDANNING_1_TIL_4")
        assertThat(resultat.profilering).isEqualTo(matchende)
    }

    @Test
    fun `opplysninger og profilering uten opplysninger gir bare periodestart`() {
        whenever(client.hentArbeidssokerPerioder(TEST_FNR.get())).thenReturn(listOf(periode()))
        whenever(client.hentOpplysningerOmArbeidssoeker(TEST_FNR.get(), periodeId)).thenReturn(null)

        val resultat = requireNotNull(service.hentSisteOpplysningerOmArbeidssoekerMedProfilering(TEST_FNR))

        assertThat(resultat.arbeidssoekerperiodeStartet).isEqualTo(periode().startet.tidspunkt)
        assertThat(resultat.opplysningerOmArbeidssoeker).isNull()
        assertThat(resultat.profilering).isNull()
        verify(client, never()).hentProfilering(any(), any())
    }
}
