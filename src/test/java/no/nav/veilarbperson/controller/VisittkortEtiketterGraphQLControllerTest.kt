package no.nav.veilarbperson.controller

import no.nav.veilarbperson.domain.visittkort.*
import no.nav.veilarbperson.client.VisittkortKildeIkkeKonfigurertException
import no.nav.veilarbperson.client.VisittkortKildeKontraktException
import no.nav.veilarbperson.client.VisittkortKildeHttpException
import no.nav.veilarbperson.service.AuthService
import no.nav.veilarbperson.service.VisittkortEtiketterService
import no.nav.veilarbperson.utils.TestData.TEST_FNR
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest
import org.springframework.context.annotation.Import
import org.springframework.graphql.test.tester.GraphQlTester
import org.springframework.http.HttpStatus
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.server.ResponseStatusException

@GraphQlTest(VisittkortEtiketterGraphQLController::class)
@Import(VisittkortEtiketterExceptionResolver::class)
class VisittkortEtiketterGraphQLControllerTest {
    @Autowired
    lateinit var graphQlTester: GraphQlTester

    @MockitoBean
    lateinit var service: VisittkortEtiketterService

    @MockitoBean
    lateinit var authService: AuthService

    @BeforeEach
    fun internBruker() {
        whenever(authService.erInternBruker()).thenReturn(true)
        whenever(service.hent(any(), anyOrNull(), any())).thenReturn(VisittkortEtiketter())
    }

    @Test
    fun `returnerer enum null og ukjent med objektunntak og riktig rekkefolge`() {
        whenever(service.hent(any(), anyOrNull(), any())).thenReturn(
            VisittkortEtiketter(
                dod = DodEtikett.DOD,
                tegnspraaktolk = TegnspraaktolkEtikett.UKJENT_TOLKEBEHOV,
                sikkerhetstiltak = SikkerhetstiltakResultat(
                    SikkerhetstiltakEtikett.SIKKERHETSTILTAK, "Kildebeskrivelse"
                )
            )
        )

        graphQlTester.document(query("dod fullmaktOppfolging tegnspraaktolk sikkerhetstiltak { etikett beskrivelse }"))
            .execute()
            .path("visittkortEtiketter.dod").entity(String::class.java).isEqualTo("DOD")
            .path("visittkortEtiketter.fullmaktOppfolging").valueIsNull()
            .path("visittkortEtiketter.tegnspraaktolk").entity(String::class.java).isEqualTo("UKJENT_TOLKEBEHOV")
            .path("visittkortEtiketter.sikkerhetstiltak.beskrivelse").entity(String::class.java)
            .isEqualTo("Kildebeskrivelse")

        inOrder(authService, service) {
            verify(authService).erInternBruker()
            verify(authService).sjekkLesetilgang(TEST_FNR)
            verify(service).hent(TEST_FNR, "B643",
                setOf("dod", "fullmaktOppfolging", "tegnspraaktolk", "sikkerhetstiltak"))
        }
    }

    @Test
    fun `ekstern og systembruker avvises foer person og kildeoppslag`() {
        whenever(authService.erInternBruker()).thenReturn(false)

        graphQlTester.document(query("dod")).execute().errors().satisfy {
            assertThat(it.single().extensions?.get("code")).isEqualTo("INGEN_TILGANG")
        }.path("visittkortEtiketter").valueIsNull()

        verify(authService, never()).sjekkLesetilgang(any())
        verifyNoInteractions(service)
    }

    @Test
    fun `tilgangsavslag og feil i tilgangsavgjorelsen stopper alle kildeoppslag`() {
        for (feil in listOf(
            ResponseStatusException(HttpStatus.FORBIDDEN, "sensitive-access-details"),
            IllegalStateException("sensitive-access-details")
        )) {
            doThrow(feil).whenever(authService).sjekkLesetilgang(any())
            graphQlTester.document(query("dod")).execute().errors().satisfy {
                assertThat(it).isNotEmpty()
                assertThat(it.toString()).doesNotContain("sensitive-access-details", TEST_FNR.get())
            }.path("visittkortEtiketter").valueIsNull()
        }
        verifyNoInteractions(service)
    }

