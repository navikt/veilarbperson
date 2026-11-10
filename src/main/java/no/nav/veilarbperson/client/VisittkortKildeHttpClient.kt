package no.nav.veilarbperson.client

import no.nav.common.json.JsonUtils
import no.nav.common.rest.client.RestClient
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.http.HttpStatusCode
import tools.jackson.core.JacksonException
import tools.jackson.databind.JsonNode

class VisittkortKildeUtilgjengeligException : RuntimeException("Visittkort source unavailable")
class VisittkortKildeKontraktException : RuntimeException("Invalid visittkort source response")
class VisittkortKildeIkkeKonfigurertException : RuntimeException("Visittkort source not configured")
// Only the label resolver maps upstream HTTP statuses; legacy endpoints retain their error responses.
class VisittkortKildeHttpException(val statusCode: HttpStatusCode) : RuntimeException("Visittkort source HTTP error")

class VisittkortKildeHttpClient(
    private val client: OkHttpClient = RestClient.baseClient()
) {
    @JvmOverloads
    fun hent(
        request: Request,
        fravaersstatuser: Set<Int> = emptySet(),
        tomBodyErFravaer: Boolean = false
    ): JsonNode? {
        client.newCall(request).execute().use { response ->
            if (response.code in fravaersstatuser) return null
            if (response.code in 500..599) throw VisittkortKildeUtilgjengeligException()
            if (!response.isSuccessful) {
                throw VisittkortKildeHttpException(HttpStatusCode.valueOf(response.code))
            }
            if (response.code != 200) throw VisittkortKildeKontraktException()
            val body = response.body?.string()
            if (body == null || body.isEmpty()) {
                if (tomBodyErFravaer) return null
                throw VisittkortKildeKontraktException()
            }
            return try {
                JsonUtils.getMapper().readTree(body) ?: throw VisittkortKildeKontraktException()
            } catch (_: JacksonException) {
                throw VisittkortKildeKontraktException()
            }
        }
    }
}

internal fun JsonNode.kildeObjekt(): JsonNode =
    takeIf { it.isObject } ?: throw VisittkortKildeKontraktException()

internal fun JsonNode.kildeListe(): List<JsonNode> {
    if (!isArray) throw VisittkortKildeKontraktException()
    return toList()
}

internal fun JsonNode.kildeTekst(): String =
    takeIf { it.isString }?.stringValue() ?: throw VisittkortKildeKontraktException()

internal fun JsonNode.kildeBoolean(): Boolean {
    if (!isBoolean) throw VisittkortKildeKontraktException()
    return booleanValue()
}

internal fun JsonNode.kildeFelt(navn: String): JsonNode =
    get(navn) ?: throw VisittkortKildeKontraktException()

internal fun JsonNode.valgfriKildeTekst(): String? = if (isNull) null else kildeTekst()
