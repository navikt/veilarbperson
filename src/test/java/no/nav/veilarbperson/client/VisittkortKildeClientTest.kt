package no.nav.veilarbperson.client

import graphql.ExecutionInput
import graphql.GraphQL
import no.nav.common.json.JsonUtils
import no.nav.veilarbperson.client.VeilarboppfolgingEtiketterClientTest.Companion.OPPFOLGING_SCHEMA
import no.nav.veilarbperson.client.nom.SkjermetClientImpl
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.OppslagArbeidssoekerregisteretClientImpl
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.ProfilertTil
import no.nav.veilarbperson.client.pdl.PdlClientImpl
import no.nav.veilarbperson.client.pdl.domain.Diskresjonskode
import no.nav.veilarbperson.client.pdl.domain.PdlRequest
import no.nav.veilarbperson.client.representasjon.RepresentasjonClientImpl
import no.nav.veilarbperson.client.veilarbdialog.VisittkortVarselClientImpl
import no.nav.veilarbperson.client.veilarboppfolging.VeilarboppfolgingClientImpl
import no.nav.veilarbperson.client.veilarbvedtaksstotte.VisittkortVedtakClientImpl
import no.nav.veilarbperson.service.AuthService
import no.nav.veilarbperson.service.OppslagArbeidssoekerregisteretService
import no.nav.veilarbperson.utils.TestData.TEST_FNR
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.springframework.web.server.ResponseStatusException
import tools.jackson.core.type.TypeReference
import java.io.IOException

class VisittkortKildeClientTest {
    private val requests = mutableListOf<Request>()

    private fun http(vararg svar: Pair<Int, String>) = VisittkortKildeHttpClient(okhttp(*svar))

    private fun okhttp(vararg svar: Pair<Int, String>): OkHttpClient {
        val iterator = svar.iterator()
        return OkHttpClient.Builder().addInterceptor { chain ->
            requests.add(chain.request())
            val (status, body) = iterator.next()
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                .code(status).message("Test response")
                .body(body.toResponseBody("application/json".toMediaType())).build()
        }.build()
    }

    private fun pdl(vararg svar: Pair<Int, String>) =
        PdlClientImpl("http://pdl.test", mock<AuthService>(), { "test-token" }, { "system-token" }, okhttp(*svar))

    private fun representasjon(vararg svar: Pair<Int, String>) =
        RepresentasjonClientImpl("http://repr.test", { "test-token" }, okhttp(*svar))

    private fun skjerming(vararg svar: Pair<Int, String>) =
        SkjermetClientImpl("http://nom.test", { "test-token" }, okhttp(*svar))

    private fun arbeidssoker(vararg svar: Pair<Int, String>) =
        OppslagArbeidssoekerregisteretService(
            OppslagArbeidssoekerregisteretClientImpl("http://paw.test", { "test-token" }, "veilarbperson", okhttp(*svar))
        )

    private fun kvp(vararg svar: Pair<Int, String>) =
        VeilarboppfolgingClientImpl("http://oppfolging.test/veilarboppfolging", { "test-token" }, okhttp(*svar))

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

    @Test
    fun `eskaleringsvarsel 204 er fravaer men 404 er ikke det`() {
        val utenVarsel = VisittkortVarselClientImpl("http://dialog.test/veilarbdialog", { "test-token" },
            http(204 to ""))
        assertThat(utenVarsel.harGjeldendeEskaleringsvarsel(TEST_FNR)).isFalse()

        val feilRute = VisittkortVarselClientImpl("http://dialog.test/veilarbdialog", { "test-token" },
            http(404 to "sensitive-source-message"))
        assertThat(assertThrows<VisittkortKildeHttpException> {
            feilRute.harGjeldendeEskaleringsvarsel(TEST_FNR)
        }.statusCode.value()).isEqualTo(404)
    }

    @Test
    fun `ugyldig eskaleringsvarsel gir ikke positiv etikett`() {
        for (body in listOf(
            "", "null", "[]", "{}", """{"id":1}""",
            """{"id":null,"tilhorendeDialogId":2}""",
            """{"id":1.5,"tilhorendeDialogId":2}""",
            """{"id":1,"tilhorendeDialogId":"2"}""",
            """{"id":9223372036854775808,"tilhorendeDialogId":2}"""
        )) {
            val client = VisittkortVarselClientImpl("http://dialog.test/veilarbdialog", { "test-token" },
                http(200 to body))
            assertThrows<VisittkortKildeKontraktException> {
                client.harGjeldendeEskaleringsvarsel(TEST_FNR)
            }
        }
    }

    @Test
    fun `ekte HTTP 5xx skilles fra oevrige statuser uten aa inkludere feilbody`() {
        val request = Request.Builder().url("http://source.test").build()
        for (status in listOf(500, 502, 503)) {
            val feil = assertThrows<VisittkortKildeUtilgjengeligException> {
                http(status to "sensitive-body").hent(request)
            }
            assertThat(feil.toString()).doesNotContain("sensitive")
        }
        for (status in listOf(400, 401, 403, 404, 429)) {
            val feil = assertThrows<VisittkortKildeHttpException> { http(status to "sensitive-body").hent(request) }
            assertThat(feil.statusCode.value()).isEqualTo(status)
            assertThat(feil.toString()).doesNotContain("sensitive")
        }
        for (status in listOf(201, 202, 204, 206)) {
            assertThrows<VisittkortKildeKontraktException> {
                http(status to "{}").hent(request)
            }
        }
    }

