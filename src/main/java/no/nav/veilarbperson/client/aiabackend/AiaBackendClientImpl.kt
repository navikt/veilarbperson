package no.nav.veilarbperson.client.aiabackend

import lombok.SneakyThrows
import no.nav.common.health.HealthCheckResult
import no.nav.common.health.HealthCheckUtils
import no.nav.common.rest.client.RestClient
import no.nav.common.rest.client.RestUtils
import no.nav.common.utils.UrlUtils.joinPaths
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import no.nav.veilarbperson.utils.toJson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.springframework.http.HttpHeaders
import java.util.function.Supplier

class AiaBackendClientImpl(private val aiaBackendUrl: String, private val userTokenSupplier: Supplier<String>) : AiaBackendClient {
    private val client: OkHttpClient = RestClient.baseClient()

    @SneakyThrows
    override fun hentEndringIRegistreringsdata(endringIRegistreringsdataRequestDTO: EndringIRegistreringsdataRequestDTO) : Response {
        val request = Request.Builder()
            .url(joinPaths(aiaBackendUrl, "/veileder/besvarelse"))
            .header(HttpHeaders.AUTHORIZATION, userTokenSupplier.get())
            .header(ACCEPT, APPLICATION_JSON_VALUE)
            .post(endringIRegistreringsdataRequestDTO.toJson().toRequestBody(RestUtils.MEDIA_TYPE_JSON))
            .build()

        return client.newCall(request).execute()
    }

    override fun checkHealth(): HealthCheckResult {
        return HealthCheckUtils.pingUrl(joinPaths(aiaBackendUrl, "/internal/isReady"), client)
    }
}