    @Test
    fun `ugyldig ident valideres uten aa ekkoe innsendt verdi`() {
        for (fnr in listOf("", "not-a-person", "123", "123456789012", "1234567890\n")) {
            graphQlTester.document("""
                query Test(${'$'}fnr: ID!) {
                  visittkortEtiketter(fnr: ${'$'}fnr) { skjerming }
                }
            """.trimIndent()).variable("fnr", fnr).execute().errors().satisfy {
                assertThat(it.single().extensions?.get("code")).isEqualTo("UGYLDIG_FORESPORSEL")
                assertThat(it.single().message).isEqualTo("UGYLDIG_FORESPORSEL")
            }
        }
        verify(authService, never()).sjekkLesetilgang(any())
        verifyNoInteractions(service)
    }

    @Test
    fun `PDL felt krever behandlingsnummer og ugyldige numre avvises foer tilgang`() {
        for (nummer in listOf(null, "", "b643", "B12345678901", "B643\n")) {
            graphQlTester.document("""
                query Test(${'$'}nummer: String) {
                  visittkortEtiketter(fnr: "${TEST_FNR.get()}", behandlingsnummer: ${'$'}nummer) { dod }
                }
            """.trimIndent()).variable("nummer", nummer).execute().errors().satisfy {
                assertThat(it.single().extensions?.get("code")).isEqualTo("UGYLDIG_FORESPORSEL")
            }
        }
        verify(authService, never()).sjekkLesetilgang(any())
        verifyNoInteractions(service)
    }

    @Test
    fun `ikke PDL felt kan ettersporres uten behandlingsnummer`() {
        graphQlTester.document("""
            { visittkortEtiketter(fnr: "${TEST_FNR.get()}") { skjerming } }
        """.trimIndent()).execute().path("visittkortEtiketter.skjerming").valueIsNull()

        verify(service).hent(TEST_FNR, null, setOf("skjerming"))
    }

    @Test
    fun `aliaser fragmenter og direktiver bruker kanoniske feltnavn`() {
        graphQlTester.document("""
            query Test(${'$'}hentTolk: Boolean!) {
              kort: visittkortEtiketter(fnr: "${TEST_FNR.get()}", behandlingsnummer: "B643") {
                ...Felter
                tolk: tegnspraaktolk @include(if: ${'$'}hentTolk)
                dod @skip(if: true)
              }
            }
            fragment Felter on VisittkortEtiketter { skjerming }
        """.trimIndent()).variable("hentTolk", false).execute()
            .path("kort.skjerming").valueIsNull()
            .path("kort.tolk").pathDoesNotExist()
            .path("kort.dod").pathDoesNotExist()

        verify(service).hent(TEST_FNR, "B643", setOf("skjerming"))
    }

    @Test
    fun `maksimalt ett aktivt rotoppslag tillates og avvisning skjer foer kildekall`() {
        graphQlTester.document("""
            {
              a: visittkortEtiketter(fnr: "${TEST_FNR.get()}") { skjerming }
              ...Flere
            }
            fragment Flere on Query {
              b: visittkortEtiketter(fnr: "${TEST_FNR.get()}") { skjerming }
            }
        """.trimIndent()).execute().errors().satisfy {
            assertThat(it).hasSize(2)
            assertThat(it.map { feil -> feil.extensions?.get("code") }).containsOnly("UGYLDIG_FORESPORSEL")
        }
        verifyNoInteractions(service)
        verify(authService, never()).sjekkLesetilgang(any())
    }

    @Test
    fun `rotalias som hoppes over teller ikke som et oppslag`() {
        graphQlTester.document("""
            query Test(${'$'}skip: Boolean!) {
              a: visittkortEtiketter(fnr: "${TEST_FNR.get()}") @skip(if: ${'$'}skip) { skjerming }
              b: visittkortEtiketter(fnr: "${TEST_FNR.get()}") { skjerming }
            }
        """.trimIndent()).variable("skip", true).execute()
            .path("b.skjerming").valueIsNull()
            .path("a").pathDoesNotExist()

        verify(service).hent(TEST_FNR, null, setOf("skjerming"))
    }

