package no.nav.veilarbperson.controller

import com.nimbusds.jwt.JWTClaimsSet
import no.nav.common.audit_log.cef.CefMessage
import no.nav.common.audit_log.log.AuditLogger
import no.nav.common.auth.context.AuthContextHolder
import no.nav.common.client.aktoroppslag.AktorOppslagClient
import no.nav.common.token_client.client.AzureAdOnBehalfOfTokenClient
import no.nav.poao_tilgang.client.Decision
import no.nav.poao_tilgang.client.NavAnsattTilgangTilEksternBrukerPolicyInput
import no.nav.poao_tilgang.client.PoaoTilgangClient
import no.nav.poao_tilgang.client.api.ApiResult
import no.nav.veilarbperson.config.EnvironmentProperties
import no.nav.veilarbperson.domain.visittkort.VisittkortEtiketter
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
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.Optional

@GraphQlTest(VisittkortEtiketterGraphQLController::class)
@Import(AuthService::class, VisittkortEtiketterExceptionResolver::class)
class VisittkortEtiketterTilgangTest {
    @Autowired
    lateinit var graphQlTester: GraphQlTester

    @MockitoBean
    lateinit var service: VisittkortEtiketterService

    @MockitoBean
    lateinit var authContextHolder: AuthContextHolder

    @MockitoBean
    lateinit var aktorOppslagClient: AktorOppslagClient

    @MockitoBean
    lateinit var tokenClient: AzureAdOnBehalfOfTokenClient

    @MockitoBean
    lateinit var properties: EnvironmentProperties

    @MockitoBean
    lateinit var poaoTilgangClient: PoaoTilgangClient

    @MockitoBean
    lateinit var auditLogger: AuditLogger

    @BeforeEach
    fun internVeileder() {
        val claims = JWTClaimsSet.Builder()
            .claim("NAVident", "Z123456")
            .claim("oid", "00000000-0000-0001-0000-0000000003e8")
            .build()
        whenever(authContextHolder.erInternBruker()).thenReturn(true)
        whenever(authContextHolder.idTokenClaims).thenReturn(Optional.of(claims))
        whenever(authContextHolder.requireIdTokenClaims()).thenReturn(claims)
        whenever(service.hent(any(), anyOrNull(), any())).thenReturn(VisittkortEtiketter())
    }

    @Test
    fun `permit fra eksisterende personpolicy auditeres foer etikettoppslaget`() {
        whenever(poaoTilgangClient.evaluatePolicy(any())).thenReturn(ApiResult(null, Decision.Permit))

        graphQlTester.document(QUERY).execute().path("visittkortEtiketter.skjerming").valueIsNull()

        inOrder(poaoTilgangClient, auditLogger, service) {
            verify(poaoTilgangClient).evaluatePolicy(isA<NavAnsattTilgangTilEksternBrukerPolicyInput>())
            verify(auditLogger).log(any<CefMessage>())
            verify(service).hent(TEST_FNR, null, setOf("skjerming"))
        }
    }

    @Test
    fun `deny fra personpolicy gir avslag og audit uten etikettkilder`() {
        whenever(poaoTilgangClient.evaluatePolicy(any()))
            .thenReturn(ApiResult(null, Decision.Deny("DENIED", "Policy denied access")))

        graphQlTester.document(QUERY).execute().errors().satisfy {
            assertThat(it.single().extensions?.get("code")).isEqualTo("INGEN_TILGANG")
        }.path("visittkortEtiketter").valueIsNull()

        verify(poaoTilgangClient).evaluatePolicy(isA<NavAnsattTilgangTilEksternBrukerPolicyInput>())
        verify(auditLogger).log(any<CefMessage>())
        verifyNoInteractions(service)
    }

    @Test
    fun `uavklart personpolicy gir ikke tilgang eller fallback`() {
        whenever(poaoTilgangClient.evaluatePolicy(any()))
            .thenThrow(IllegalStateException("sensitive-policy-details"))

        graphQlTester.document(QUERY).execute().errors().satisfy {
            assertThat(it.single().extensions?.get("code")).isEqualTo("INTERN_FEIL")
            assertThat(it.toString()).doesNotContain("sensitive-policy-details", TEST_FNR.get())
        }.path("visittkortEtiketter").valueIsNull()

        verifyNoInteractions(auditLogger, service)
    }

    @Test
    fun `ekstern og systembruker avvises av faktisk AuthService foer policyoppslag`() {
        whenever(authContextHolder.erInternBruker()).thenReturn(false)
        for (ekstern in listOf(false, true)) {
            whenever(authContextHolder.erEksternBruker()).thenReturn(ekstern)
            whenever(authContextHolder.erSystemBruker()).thenReturn(!ekstern)

            graphQlTester.document(QUERY).execute().errors().satisfy {
                assertThat(it.single().extensions?.get("code")).isEqualTo("INGEN_TILGANG")
            }.path("visittkortEtiketter").valueIsNull()
        }
        verifyNoInteractions(poaoTilgangClient, auditLogger, service)
    }

    companion object {
        private val QUERY = """
            { visittkortEtiketter(fnr: "${TEST_FNR.get()}") { skjerming } }
        """.trimIndent()
    }
}
