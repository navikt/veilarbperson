package no.nav.veilarbperson.client.veilarbvedtaksstotte

import no.nav.common.rest.client.RestUtils
import no.nav.common.types.identer.Fnr
import no.nav.common.utils.UrlUtils.joinPaths
import no.nav.veilarbperson.client.VisittkortKildeHttpClient
import no.nav.veilarbperson.client.VisittkortKildeKontraktException
import no.nav.veilarbperson.client.kildeFelt
import no.nav.veilarbperson.client.kildeObjekt
import no.nav.veilarbperson.client.kildeTekst
import okhttp3.Request
import org.springframework.http.HttpHeaders
import java.util.function.Supplier

class VisittkortVedtakClientImpl(
    private val url: String,
    private val tokenSupplier: Supplier<String>,
    private val httpClient: VisittkortKildeHttpClient = VisittkortKildeHttpClient()
) : VisittkortVedtakClient {
    override fun harGjeldendeVedtak(fnr: Fnr): Boolean {
        val request = Request.Builder()
            .url(joinPaths(url, "/api/hent-gjeldende-14a-vedtak"))
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${tokenSupplier.get()}")
            .header(HttpHeaders.ACCEPT, "application/json")
            .post(RestUtils.toJsonRequestBody(mapOf("fnr" to fnr.get())))
            .build()
        val vedtak = httpClient.hent(request, tomBodyErFravaer = true) ?: return false
        if (vedtak.isNull) return false
        if (vedtak.kildeObjekt().kildeFelt("innsatsgruppe").kildeTekst().isBlank()) {
            throw VisittkortKildeKontraktException()
        }
        return true
    }
}
