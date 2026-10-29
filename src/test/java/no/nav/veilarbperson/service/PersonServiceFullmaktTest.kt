package no.nav.veilarbperson.service

import no.nav.common.types.identer.Fnr
import no.nav.veilarbperson.client.pdl.HentPerson
import no.nav.veilarbperson.client.pdl.HentPerson.MetadataNavn
import no.nav.veilarbperson.client.pdl.PdlClient
import no.nav.veilarbperson.client.pdl.domain.PdlRequest
import no.nav.veilarbperson.client.representasjon.ReprFullmaktV2Data
import no.nav.veilarbperson.client.representasjon.RepresentasjonClient
import no.nav.veilarbperson.domain.FullmaktDTO.OmraadeHandlingType
import no.nav.veilarbperson.domain.PersonRequest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito.CALLS_REAL_METHODS
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.io.IOException
import java.time.LocalDate

class PersonServiceFullmaktTest {
    private val pdlClient = mock<PdlClient>()
    private val representasjonClient = mock<RepresentasjonClient>()
    private val kodeverkService = mock<KodeverkService>()
    private var datoMock: MockedStatic<LocalDate>? = null

    private val service = PersonService(
        pdlClient, null, null, null, null,
        kodeverkService, representasjonClient
    )

    @Before
    fun setup() {
        val mock = mockStatic(LocalDate::class.java, CALLS_REAL_METHODS)
        datoMock = mock
        mock.`when`<LocalDate> { LocalDate.now() }.thenReturn(IDAG)

        whenever(pdlClient.hentPersonNavn(PdlRequest(GIVER, "B555")))
            .thenReturn(navn("Giver"))
        whenever(pdlClient.hentPersonNavn(PdlRequest(MEKTIG, "B555")))
            .thenReturn(navn("Fullmektig"))
        whenever(kodeverkService.getBeskrivelseForTema("AAP")).thenReturn("Arbeidsavklaringspenger")
    }

    @After
    fun ryddOppDatoMock() {
        datoMock?.close()
    }

    @Test
    fun `mocker dagens dato`() {
        assertEquals(IDAG, LocalDate.now())
        assertEquals(IDAG, LocalDate.of(2026, 10, 22))
    }

    @Test
    fun filtrererPaaSluttdatoOgBeholderFremtidigStartOgIngenSluttdato() {
        whenever(representasjonClient.hentFullmakt(GIVER.get())).thenReturn(
            listOf(
                fullmakt(UTGAATT_MEKTIG, IDAG.minusYears(1), IDAG.minusDays(1)),
                fullmakt(MEKTIG, IDAG.minusDays(10), IDAG),
                fullmakt(MEKTIG, IDAG.minusDays(10), IDAG.plusDays(1)),
                fullmakt(MEKTIG, IDAG.plusYears(2), IDAG.plusYears(3)),
                fullmakt(MEKTIG, IDAG.minusYears(1), null),
                fullmakt(MEKTIG, IDAG.plusYears(2), null)
            )
        )

        val resultat = service.hentFullmakt(REQUEST).fullmakt

        assertEquals(5, resultat.size)
        assertEquals(IDAG, resultat[0].gyldigTilOgMed)
        assertEquals(IDAG.plusDays(1), resultat[1].gyldigTilOgMed)
        assertEquals(IDAG.plusYears(2), resultat[2].gyldigFraOgMed)
        assertNull(resultat[3].gyldigTilOgMed)
        assertNull(resultat[4].gyldigTilOgMed)
        assertEquals(IDAG.plusYears(2), resultat[4].gyldigFraOgMed)
        verify(pdlClient).hentPersonNavn(PdlRequest(GIVER, "B555"))
        verify(pdlClient).hentPersonNavn(PdlRequest(MEKTIG, "B555"))
        verifyNoMoreInteractions(pdlClient)
    }

