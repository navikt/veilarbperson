package no.nav.veilarbperson.controller

import no.nav.common.types.identer.Fnr
import no.nav.veilarbperson.client.pdl.domain.Telefon
import no.nav.veilarbperson.domain.PersonVisittkortData
import no.nav.veilarbperson.service.AuthService
import no.nav.veilarbperson.service.PersonVisittkortService
import no.nav.veilarbperson.utils.TestData.TEST_FNR
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest
import org.springframework.graphql.test.tester.GraphQlTester
import org.springframework.graphql.test.tester.entity
import org.springframework.http.HttpStatus
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.server.ResponseStatusException

@GraphQlTest(PersonGraphQLController::class)
internal class PersonGraphQLControllerTest {

    @Autowired
    lateinit var graphQlTester: GraphQlTester

    @MockitoBean
    lateinit var personVisittkortService: PersonVisittkortService

    @MockitoBean
    lateinit var authService: AuthService

    @Test
    fun henter_navn_og_fodselsdato() {
        whenever(personVisittkortService.hentVisittkortData(any(), any())).thenReturn(
            PersonVisittkortData(fornavn = "Kari", etternavn = "Nordmann", fodselsdato = "1980-01-15")
        )

        graphQlTester.document("""
            query {
                person(fnr: "$TEST_FNR", behandlingsnummer: "B643") {
                    fornavn
                    etternavn
                    fodselsdato
                    egenAnsatt
                }
            }
        """)
            .execute()
            .path("person.fornavn").entity<String>().isEqualTo("Kari")
            .path("person.etternavn").entity<String>().isEqualTo("Nordmann")
            .path("person.fodselsdato").entity<String>().isEqualTo("1980-01-15")
            .path("person.egenAnsatt").entity<Boolean>().isEqualTo(false)
    }

    @Test
    fun skjermet_person_returnerer_egenAnsatt_true() {
        whenever(personVisittkortService.hentVisittkortData(any(), any())).thenReturn(
            PersonVisittkortData(egenAnsatt = true)
        )

        graphQlTester.document("""
            query {
                person(fnr: "$TEST_FNR", behandlingsnummer: "B643") {
                    egenAnsatt
                }
            }
        """)
            .execute()
            .path("person.egenAnsatt").entity<Boolean>().isEqualTo(true)
    }

    @Test
    fun returnerer_null_felt_naar_service_returnerer_tomme_verdier() {
        whenever(personVisittkortService.hentVisittkortData(any(), any())).thenReturn(
            PersonVisittkortData()
        )

        graphQlTester.document("""
            query {
                person(fnr: "$TEST_FNR", behandlingsnummer: "B643") {
                    fornavn
                    egenAnsatt
                }
            }
        """)
            .execute()
            .path("person.fornavn").valueIsNull()
            .path("person.egenAnsatt").entity<Boolean>().isEqualTo(false)
    }

    @Test
    fun returnerer_telefoner_fra_service() {
        val telefoner = listOf(
            Telefon().setPrioritet("1").setTelefonNr("99999999").setMaster("KRR"),
            Telefon().setPrioritet("2").setTelefonNr("11111111").setMaster("PDL")
        )
        whenever(personVisittkortService.hentVisittkortData(any(), any())).thenReturn(
            PersonVisittkortData(telefon = telefoner)
        )

        graphQlTester.document("""
            query {
                person(fnr: "$TEST_FNR", behandlingsnummer: "B643") {
                    telefon { telefonNr prioritet master }
                }
            }
        """)
            .execute()
            .path("person.telefon[0].telefonNr").entity<String>().isEqualTo("99999999")
            .path("person.telefon[0].prioritet").entity<String>().isEqualTo("1")
            .path("person.telefon[0].master").entity<String>().isEqualTo("KRR")
            .path("person.telefon[1].telefonNr").entity<String>().isEqualTo("11111111")
            .path("person.telefon[1].prioritet").entity<String>().isEqualTo("2")
    }

    @Test
    fun blokkerer_ekstern_bruker() {
        doThrow(ResponseStatusException(HttpStatus.FORBIDDEN)).`when`(authService).stoppHvisEksternBruker()

        graphQlTester.document("""
            query {
                person(fnr: "$TEST_FNR", behandlingsnummer: "B643") {
                    fornavn
                }
            }
        """)
            .execute()
            .errors()
            .satisfy { errors -> assertThat(errors).isNotEmpty() }

        verify(authService).stoppHvisEksternBruker()
    }

    @Test
    fun blokkerer_veileder_uten_lesetilgang() {
        doThrow(ResponseStatusException(HttpStatus.FORBIDDEN)).`when`(authService).sjekkLesetilgang(any())

        graphQlTester.document("""
            query {
                person(fnr: "$TEST_FNR", behandlingsnummer: "B643") {
                    fornavn
                }
            }
        """)
            .execute()
            .errors()
            .satisfy { errors -> assertThat(errors).isNotEmpty() }

        verify(authService).sjekkLesetilgang(Fnr.of(TEST_FNR.get()))
    }
}
