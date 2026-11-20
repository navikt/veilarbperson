package no.nav.veilarbperson.client

import no.nav.common.json.JsonUtils
import no.nav.veilarbperson.domain.visittkort.OppfolgingEtikettgrunnlag
import no.nav.veilarbperson.domain.visittkort.UtmeldingskandidatEtikett
import no.nav.veilarbperson.utils.TestData.TEST_FNR
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class VeilarboppfolgingEtiketterClientTest : VeilarboppfolgingEtiketterClientTestBase() {

    @Test
    fun `alle felter gir ett GraphQL kall og full mapping`() {
        val client = client(200 to """
            {"data":{
              "brukerStatus":{"erKontorsperret":true,"manuell":{"erManuell":false},
                "krr":{"kanVarsles":false,"reservertIKrr":true,"registrertIKrr":false},
                "sykmeldtStatus":"FREMTIDIG_VERDI"},
              "oppfolging":{"erUnderOppfolging":true},
              "utmeldingskandidat":{"tag":"FORLENGELSE_UTLOPT"}}}
        """.trimIndent())

        val resultat = client.hentEtikettgrunnlag(TEST_FNR, VeilarboppfolgingEtiketterClientTestBase.ALLE_FELTER)

        assertThat(resultat).isEqualTo(
            OppfolgingEtikettgrunnlag(
                underOppfolging = true,
                manuell = false,
                reservasjonKrr = true,
                registrertKrr = false,
                kanVarsles = false,
                sykmeldt = true,
                utmeldingskandidat = UtmeldingskandidatEtikett.FORLENGELSE_UTLOPT,
                kvp = true
            )
        )
        val request = requests.single()
        assertThat(request.method).isEqualTo("POST")
        assertThat(request.url.encodedPath).isEqualTo("/veilarboppfolging/api/graphql")
        assertThat(request.url.query).isNull()
        assertThat(request.header("Authorization")).isEqualTo("Bearer test-token")
        assertThat(request.header("behandlingsnummer")).isNull()
        assertThat(variabler()).isEqualTo(
            forventetVariabler(
                "hentBrukerStatus", "hentKvp", "hentManuell", "hentKrr", "hentKanVarsles",
                "hentReservertKrr", "hentRegistrertKrr", "hentSykmeldt", "hentOppfolging", "hentUtmeldingskandidat"
            )
        )
        val query = JsonUtils.getMapper().readTree(request.bodyTekst()).path("query").stringValue()
        assertThat(query).contains("@include(if: \$hentKvp)", "erKontorsperret", "sykmeldtStatus", "tag")
            .doesNotContain(TEST_FNR.get())
    }

    @Test
    fun `minimale utvalg setter bare noedvendige flagg og lar andre verdier vaere null`() {
        data class Tilfelle(
            val felt: String,
            val flagg: List<String>,
            val data: String,
            val forventet: OppfolgingEtikettgrunnlag
        )
        val tilfeller = listOf(
            Tilfelle(
                "kvp", listOf("hentBrukerStatus", "hentKvp"),
                """{"brukerStatus":{"erKontorsperret":false}}""",
                OppfolgingEtikettgrunnlag(kvp = false)
            ),
            Tilfelle(
                "manuellOppfolging", listOf("hentBrukerStatus", "hentManuell"),
                """{"brukerStatus":{"manuell":{"erManuell":true}}}""",
                OppfolgingEtikettgrunnlag(manuell = true)
            ),
            Tilfelle(
                "reservertKrr", listOf("hentBrukerStatus", "hentKrr", "hentReservertKrr"),
                """{"brukerStatus":{"krr":{"reservertIKrr":false}}}""",
                OppfolgingEtikettgrunnlag(reservasjonKrr = false)
            ),
            Tilfelle(
                "ikkeRegistrertKrr", listOf("hentBrukerStatus", "hentKrr", "hentRegistrertKrr"),
                """{"brukerStatus":{"krr":{"registrertIKrr":false}}}""",
                OppfolgingEtikettgrunnlag(registrertKrr = false)
            ),
            Tilfelle(
                "utdatertIKrr",
                listOf(
                    "hentBrukerStatus", "hentManuell", "hentKrr", "hentKanVarsles",
                    "hentReservertKrr", "hentRegistrertKrr"
                ),
                """{"brukerStatus":{"manuell":{"erManuell":false},
                    "krr":{"kanVarsles":true,"reservertIKrr":false,"registrertIKrr":true}}}""",
                OppfolgingEtikettgrunnlag(
                    manuell = false, reservasjonKrr = false, registrertKrr = true, kanVarsles = true
                )
            ),
            Tilfelle(
                "sykmeldt", listOf("hentBrukerStatus", "hentSykmeldt"),
                """{"brukerStatus":{"sykmeldtStatus":"SYKMELDT"}}""",
                OppfolgingEtikettgrunnlag(sykmeldt = true)
            ),
            Tilfelle(
                "utmeldingskandidat", listOf("hentUtmeldingskandidat"),
                """{"utmeldingskandidat":{"tag":"AVSLUTTET_AAP_SAK"}}""",
                OppfolgingEtikettgrunnlag(utmeldingskandidat = UtmeldingskandidatEtikett.AVSLUTTET_AAP_SAK)
            ),
            Tilfelle(
                "ikkeUnderOppfolging", listOf("hentOppfolging"),
                """{"oppfolging":{"erUnderOppfolging":false}}""",
                OppfolgingEtikettgrunnlag(underOppfolging = false)
            ),
            Tilfelle(
                "trengerOppfolgingsvedtak", listOf("hentOppfolging"),
                """{"oppfolging":{"erUnderOppfolging":true}}""",
                OppfolgingEtikettgrunnlag(underOppfolging = true)
            ),
            Tilfelle(
                "antattGodeMuligheter", listOf("hentOppfolging"),
                """{"oppfolging":{"erUnderOppfolging":true}}""",
                OppfolgingEtikettgrunnlag(underOppfolging = true)
            ),
            Tilfelle(
                "antattBehovForVeiledning", listOf("hentOppfolging"),
                """{"oppfolging":{"erUnderOppfolging":false}}""",
                OppfolgingEtikettgrunnlag(underOppfolging = false)
            ),
            Tilfelle(
                "oppgittHindringer", listOf("hentOppfolging"),
                """{"oppfolging":{"erUnderOppfolging":null}}""",
                OppfolgingEtikettgrunnlag(underOppfolging = null)
            )
        )

        for (tilfelle in tilfeller) {
            requests.clear()
            assertThat(hent(setOf(tilfelle.felt), tilfelle.data)).`as`(tilfelle.felt).isEqualTo(tilfelle.forventet)
            assertThat(requests).`as`(tilfelle.felt).hasSize(1)
            assertThat(variabler()).`as`(tilfelle.felt).isEqualTo(forventetVariabler(*tilfelle.flagg.toTypedArray()))
        }
    }

    @Test
    fun `uetterspurte felter parses ikke og kreves ikke`() {
        val resultat = hent(
            setOf("kvp"),
            """{"brukerStatus":{"erKontorsperret":true,"manuell":null,"krr":"feil","sykmeldtStatus":7},
                "oppfolging":"feil","utmeldingskandidat":{"tag":"UKJENT_UTMELDINGSKANDIDAT"}}"""
        )

        assertThat(resultat).isEqualTo(OppfolgingEtikettgrunnlag(kvp = true))
    }

    @Test
    fun `flere felter med overlappende avhengigheter gir ett kall og delt manuell-flagg`() {
        val resultat = hent(
            setOf("manuellOppfolging", "utdatertIKrr", "kvp"),
            """{"brukerStatus":{"erKontorsperret":false,"manuell":{"erManuell":true},
                "krr":{"kanVarsles":false,"reservertIKrr":false,"registrertIKrr":true}}}"""
        )

        assertThat(resultat).isEqualTo(
            OppfolgingEtikettgrunnlag(
                manuell = true, reservasjonKrr = false, registrertKrr = true, kanVarsles = false, kvp = false
            )
        )
        assertThat(requests).hasSize(1)
        assertThat(variabler()).isEqualTo(
            forventetVariabler(
                "hentBrukerStatus", "hentKvp", "hentManuell", "hentKrr", "hentKanVarsles",
                "hentReservertKrr", "hentRegistrertKrr"
            )
        )
    }

    @Test
    fun `sant falsk og null er gyldige verdier der kontrakten tillater det`() {
        for (verdi in listOf(true, false)) {
            assertThat(hent(setOf("kvp"), """{"brukerStatus":{"erKontorsperret":$verdi}}""").kvp).isEqualTo(verdi)
            assertThat(hent(setOf("ikkeUnderOppfolging"), """{"oppfolging":{"erUnderOppfolging":$verdi}}""")
                .underOppfolging).isEqualTo(verdi)
        }
        assertThat(hent(setOf("sykmeldt"), """{"brukerStatus":{"sykmeldtStatus":null}}""").sykmeldt).isFalse()
        assertThat(hent(setOf("sykmeldt"), """{"brukerStatus":{"sykmeldtStatus":"NY_STATUS"}}""").sykmeldt).isTrue()
        assertThat(hent(setOf("utmeldingskandidat"), """{"utmeldingskandidat":{"tag":null}}""").utmeldingskandidat)
            .isNull()
    }

    @Test
    fun `profilfelter krever objekt og felt men ikke true`() {
        for (felt in listOf("antattGodeMuligheter", "antattBehovForVeiledning", "oppgittHindringer")) {
            assertThat(hent(setOf(felt), """{"oppfolging":{"erUnderOppfolging":false}}""").underOppfolging).isFalse()
            assertThat(hent(setOf(felt), """{"oppfolging":{"erUnderOppfolging":null}}""").underOppfolging).isNull()
            for (data in listOf(
                "{}", """{"oppfolging":null}""", """{"oppfolging":{}}""", """{"oppfolging":[]}""",
                """{"oppfolging":{"erUnderOppfolging":"true"}}"""
            )) {
                assertThrows<VisittkortKildeKontraktException>(felt + data) { hent(setOf(felt), data) }
            }
        }
    }

    @Test
    fun `profilfelt sammen med etikett som krever oppfolgingsstatus godtar ikke null`() {
        val felter = setOf("oppgittHindringer", "ikkeUnderOppfolging")

        assertThat(hent(felter, """{"oppfolging":{"erUnderOppfolging":false}}""").underOppfolging).isFalse()
        assertThrows<VisittkortKildeKontraktException> {
            hent(felter, """{"oppfolging":{"erUnderOppfolging":null}}""")
        }
        assertThat(requests).hasSize(2)
        assertThat(requests.map { variabler(it)["hentOppfolging"] }).containsOnly(true)
    }

    @Test
    fun `alle ni gyldige utmeldingskandidat tagger mappes`() {
        val gyldige = UtmeldingskandidatEtikett.entries.filterNot { it.erUkjent }
        assertThat(gyldige).hasSize(9)
        assertThat(gyldige).contains(
            UtmeldingskandidatEtikett.FOLKEREGISTERSTATUS_UKJENT,
            UtmeldingskandidatEtikett.FOLKEREGISTERSTATUS_INGEN_STATUS
        )

        for (etikett in gyldige) {
            val resultat = hent(setOf("utmeldingskandidat"), """{"utmeldingskandidat":{"tag":"${etikett.name}"}}""")
            assertThat(resultat.utmeldingskandidat).isEqualTo(etikett)
        }
    }

    @Test
    fun `utilgjengelighetsmarkoer og ukjente tagger avvises`() {
        for (tag in listOf(
            "\"UKJENT_UTMELDINGSKANDIDAT\"", "\"NY_TAG\"", "\"forlengelse_utlopt\"", "\"\"", "1", "true", "[]", "{}"
        )) {
            assertThrows<VisittkortKildeKontraktException>(tag) {
                hent(setOf("utmeldingskandidat"), """{"utmeldingskandidat":{"tag":$tag}}""")
            }
        }
    }
}