    @Test
    fun `gjentatte fragmenter utvider ikke antallet kildeoppslag`() {
        val fragmenter = (1..12).joinToString("\n") {
            "fragment F$it on Query { ...F${it - 1} ...F${it - 1} }"
        }
        graphQlTester.document("""
            { ...F12 }
            fragment F0 on Query {
              visittkortEtiketter(fnr: "${TEST_FNR.get()}") { skjerming }
            }
            $fragmenter
        """.trimIndent()).execute().path("visittkortEtiketter.skjerming").valueIsNull()

        verify(service).hent(TEST_FNR, null, setOf("skjerming"))
    }

    @Test
    fun `fatale kildefeil gir null rot med sanitert feil og ikke vanlige null etiketter`() {
        for ((feil, kode) in listOf(
            VisittkortKildeHttpException(HttpStatus.BAD_REQUEST) to "UGYLDIG_FORESPORSEL",
            VisittkortKildeHttpException(HttpStatus.UNAUTHORIZED) to "IKKE_AUTENTISERT",
            VisittkortKildeHttpException(HttpStatus.FORBIDDEN) to "INGEN_TILGANG",
            VisittkortKildeHttpException(HttpStatus.NOT_FOUND) to "PERSON_IKKE_FUNNET",
            VisittkortKildeHttpException(HttpStatus.TOO_MANY_REQUESTS) to "INTERN_FEIL",
            VisittkortKildeKontraktException() to "UGYLDIG_KILDESVAR",
            VisittkortKildeIkkeKonfigurertException() to "KILDE_IKKE_KONFIGURERT",
            IllegalStateException("sensitive-source-message") to "INTERN_FEIL"
        )) {
            doThrow(feil).whenever(service).hent(any(), anyOrNull(), any())
            graphQlTester.document(query("dod")).execute().errors().satisfy {
                assertThat(it.single().extensions?.get("code")).isEqualTo(kode)
                assertThat(it.single().message).isEqualTo(kode)
                assertThat(it.toString()).doesNotContain("sensitive-source-message", TEST_FNR.get())
            }.path("visittkortEtiketter").valueIsNull()
        }
    }

    @Test
    fun `sikkerhetstiltak ukjent inneholder aldri en gammel beskrivelse`() {
        whenever(service.hent(any(), anyOrNull(), any())).thenReturn(VisittkortEtiketter(
            sikkerhetstiltak = SikkerhetstiltakResultat(SikkerhetstiltakEtikett.UKJENT_SIKKERHETSTILTAK, null)
        ))

        graphQlTester.document(query("sikkerhetstiltak { etikett beskrivelse }")).execute()
            .path("visittkortEtiketter.sikkerhetstiltak.etikett").entity(String::class.java)
            .isEqualTo("UKJENT_SIKKERHETSTILTAK")
            .path("visittkortEtiketter.sikkerhetstiltak.beskrivelse").valueIsNull()
    }

    @Test
    fun `KVP enum null og ukjent kan hentes uten behandlingsnummer`() {
        for (etikett in listOf(KvpEtikett.KVP, null, KvpEtikett.UKJENT_OPPFOLGING)) {
            whenever(service.hent(any(), anyOrNull(), any()))
                .thenReturn(VisittkortEtiketter(kvp = etikett))
            val resultat = graphQlTester.document("""
                { visittkortEtiketter(fnr: "${TEST_FNR.get()}") { kvp } }
            """.trimIndent()).execute().path("visittkortEtiketter.kvp")
            if (etikett == null) resultat.valueIsNull()
            else resultat.entity(String::class.java).isEqualTo(etikett.name)
        }
        verify(service, times(3)).hent(TEST_FNR, null, setOf("kvp"))
    }

    private fun query(felter: String) = """
        { visittkortEtiketter(fnr: "${TEST_FNR.get()}", behandlingsnummer: "B643") { $felter } }
    """.trimIndent()
}
