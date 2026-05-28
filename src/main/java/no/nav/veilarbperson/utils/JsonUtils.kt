package no.nav.veilarbperson.utils

import no.nav.common.json.JsonUtils
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import no.nav.common.rest.client.RestUtils
import okhttp3.Response

val objectMapper: ObjectMapper = JsonUtils.getMapper()

inline fun <reified T> Response.deserializeJson(): T? {
    return RestUtils.getBodyStr(this)
        .map {
            val result: T = objectMapper.readValue(it)
            result
        }
        .orElse(null)
}

inline fun <reified T> Response.deserializeJsonOrThrow(): T {
    return this.deserializeJson() ?: throw IllegalStateException("Unable to parse JSON object from response body")
}

fun <T> T.toJson(): String {
    return objectMapper.writeValueAsString(this)
}