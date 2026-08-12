package no.nav.veilarbperson.service

import no.nav.veilarbperson.client.digdir.DigdirClient
import no.nav.veilarbperson.client.digdir.DigdirKontaktinfo
import no.nav.veilarbperson.client.digdir.KRRPostPersonerResponse
import no.nav.veilarbperson.client.nom.SkjermetClient
import no.nav.veilarbperson.client.pdl.HentPerson
import no.nav.veilarbperson.client.pdl.PdlClient
import no.nav.veilarbperson.utils.TestData.TEST_FNR
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

class PersonVisittkortServiceTest {

    private val pdlClient = mock(PdlClient::class.java)
    private val skjermetClient = mock(SkjermetClient::class.java)
    private val digdirClient = mock(DigdirClient::class.java)

    private val service = PersonVisittkortService(pdlClient, digdirClient, skjermetClient)

    @Test
    fun henter_navn_og_fodselsdato() {
        whenever(pdlClient.hentPerson(any())).thenReturn(lagTestPerson())
        whenever(skjermetClient.hentSkjermet(any())).thenReturn(false)
        whenever(digdirClient.hentKontaktInfo(any())).thenReturn(null)

        val result = service.hentVisittkortData(TEST_FNR, "B643")

        assertThat(result.fornavn).isEqualTo("Kari")
        assertThat(result.etternavn).isEqualTo("Nordmann")
        assertThat(result.fodselsdato).isEqualTo("1980-01-15")
        assertThat(result.egenAnsatt).isFalse()
    }

    @Test
    fun kaster_NOT_FOUND_naar_pdl_returnerer_null() {
        whenever(pdlClient.hentPerson(any())).thenReturn(null)

        val ex = assertThrows<ResponseStatusException> {
            service.hentVisittkortData(TEST_FNR, "B643")
        }
        assertThat(ex.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun skjermet_person_returnerer_egenAnsatt_true() {
        whenever(pdlClient.hentPerson(any())).thenReturn(lagTestPerson())
        whenever(skjermetClient.hentSkjermet(any())).thenReturn(true)
        whenever(digdirClient.hentKontaktInfo(any())).thenReturn(null)

        val result = service.hentVisittkortData(TEST_FNR, "B643")

        assertThat(result.egenAnsatt).isTrue()
    }

    @Test
    fun krr_telefon_faar_prioritet_1_og_pdl_telefon_bumpes() {
        val pdlMetadata = HentPerson.Metadata().apply {
            master = "PDL"
            endringer = emptyList()
        }
        val pdlTelefon = HentPerson.Telefonnummer().apply {
            nummer = "11111111"
            prioritet = "1"
            metadata = pdlMetadata
        }
        val personMedTelefon = lagTestPerson().apply { telefonnummer = listOf(pdlTelefon) }

        val krrInfo = DigdirKontaktinfo(
            TEST_FNR.get(), true, true, null, false,
            null, null, null, null, null,
            "99999999", "2024-01-15T12:00:00+01:00", null
        )
        val krrResponse = KRRPostPersonerResponse(mapOf(TEST_FNR.get() to krrInfo), null)

        whenever(pdlClient.hentPerson(any())).thenReturn(personMedTelefon)
        whenever(skjermetClient.hentSkjermet(any())).thenReturn(false)
        whenever(digdirClient.hentKontaktInfo(any())).thenReturn(krrResponse)

        val result = service.hentVisittkortData(TEST_FNR, "B643")

        assertThat(result.telefon).hasSize(2)
        assertThat(result.telefon[0].telefonNr).isEqualTo("99999999")
        assertThat(result.telefon[0].prioritet).isEqualTo("1")
        assertThat(result.telefon[0].master).isEqualTo("KRR")
        assertThat(result.telefon[1].telefonNr).isEqualTo("11111111")
        assertThat(result.telefon[1].prioritet).isEqualTo("2")
    }

    @Test
    fun krr_feil_gir_person_uten_krr_telefon() {
        whenever(pdlClient.hentPerson(any())).thenReturn(lagTestPerson())
        whenever(skjermetClient.hentSkjermet(any())).thenReturn(false)
        whenever(digdirClient.hentKontaktInfo(any()))
            .thenThrow(RuntimeException("KRR utilgjengelig"))

        val result = service.hentVisittkortData(TEST_FNR, "B643")

        assertThat(result.telefon).isEmpty()
        assertThat(result.fornavn).isEqualTo("Kari")
    }

    @Test
    fun skjermet_feil_gir_egenAnsatt_false() {
        whenever(pdlClient.hentPerson(any())).thenReturn(lagTestPerson())
        whenever(skjermetClient.hentSkjermet(any()))
            .thenThrow(RuntimeException("Opplysninger om Skjermet er utilgjengelig"))
        whenever(digdirClient.hentKontaktInfo(any())).thenReturn(null)

        val result = service.hentVisittkortData(TEST_FNR, "B643")

        assertThat(result.egenAnsatt).isFalse()
        assertThat(result.fornavn).isEqualTo("Kari")
    }

    private fun lagTestPerson() = HentPerson.Person().apply {
        navn = listOf(HentPerson.Navn().apply {
            fornavn = "Kari"
            etternavn = "Nordmann"
            metadata = HentPerson.MetadataNavn().apply { master = HentPerson.PdlNavnMaster.PDL }
        })
        foedselsdato = listOf(HentPerson.Foedselsdato().apply {
            foedselsdato = LocalDate.of(1980, 1, 15)
        })
        doedsfall = emptyList()
        kjoenn = emptyList()
        adressebeskyttelse = emptyList()
        sikkerhetstiltak = emptyList()
        telefonnummer = emptyList()
    }
}
