package no.nav.veilarbperson.client.regoppslag

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.common.rest.client.RestUtils
import okhttp3.Response

object JsonUtils {
    val objectMapper: ObjectMapper =
        no.nav.common.json.JsonUtils.getMapper().registerModule(KotlinModule.Builder().build())

    @JvmStatic
    fun init() {
    }
}

inline fun <reified T> Response.deserializeJson(): T? {
    return RestUtils.getBodyStr(this)
        .map { JsonUtils.objectMapper.readValue(it, T::class.java) }
        .orElse(null)
}

inline fun <reified T> Response.deserializeJsonOrThrow(): T {
    return this.deserializeJson() ?: throw IllegalStateException("Unable to parse JSON object from response body")
}

fun <T> T.toJson(): String {
    return JsonUtils.objectMapper.writeValueAsString(this)
}