    @Test
    fun `transportfeil beholdes som transportfeil og parsingsfeil lekker ikke data`() {
        val request = Request.Builder().url("http://source.test").build()
        val transportfeil = IOException("source unavailable")
        val client = VisittkortKildeHttpClient(
            OkHttpClient.Builder().addInterceptor { throw transportfeil }.build()
        )
        assertThat(assertThrows<IOException> { client.hent(request) }).isSameAs(transportfeil)
        val feil = assertThrows<VisittkortKildeKontraktException> {
            http(200 to "sensitive-invalid-json").hent(request)
        }
        assertThat(feil.toString()).doesNotContain("sensitive")
        assertThat(feil.cause).isNull()
    }

    @Test
    fun `KVP bruker eksisterende GraphQL endpoint OBO og bare kontorsperrefeltet`() {
        for (underKvp in listOf(true, false)) {
            val client = kvp(200 to """{"data":{"brukerStatus":{"erKontorsperret":$underKvp}}}""")

            assertThat(client.hentKvpStatus(TEST_FNR)).isEqualTo(underKvp)

            val request = requests.last()
            assertThat(request.method).isEqualTo("POST")
            assertThat(request.url.encodedPath).isEqualTo("/veilarboppfolging/api/graphql")
            assertThat(request.url.query).isNull()
            assertThat(request.header("Authorization")).isEqualTo("Bearer test-token")
            assertThat(request.header("behandlingsnummer")).isNull()
            val body = JsonUtils.getMapper().readTree(request.bodyTekst())
            assertThat(body.path("variables").path("fnr").stringValue()).isEqualTo(TEST_FNR.get())
            assertThat(body.path("query").stringValue()).contains("erKontorsperret", "@include")
                .doesNotContain("veilederIdent", TEST_FNR.get())
            val variables = JsonUtils.getMapper().convertValue(
                body.path("variables"), object : TypeReference<Map<String, Any>>() {}
            )
            val kildeResultat = GraphQL.newGraphQL(OPPFOLGING_SCHEMA).build().execute(
                ExecutionInput.newExecutionInput()
                    .query(body.path("query").stringValue())
                    .variables(variables)
                    .root(mapOf("brukerStatus" to mapOf("erKontorsperret" to underKvp)))
                    .build()
            )
            assertThat(kildeResultat.errors).isEmpty()
            assertThat(kildeResultat.getData<Map<String, Any>>())
                .isEqualTo(mapOf("brukerStatus" to mapOf("erKontorsperret" to underKvp)))
        }
        assertThat(requests).hasSize(2)
    }

    @Test
    fun `KVP tillater tom feilliste med gyldig data men ikke delvis GraphQL feil`() {
        val gyldig = kvp(200 to """{"errors":[],"data":{"brukerStatus":{"erKontorsperret":true}}}""")
        assertThat(gyldig.hentKvpStatus(TEST_FNR)).isTrue()

        val feil = kvp(200 to """
                {"errors":[{"message":"sensitive-source-message"}],
                 "data":{"brukerStatus":{"erKontorsperret":true}}}
            """.trimIndent())
        assertThat(assertThrows<VisittkortKildeKontraktException> { feil.hentKvpStatus(TEST_FNR) }.toString())
            .doesNotContain("sensitive-source-message")
    }

    @Test
    fun `manglende eller ugyldig KVP boolsk verdi gir feil og ikke fravaer`() {
        for (body in listOf(
            "", "null", "{}",
            """{"data":null}""",
            """{"data":{"brukerStatus":null}}""",
            """{"data":{"brukerStatus":{}}}""",
            """{"data":{"brukerStatus":{"erKontorsperret":null}}}""",
            """{"data":{"brukerStatus":{"erKontorsperret":"false"}}}""",
            """{"data":{"brukerStatus":{"erKontorsperret":0}}}""",
            """{"errors":{},"data":{"brukerStatus":{"erKontorsperret":true}}}"""
        )) {
            val client = kvp(200 to body)
            assertThrows<VisittkortKildeKontraktException> { client.hentKvpStatus(TEST_FNR) }
        }
    }

    @Test
    fun `KVP HTTP tilgangsfeil og 404 er ikke gyldig fravaer`() {
        for (status in listOf(401, 403, 404)) {
            val client = kvp(status to "sensitive-source-message")
            val feil = assertThrows<VisittkortKildeHttpException> { client.hentKvpStatus(TEST_FNR) }
            assertThat(feil.statusCode.value()).isEqualTo(status)
            assertThat(feil.toString()).doesNotContain("sensitive-source-message")
        }
        val utilgjengelig = kvp(503 to "source unavailable")
        assertThrows<VisittkortKildeUtilgjengeligException> { utilgjengelig.hentKvpStatus(TEST_FNR) }
    }

