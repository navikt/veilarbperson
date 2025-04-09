package no.nav.veilarbperson.client.digdir

import lombok.SneakyThrows
import no.nav.common.health.HealthCheckResult
import no.nav.common.health.HealthCheckUtils
import no.nav.common.rest.client.RestClient
import no.nav.common.rest.client.RestUtils
import no.nav.common.utils.UrlUtils
import no.nav.veilarbperson.config.CacheConfig
import no.nav.veilarbperson.utils.toJson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.util.function.Supplier

open class DigdirClientImpl(
    private val digdirUrl: String,
    private val systemUserTokenProvider: Supplier<String>
) : DigdirClient {

    val client: OkHttpClient = RestClient.baseClient()

    @Cacheable(CacheConfig.DIGDIR_KONTAKTINFO_CACHE_NAME)
    @SneakyThrows
    override fun hentKontaktInfo(personidenter: PostPersonerRequest): PostPersonerResponse? {
        val requestBody = personidenter.toJson()

        val request = Request.Builder()
            .url(UrlUtils.joinPaths(digdirUrl, "/rest/v1/personer?inkluderSikkerDigitalPost=false"))
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + systemUserTokenProvider.get())
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            RestUtils.throwIfNotSuccessful(response)
            return RestUtils.parseJsonResponse(response, PostPersonerResponse::class.java)
                .orElseThrow { IllegalStateException("Digdir body is missing") }
        }
    }

    override fun checkHealth(): HealthCheckResult {
        val request = Request.Builder()
            .url(UrlUtils.joinPaths(digdirUrl, "/rest/ping"))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + systemUserTokenProvider.get())
            .build()

        return HealthCheckUtils.pingUrl(request, client)
    }
}
