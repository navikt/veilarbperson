package no.nav.veilarbperson.client

import graphql.ExecutionInput
import graphql.GraphQL
import no.nav.common.json.JsonUtils
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.ProfilertTil
import no.nav.veilarbperson.client.pdl.domain.Diskresjonskode
import no.nav.veilarbperson.client.pdl.domain.PdlRequest
import no.nav.veilarbperson.client.veilarbdialog.VisittkortVarselClientImpl
import no.nav.veilarbperson.client.veilarbvedtaksstotte.VisittkortVedtakClientImpl
import no.nav.veilarbperson.utils.TestData.TEST_FNR
import okhttp3.Request
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.server.ResponseStatusException
import tools.jackson.core.type.TypeReference
import java.io.IOException

class VisittkortKildeClientTest : VisittkortKildeClientTestBase() {

    @Test
    fun `PDL bruker OBO og behandlingsnummer og bare etterspurte felt`() {
        val client = pdl(200 to """{"data":{"hentPerson":{"doedsfall":[]}}}""")

        assertThat(client.hentVisittkortEtiketter(PdlRequest(TEST_FNR, "B643"), setOf("dod")).dod).isFalse()

        val request = requests.single()
        assertThat(request.url.encodedPath).isEqualTo("/graphql")
        assertThat(request.url.query).isNull()
        assertThat(request.header("Authorization")).isEqualTo("Bearer test-token")
        assertThat(request.header("behandlingsnummer")).isEqualTo("B643")
        val body = JsonUtils.getMapper().readTree(request.bodyTekst())
        assertThat(body.path("variables").path("ident").asText()).isEqualTo(TEST_FNR.get())
        assertThat(body.path("variables").path("hentDod").booleanValue()).isTrue()
        assertThat(body.path("variables").path("hentDiskresjonskode").booleanValue()).isFalse()
        assertThat(body.path("variables").path("hentTolk").booleanValue()).isFalse()
        assertThat(body.path("query").asText()).doesNotContain("telefon", "navn")
    }

    @Test
    fun `PDL leser alle etikettopplysninger uten aa eksponere resten av personen`() {
        val client = pdl(200 to """
            {"data":{"hentPerson":{
              "doedsfall":[{"doedsdato":"2020-01-01"}],
              "adressebeskyttelse":[{"gradering":"STRENGT_FORTROLIG_UTLAND"}],
              "sikkerhetstiltak":[{"beskrivelse":"Kildebeskrivelse"}],
              "tilrettelagtKommunikasjon":[{"tegnspraaktolk":{"spraak":"NO"},"talespraaktolk":{"spraak":"EN"}}]
            }}}
        """.trimIndent())

        val resultat = client.hentVisittkortEtiketter(PdlRequest(TEST_FNR, "B643"),
            setOf("dod", "diskresjonskode", "sikkerhetstiltak", "tegnspraaktolk", "talespraaktolk"))
        assertThat(resultat.dod).isTrue()
        assertThat(resultat.diskresjonskode).isEqualTo(Diskresjonskode.STRENGT_FORTROLIG_UTLAND)
        assertThat(resultat.sikkerhetstiltak).isEqualTo("Kildebeskrivelse")
        assertThat(resultat.tegnspraaktolk).isTrue()
        assertThat(resultat.talespraaktolk).isTrue()
    }

    @Test
    fun `PDL errors og ugyldige svar er ikke utilgjengelig kilde`() {
        for (body in listOf(
            """{"errors":[{"message":"sensitive-source-message"}],"data":{"hentPerson":null}}""",
            """{"errors":[]}""",
            """{"data":{"hentPerson":{}}}""",
            """{"data":{"hentPerson":{"doedsfall":null}}}""",
            """{"data":{"hentPerson":{"doedsfall":[{"doedsdato":"sensitive-value"}]}}}""",
            "sensitive-invalid-json",
            "null"
        )) {
            val client = pdl(200 to body)
            val feil = assertThrows<VisittkortKildeKontraktException> {
                client.hentVisittkortEtiketter(PdlRequest(TEST_FNR, "B643"), setOf("dod"))
            }
            assertThat(feil.toString()).doesNotContain("sensitive")
            assertThat(feil.cause).isNull()
        }
    }

    @Test
    fun `PDL tilgangsfeil i en senere feilpost stopper oppslaget`() {
        val client = pdl(200 to """
            {"errors":[{"message":{}},{"message":"Ikke tilgang til å se person"}]}
        """.trimIndent())
        assertThat(assertThrows<ResponseStatusException> {
            client.hentVisittkortEtiketter(PdlRequest(TEST_FNR, "B643"), setOf("dod"))
        }.statusCode.value()).isEqualTo(403)
    }