    @Test
    fun `vedtak bruker intern rute med fagsystemkontroll og OBO`() {
        val client = VisittkortVedtakClientImpl("http://vedtak.test/veilarbvedtaksstotte", { "test-token" },
            http(200 to """
                {"innsatsgruppe":"GODE_MULIGHETER","hovedmal":"SKAFFE_ARBEID","fattetDato":"2026-01-01"}
            """.trimIndent()))

        assertThat(client.harGjeldendeVedtak(TEST_FNR)).isTrue()
        val request = requests.single()
        assertThat(request.method).isEqualTo("POST")
        assertThat(request.url.encodedPath).isEqualTo("/veilarbvedtaksstotte/api/hent-gjeldende-14a-vedtak")
        assertThat(request.url.encodedPath).doesNotContain("/ekstern/")
        assertThat(request.url.query).isNull()
        assertThat(request.header("Authorization")).isEqualTo("Bearer test-token")
        assertThat(request.header("behandlingsnummer")).isNull()
        assertThat(request.bodyTekst()).isEqualTo("""{"fnr":"${TEST_FNR.get()}"}""")
    }

    @Test
    fun `vedtak HTTP 200 tom body eller JSON null er gyldig fravaer`() {
        for (body in listOf("", "null")) {
            val client = VisittkortVedtakClientImpl("http://vedtak.test/veilarbvedtaksstotte", { "test-token" },
                http(200 to body))
            assertThat(client.harGjeldendeVedtak(TEST_FNR)).isFalse()
        }
    }

    @Test
    fun `vedtakseksistens er uavhengig av innsatsgruppekategori og ubrukte DTO felter`() {
        val client = VisittkortVedtakClientImpl("http://vedtak.test/veilarbvedtaksstotte", { "test-token" },
            http(200 to """{"innsatsgruppe":"EN_NY_KATEGORI","hovedmal":null}"""))

        assertThat(client.harGjeldendeVedtak(TEST_FNR)).isTrue()
    }

    @Test
    fun `ugyldig vedtaksrespons er ikke fravaer`() {
        for (body in listOf(
            "invalid-json", " ", "[]", "true", "\"null\"", "{}",
            """{"innsatsgruppe":null}""",
            """{"innsatsgruppe":123}""",
            """{"innsatsgruppe":""}""",
            """{"innsatsgruppe":" "}"""
        )) {
            val client = VisittkortVedtakClientImpl("http://vedtak.test/veilarbvedtaksstotte", { "test-token" },
                http(200 to body))
            assertThrows<VisittkortKildeKontraktException> { client.harGjeldendeVedtak(TEST_FNR) }
        }
    }

    @Test
    fun `feilede vedtakskall blir ikke tolket som ingen vedtak`() {
        for (status in listOf(401, 403, 404)) {
            val client = VisittkortVedtakClientImpl("http://vedtak.test/veilarbvedtaksstotte", { "test-token" },
                http(status to "sensitive-source-message"))
            val feil = assertThrows<VisittkortKildeHttpException> { client.harGjeldendeVedtak(TEST_FNR) }
            assertThat(feil.statusCode.value()).isEqualTo(status)
            assertThat(feil.toString()).doesNotContain("sensitive-source-message")
        }
        val utilgjengelig = VisittkortVedtakClientImpl("http://vedtak.test/veilarbvedtaksstotte", { "test-token" },
            http(503 to "source unavailable"))
        assertThrows<VisittkortKildeUtilgjengeligException> {
            utilgjengelig.harGjeldendeVedtak(TEST_FNR)
        }
        val uventetFravaersstatus = VisittkortVedtakClientImpl("http://vedtak.test/veilarbvedtaksstotte", { "test-token" },
            http(204 to ""))
        assertThrows<VisittkortKildeKontraktException> {
            uventetFravaersstatus.harGjeldendeVedtak(TEST_FNR)
        }
    }

    @Test
    fun `tom HTTP 200 body tillates bare ved eksplisitt kildekontrakt`() {
        val request = Request.Builder().url("http://source.test").build()
        assertThrows<VisittkortKildeKontraktException> { http(200 to "").hent(request) }
        assertThat(http(200 to "").hent(request, tomBodyErFravaer = true)).isNull()
    }

    private fun Request.bodyTekst(): String {
        val buffer = Buffer()
        requireNotNull(body).writeTo(buffer)
        return buffer.readUtf8()
    }

    companion object {
        private const val PERIODE_ID = "00000000-0000-0000-0000-000000000001"
        private const val OPPLYSNINGER = "00000000-0000-0000-0000-000000000002"
        private const val METADATA = """{"tidspunkt":"2026-01-01T00:00:00Z",
            "utfoertAv":{"type":"SYSTEM","id":null},"kilde":"test","aarsak":"test"}"""
        private val PERIODE = """
            [{"periodeId":"$PERIODE_ID","startet":$METADATA,"avsluttet":null}]
        """.trimIndent()
    }
}
