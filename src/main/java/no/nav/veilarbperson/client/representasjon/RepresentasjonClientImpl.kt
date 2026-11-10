package no.nav.veilarbperson.client.representasjon

import no.nav.common.rest.client.RestClient
import no.nav.common.rest.client.RestUtils
import no.nav.common.types.identer.Fnr
import no.nav.common.utils.UrlUtils.joinPaths
import no.nav.veilarbperson.client.VisittkortKildeHttpClient
import no.nav.veilarbperson.client.VisittkortKildeKontraktException
import no.nav.veilarbperson.client.kildeFelt
import no.nav.veilarbperson.client.kildeListe
import no.nav.veilarbperson.client.kildeObjekt
import no.nav.veilarbperson.client.kildeTekst
import no.nav.veilarbperson.utils.RestClientUtils.createBearerToken
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import tools.jackson.core.JacksonException
import tools.jackson.databind.JsonNode
import java.io.IOException
import java.util.function.Supplier

open class RepresentasjonClientImpl internal constructor(
    private val reprUrl: String,
    private val userTokenProvider: Supplier<String>,
    private val client: OkHttpClient
) : RepresentasjonClient {
    constructor(reprUrl: String, userTokenProvider: Supplier<String>) :
        this(reprUrl, userTokenProvider, RestClient.baseClient())

    override fun hentOppfolgingsrepresentasjon(fnr: Fnr): VisittkortRepresentasjonData {
        val request = Request.Builder()
            .url(joinPaths(reprUrl, "/api/v2/internbruker/kan-representeres-av"))
            .header(ACCEPT, APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, createBearerToken(userTokenProvider.get()))
            .post(RestUtils.toJsonRequestBody(PersonIdentDTO(fnr.get())))
            .build()
        val data = VisittkortKildeHttpClient(client).hent(request)?.kildeObjekt()
            ?: throw VisittkortKildeKontraktException()
        return VisittkortRepresentasjonData(
            data.kildeFelt("vergemaal").harOppfolging(),
            data.kildeFelt("fullmakt").harOppfolging()
        )
    }

    private fun JsonNode.harOppfolging(): Boolean {
        val rettigheter = kildeListe().map { representasjon ->
            val objekt = representasjon.kildeObjekt()
            objekt.kildeFelt("leserettigheter").kildeListe().map { it.kildeTekst() } +
                objekt.kildeFelt("skriverettigheter").kildeListe().map { it.kildeTekst() }
        }
        return rettigheter.any { "OPP" in it }
    }

    @Throws(IOException::class)
    override fun hentFullmakt(personIdent: String?): List<ReprFullmaktV2Data> {
        val request = Request.Builder()
            .url(joinPaths(reprUrl, "/api/v2/internbruker/fullmakt/bruker-som-fullmaktsgiver/alle-fullmakter"))
            .header(ACCEPT, APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, createBearerToken(userTokenProvider.get()))
            .post(RestUtils.toJsonRequestBody(PersonIdentDTO(personIdent)))
            .build()

        client.newCall(request).execute().use { response ->
            RestUtils.throwIfNotSuccessful(response)
            val fullmakter = try {
                RestUtils.parseJsonArrayResponse(response, ReprFullmaktV2Data::class.java)
                    .orElseThrow { IllegalStateException("Mangler body i responsen fra representasjon") }
            } catch (_: JacksonException) {
                // Parserfeil kan inneholde persondata, og skal ikke videreføres som årsak.
                throw IllegalStateException("Ugyldig fullmaktsrespons fra representasjon")
            }
            if (fullmakter.any { fullmakt -> fullmakt == null }) {
                throw IllegalStateException("Ugyldig fullmaktsrespons fra representasjon")
            }
            return fullmakter
        }
    }
}
