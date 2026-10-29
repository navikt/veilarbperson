package no.nav.veilarbperson.client.representasjon

import no.nav.common.rest.client.RestClient
import no.nav.common.rest.client.RestUtils
import no.nav.common.utils.UrlUtils.joinPaths
import no.nav.veilarbperson.utils.RestClientUtils.createBearerToken
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import tools.jackson.core.JacksonException
import java.io.IOException
import java.util.function.Supplier

open class RepresentasjonClientImpl internal constructor(
    private val reprUrl: String,
    private val userTokenProvider: Supplier<String>,
    private val client: OkHttpClient
) : RepresentasjonClient {
    constructor(reprUrl: String, userTokenProvider: Supplier<String>) :
        this(reprUrl, userTokenProvider, RestClient.baseClient())

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

    @Throws(IOException::class)
    override fun hentRepresenteresAv(personIdent: String?): ReprKanRepresenteresAvData {
        val request = Request.Builder()
            .url(joinPaths(reprUrl, "/api/v2/internbruker/kan-representeres-av"))
            .header(ACCEPT, APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, createBearerToken(userTokenProvider.get()))
            .post(RestUtils.toJsonRequestBody(PersonIdentDTO(personIdent)))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                TODO(
                    "Kast ResponseStatusException med HTTP-statusen fra response.code " +
                    "og en fast melding uten responsbody"
                )
            }
            val representeresAv = try {
                RestUtils.parseJsonResponse(response, ReprKanRepresenteresAvData::class.java)
                    .orElseThrow { IllegalStateException("Mangler body i responsen fra kan-representeres-av") }
            } catch (_: JacksonException) {
                // Parserfeil kan inneholde persondata, og skal ikke videreføres som årsak.
                throw IllegalStateException("Ugyldig respons fra kan-representeres-av")
            }
            return representeresAv
        }
    }
}