    @Test
    fun tomListeOgBareUtgaatteEllerTidligAvsluttedeGir204UtenNavneoppslag() {
        for (fullmakter in listOf(
            emptyList(),
            listOf(fullmakt(MEKTIG, IDAG.minusYears(1), IDAG.minusDays(1))),
            listOf(fullmakt(MEKTIG, IDAG.plusDays(10), IDAG.minusDays(1)))
        )) {
            whenever(representasjonClient.hentFullmakt(GIVER.get())).thenReturn(fullmakter)

            val feil = assertThrows(ResponseStatusException::class.java) { service.hentFullmakt(REQUEST) }

            assertEquals(HttpStatus.NO_CONTENT, feil.statusCode)
        }
        verifyNoInteractions(pdlClient, kodeverkService)
    }

    @Test
    fun konsistenskontrollSkjerForHeleListenFoerNavneoppslag() {
        whenever(representasjonClient.hentFullmakt(GIVER.get())).thenReturn(
            listOf(
                fullmakt(MEKTIG, IDAG, null),
                ReprFullmaktV2Data(
                    MEKTIG,
                    GIVER,
                    IDAG.minusYears(1),
                    IDAG.minusDays(1),
                    emptyList(),
                    emptyList()
                )
            )
        )

        val feil = assertThrows(IllegalStateException::class.java) { service.hentFullmakt(REQUEST) }

        assertFalse(feil.toString().contains(GIVER.get()))
        assertFalse(feil.toString().contains(MEKTIG.get()))
        verifyNoInteractions(pdlClient, kodeverkService)
    }

    @Test
    fun berikerBeggeNavnOgTemabeskrivelserOgDeduplisererBarePerRequest() {
        whenever(representasjonClient.hentFullmakt(GIVER.get())).thenReturn(
            listOf(
                fullmakt(MEKTIG, IDAG, null), fullmakt(MEKTIG, IDAG.plusDays(1), null)
            )
        )

        val resultat = service.hentFullmakt(REQUEST).fullmakt.first()

        assertEquals(GIVER.get(), resultat.fullmaktsgiver)
        assertEquals(MEKTIG.get(), resultat.fullmektig)
        assertEquals("Giver Mellom Testesen", resultat.fullmaktsgiverNavn)
        assertEquals("Fullmektig Mellom Testesen", resultat.fullmektigsNavn)
        assertEquals("Arbeidsavklaringspenger", resultat.omraade[0].tema)
        assertEquals("alle ytelser", resultat.omraade[1].tema)
        assertEquals(listOf(OmraadeHandlingType.SKRIV), resultat.omraade[1].handling)
        verify(pdlClient).hentPersonNavn(PdlRequest(GIVER, "B555"))
        verify(pdlClient).hentPersonNavn(PdlRequest(MEKTIG, "B555"))
        verifyNoMoreInteractions(pdlClient)

        service.hentFullmakt(REQUEST)
        verify(pdlClient, times(2)).hentPersonNavn(PdlRequest(GIVER, "B555"))
        verify(pdlClient, times(2)).hentPersonNavn(PdlRequest(MEKTIG, "B555"))
    }

    @Test
    fun sammeIdentIParteneHentesBareEnGang() {
        whenever(representasjonClient.hentFullmakt(GIVER.get()))
            .thenReturn(listOf(fullmakt(GIVER, IDAG, null)))
        service.hentFullmakt(REQUEST)
        verify(pdlClient).hentPersonNavn(PdlRequest(GIVER, "B555"))
        verifyNoMoreInteractions(pdlClient)
    }

    @Test
    fun manglendeNavnPaaHverAvParteneFeilerHeleKallet() {
        whenever(representasjonClient.hentFullmakt(GIVER.get()))
            .thenReturn(listOf(fullmakt(MEKTIG, IDAG, null)))
        for (ident in listOf(MEKTIG, GIVER)) {
            whenever(pdlClient.hentPersonNavn(PdlRequest(ident, "B555")))
                .thenReturn(HentPerson.PersonNavn().setNavn(emptyList()))

            val feil = assertThrows(ResponseStatusException::class.java) { service.hentFullmakt(REQUEST) }

            assertEquals(HttpStatus.NOT_FOUND, feil.statusCode)
        }
        verifyNoInteractions(kodeverkService)
    }

