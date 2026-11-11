package no.nav.veilarbperson.client.veilarbdialog

import no.nav.common.types.identer.Fnr
import no.nav.common.utils.UrlUtils.joinPaths
import no.nav.veilarbperson.client.VisittkortKildeHttpClient
import no.nav.veilarbperson.client.VisittkortKildeKontraktException
import no.nav.veilarbperson.client.kildeFelt
import no.nav.veilarbperson.client.kildeObjekt
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import org.springframework.http.HttpHeaders
import java.util.function.Supplier

class VisittkortVarselClientImpl(
    private val url: String,
    private val tokenSupplier: Supplier<String>,
    private val httpClient: VisittkortKildeHttpClient = VisittkortKildeHttpClient()
) : VisittkortVarselClient {
    override fun harGjeldendeEskaleringsvarsel(fnr: Fnr): Boolean {
        val endpoint = joinPaths(url, "/api/eskaleringsvarsel/gjeldende").toHttpUrl().newBuilder()
            .addQueryParameter("fnr", fnr.get())
            .build()
        val request = Request.Builder()
            .url(endpoint)
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${tokenSupplier.get()}")
            .header(HttpHeaders.ACCEPT, "application/json")
            .get()
            .build()
        val varsel = httpClient.hent(request, setOf(204))?.kildeObjekt() ?: return false
        for (felt in listOf("id", "tilhorendeDialogId")) {
            val verdi = varsel.kildeFelt(felt)
            if (!verdi.isIntegralNumber || !verdi.canConvertToLong()) {
                throw VisittkortKildeKontraktException()
            }
        }
        return true
    }
}