    @Test
    fun `PDL gjenbruker diskresjonskoder og avviser ukjent gradering`() {
        for (kode in Diskresjonskode.values()) {
            val resultat = pdl(200 to """{"data":{"hentPerson":{"adressebeskyttelse":[{"gradering":"${kode.kode}"}]}}}""")
                .hentVisittkortEtiketter(PdlRequest(TEST_FNR, "B643"), setOf("diskresjonskode"))
            assertThat(resultat.diskresjonskode).isEqualTo(kode)
        }
        val feil = assertThrows<VisittkortKildeKontraktException> {
            pdl(200 to """{"data":{"hentPerson":{"adressebeskyttelse":[{"gradering":"sensitive-invalid"}]}}}""")
                .hentVisittkortEtiketter(PdlRequest(TEST_FNR, "B643"), setOf("diskresjonskode"))
        }
        assertThat(feil.toString()).doesNotContain("sensitive")
        assertThat(feil.cause).isNull()
    }

    @Test
    fun `representasjon filtrerer baade vergemaal og fullmakt paa OPP`() {
        for (rettighet in listOf("AAP", "DAG", "OPP")) {
            val client = representasjon(200 to """
                {"fullmakt":[{"leserettigheter":[],"skriverettigheter":["$rettighet"]}],
                 "vergemaal":[{"leserettigheter":["$rettighet"],"skriverettigheter":[]}]}
            """.trimIndent())
            val resultat = client.hentOppfolgingsrepresentasjon(TEST_FNR)
            assertThat(resultat.fullmaktOppfolging).isEqualTo(rettighet == "OPP")
            assertThat(resultat.vergemaalOppfolging).isEqualTo(rettighet == "OPP")
        }
        assertThat(requests.first().url.encodedPath).isEqualTo("/api/v2/internbruker/kan-representeres-av")
        assertThat(requests.first().bodyTekst()).isEqualTo("""{"ident":"${TEST_FNR.get()}"}""")
    }

    @Test
    fun `manglende representasjonsrettigheter gir feil og ikke fravaer`() {
        for (body in listOf(
            """{"fullmakt":[],"vergemaal":null}""",
            """{"fullmakt":[{"leserettigheter":["OPP"]}],"vergemaal":[]}""",
            """{"fullmakt":[],"vergemaal":[null]}"""
        )) {
            val client = representasjon(200 to body)
            assertThrows<VisittkortKildeKontraktException> { client.hentOppfolgingsrepresentasjon(TEST_FNR) }
        }
    }

    @Test
    fun `skjerming krever boolean og sender personident i body`() {
        val client = skjerming(200 to "true")
        assertThat(client.hentSkjermet(TEST_FNR)).isTrue()
        assertThat(requests.single().bodyTekst()).isEqualTo("""{"personident":"${TEST_FNR.get()}"}""")
        assertThat(requests.single().url.query).isNull()
        for (body in listOf("null", "\"false\"", "0", "{}", "")) {
            assertThrows<VisittkortKildeKontraktException> {
                skjerming(200 to body).hentSkjermet(TEST_FNR)
            }
        }
    }

    @Test
    fun `gjenbrukte klienter skiller ekte HTTP 5xx fra tilgangsfeil uten feilbody`() {
        for (status in listOf(401, 403, 429, 500, 503)) {
            val oppslag: List<() -> Any?> = listOf(
                { pdl(status to "sensitive-body").hentVisittkortEtiketter(PdlRequest(TEST_FNR, "B643"), setOf("dod")) },
                { representasjon(status to "sensitive-body").hentOppfolgingsrepresentasjon(TEST_FNR) },
                { skjerming(status to "sensitive-body").hentSkjermet(TEST_FNR) },
                { arbeidssoker(status to "sensitive-body").hentVisittkortData(TEST_FNR, false) }
            )
            for (hent in oppslag) {
                val feil = if (status >= 500) {
                    assertThrows<VisittkortKildeUtilgjengeligException> { hent() }
                } else {
                    assertThrows<VisittkortKildeHttpException> { hent() }.also {
                        assertThat(it.statusCode.value()).isEqualTo(status)
                    }
                }
                assertThat(feil.toString()).doesNotContain("sensitive-body")
                assertThat(feil.cause).isNull()
            }
        }
    }

    @Test
    fun `ugyldig PAW respons er fatal og eksponerer ikke kildeinnhold`() {
        for (body in listOf("null", "[null]", "{}", "sensitive-invalid-json")) {
            val feil = assertThrows<VisittkortKildeKontraktException> {
                arbeidssoker(200 to body).hentVisittkortData(TEST_FNR, false)
            }
            assertThat(feil.toString()).doesNotContain("sensitive")
            assertThat(feil.cause).isNull()
        }
    }

    @Test
    fun `registeroppslag uten profilering henter bare perioder`() {
        val service = arbeidssoker(200 to PERIODE)

        val resultat = service.hentVisittkortData(TEST_FNR, false)

        assertThat(resultat.registrert).isTrue()
        assertThat(resultat.profilering).isNull()
        assertThat(requests).hasSize(1)
        assertThat(requests.single().header("Nav-Consumer-Id")).isEqualTo("veilarbperson")
    }