    @Test
    fun ufullstendigNavnGirIkkeTilsynelatendeGyldigRespons() {
        whenever(representasjonClient.hentFullmakt(GIVER.get()))
            .thenReturn(listOf(fullmakt(MEKTIG, IDAG, null)))
        val ufullstendigNavn = navn("Fullmektig")
        ufullstendigNavn.navn.first().etternavn = null
        whenever(pdlClient.hentPersonNavn(PdlRequest(MEKTIG, "B555")))
            .thenReturn(ufullstendigNavn)

        val feil = assertThrows(ResponseStatusException::class.java) { service.hentFullmakt(REQUEST) }

        assertEquals(HttpStatus.NOT_FOUND, feil.statusCode)
        verifyNoInteractions(kodeverkService)
    }

    @Test
    fun pdl403OgTekniskFeilBeholderFeiltypeUtenDelvisRespons() {
        whenever(representasjonClient.hentFullmakt(GIVER.get()))
            .thenReturn(listOf(fullmakt(MEKTIG, IDAG, null)))
        for (ident in listOf(MEKTIG, GIVER)) {
            for (aarsak in listOf(
                ResponseStatusException(HttpStatus.FORBIDDEN, "Feilbody med " + ident.get()),
                ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Feilbody med " + ident.get()),
                IllegalStateException("PDL utilgjengelig")
            )) {
                doThrow(aarsak).whenever(pdlClient).hentPersonNavn(PdlRequest(ident, "B555"))

                val feil = assertThrows(RuntimeException::class.java) { service.hentFullmakt(REQUEST) }
                if (aarsak is ResponseStatusException) {
                    assertEquals(aarsak.statusCode, (feil as ResponseStatusException).statusCode)
                    assertFalse(feil.toString().contains(ident.get()))
                    assertNull(feil.cause)
                } else {
                    assertSame(aarsak, feil)
                }
            }
        }
        verifyNoInteractions(kodeverkService)
    }

    @Test
    fun offentligMetodeBrukerSammeFullmaktsflyt() {
        whenever(representasjonClient.hentFullmakt(GIVER.get()))
            .thenReturn(listOf(fullmakt(MEKTIG, IDAG, null)))

        assertEquals(1, service.hentFullmakt(REQUEST).fullmakt.size)
        verify(pdlClient).hentPersonNavn(PdlRequest(GIVER, "B555"))
        verify(pdlClient).hentPersonNavn(PdlRequest(MEKTIG, "B555"))
    }

    @Test
    fun representasjonsfeilPropageresUtenNavneoppslag() {
        val feil = IOException("Representasjon utilgjengelig")
        whenever(representasjonClient.hentFullmakt(GIVER.get())).thenThrow(feil)

        assertSame(feil, assertThrows(IOException::class.java) { service.hentFullmakt(REQUEST) })
        verifyNoInteractions(pdlClient, kodeverkService)
    }

    companion object {
        private val GIVER: Fnr = Fnr.of("01010112345")
        private val MEKTIG: Fnr = Fnr.of("02020212345")
        private val UTGAATT_MEKTIG: Fnr = Fnr.of("03030312345")
        private val IDAG: LocalDate = LocalDate.of(2026, 10, 22)
        private val REQUEST = PersonRequest(GIVER, "B555")
        private fun fullmakt(fullmektig: Fnr, fra: LocalDate, til: LocalDate?): ReprFullmaktV2Data {
            return ReprFullmaktV2Data(
                GIVER,
                fullmektig,
                fra,
                til,
                listOf("AAP"),
                listOf("*")
            )
        }

        private fun navn(fornavn: String): HentPerson.PersonNavn {
            return HentPerson.PersonNavn().setNavn(
                listOf(
                    HentPerson.Navn()
                        .setFornavn(fornavn).setMellomnavn("Mellom").setEtternavn("Testesen")
                        .setForkortetNavn("Skal ikke brukes")
                        .setMetadata(MetadataNavn().setMaster(HentPerson.PdlNavnMaster.PDL))
                )
            )
        }
    }
}
