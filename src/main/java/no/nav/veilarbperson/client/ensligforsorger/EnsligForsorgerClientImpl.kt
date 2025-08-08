package no.nav.veilarbperson.client.ensligforsorger

import no.nav.common.health.HealthCheckResult
import no.nav.common.health.HealthCheckUtils
import no.nav.common.rest.client.RestClient
import no.nav.common.rest.client.RestUtils
import no.nav.common.types.identer.Fnr
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
import java.util.*
import java.util.function.Supplier

class EnsligForsorgerClientImpl(
    private val ensligForsorgerUrl: String,
    private val machineToMachineTokenSupplier: Supplier<String>
) :
    EnsligForsorgerClient {
    private val client: OkHttpClient = RestClient.baseClient()

    private fun buildAuthorizedRequest(url: String): Request.Builder {
        return Request.Builder()
            .url(url)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer ${machineToMachineTokenSupplier.get()}")
            .header("Nav-Call-Id", UUID.randomUUID().toString())
    }

    @Cacheable(CacheConfig.ENSLIGFORSORGER_OVERGANGSSTONAD_CACHE_NAME)
    override fun hentEnsligForsorgerOvergangsstonad(fnr: Fnr): OvergangsstonadResponseDto? {
        val requestBody = fnr.toJson()
        val request = buildAuthorizedRequest(UrlUtils.joinPaths(ensligForsorgerUrl, "/api/ekstern/perioder/perioder-aktivitet"))
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            RestUtils.throwIfNotSuccessful(response)
            return RestUtils.parseJsonResponse(response, OvergangsstonadResponseDto::class.java)
                .orElseThrow { IllegalStateException("Enslig Forsorger body is missing") }
        }
    }

    override fun checkHealth(): HealthCheckResult {
        val request = Request.Builder()
            .url(UrlUtils.joinPaths(ensligForsorgerUrl, "rest/ping"))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + machineToMachineTokenSupplier.get())
            .build()

        return HealthCheckUtils.pingUrl(request, client)
    }
}