    @Test
    fun `ingen periode er gyldig fravaer og utloeser ikke profileringsoppslag`() {
        for ((status, body) in listOf(200 to "[]", 404 to "not found")) {
            val service = arbeidssoker(status to body)
            assertThat(service.hentVisittkortData(TEST_FNR, true).registrert).isFalse()
        }
        assertThat(requests).hasSize(2)
    }

    @Test
    fun `profilering maa tilhoere siste opplysninger`() {
        val service = arbeidssoker(
            200 to PERIODE,
            200 to """[{"opplysningerOmArbeidssoekerId":"$OPPLYSNINGER","periodeId":"$PERIODE_ID",
              "sendtInnAv":$METADATA,"utdanning":null,"helse":null,"annet":null,"jobbsituasjon":[]}]""",
            200 to """[
              {"profileringId":"00000000-0000-0000-0000-000000000004","periodeId":"$PERIODE_ID",
               "opplysningerOmArbeidssoekerId":"00000000-0000-0000-0000-000000000003",
               "sendtInnAv":${METADATA.replace("2026-01", "2026-02")},"profilertTil":"OPPGITT_HINDRINGER"},
              {"profileringId":"00000000-0000-0000-0000-000000000005","periodeId":"$PERIODE_ID",
               "opplysningerOmArbeidssoekerId":"$OPPLYSNINGER",
               "sendtInnAv":$METADATA,"profilertTil":"ANTATT_GODE_MULIGHETER"}]"""
        )

        assertThat(service.hentVisittkortData(TEST_FNR, true).profilering).isEqualTo(ProfilertTil.ANTATT_GODE_MULIGHETER)
        assertThat(requests).hasSize(3)
    }

    @Test
    fun `avsluttet periode og ugyldig periodesvar skilles fra aktiv periode`() {
        val avsluttet = arbeidssoker(
            200 to """[{"periodeId":"$PERIODE_ID","startet":$METADATA,"avsluttet":$METADATA}]""")
        assertThat(avsluttet.hentVisittkortData(TEST_FNR, false).registrert).isFalse()

        for (body in listOf(
            """[{"avsluttet":false}]""",
            """[{"avsluttet":{"tidspunkt":"invalid"}}]""",
            """[{"avsluttet":null,"startet":null}]""",
            """[{"avsluttet":null}]""",
            PERIODE.replace("\"2026-01-01T00:00:00Z\"", "0"),
            PERIODE.replace("\"$PERIODE_ID\"", "123")
        )) {
            val service = arbeidssoker(200 to body)
            assertThrows<VisittkortKildeKontraktException> { service.hentVisittkortData(TEST_FNR, false) }
        }
    }

    @Test
    fun `nullable periodeslutt kan vaere utelatt i gyldig svar`() {
        val service = arbeidssoker(200 to """[{"periodeId":"$PERIODE_ID","startet":$METADATA}]""")

        assertThat(service.hentVisittkortData(TEST_FNR, false).registrert).isTrue()
    }

    @Test
    fun `profilering tolker ikke tall eller tallstrenger som enum ordinal`() {
        for (verdi in listOf("0", "\"0\"")) {
            val service = arbeidssoker(
                200 to PERIODE,
                200 to """[{"opplysningerOmArbeidssoekerId":"$OPPLYSNINGER","periodeId":"$PERIODE_ID",
                  "sendtInnAv":$METADATA,"utdanning":null,"helse":null,"annet":null,"jobbsituasjon":[]}]""",
                200 to """[{"profileringId":"00000000-0000-0000-0000-000000000004","periodeId":"$PERIODE_ID",
                  "opplysningerOmArbeidssoekerId":"$OPPLYSNINGER","sendtInnAv":$METADATA,"profilertTil":$verdi}]"""
            )
            assertThrows<VisittkortKildeKontraktException> { service.hentVisittkortData(TEST_FNR, true) }
        }
    }

    @Test
    fun `eskaleringsvarsel bruker dokumentert GET kontrakt og ignorerer unodvendige opplysninger`() {
        val client = VisittkortVarselClientImpl("http://dialog.test/veilarbdialog", { "test-token" },
            http(200 to """
                {"id":1,"tilhorendeDialogId":2,"opprettetAv":"unused",
                 "opprettetDato":"unused","opprettetBegrunnelse":"unused"}
            """.trimIndent()))

        assertThat(client.harGjeldendeEskaleringsvarsel(TEST_FNR)).isTrue()
        val request = requests.single()
        assertThat(request.method).isEqualTo("GET")
        assertThat(request.url.encodedPath).isEqualTo("/veilarbdialog/api/eskaleringsvarsel/gjeldende")
        assertThat(request.url.queryParameter("fnr")).isEqualTo(TEST_FNR.get())
        assertThat(request.header("behandlingsnummer")).isNull()
        assertThat(request.body).isNull()
    }

}
