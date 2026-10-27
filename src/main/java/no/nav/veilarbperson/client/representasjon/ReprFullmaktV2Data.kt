package no.nav.veilarbperson.client.representasjon

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.common.types.identer.Fnr
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
open class ReprFullmaktV2Data @JsonCreator constructor(
    @JsonProperty(value = "fullmaktsgiver", required = true) fullmaktsgiver: Fnr?,
    @JsonProperty(value = "fullmektig", required = true) fullmektig: Fnr?,
    @JsonProperty(value = "gyldigFraOgMed", required = true) gyldigFraOgMed: LocalDate?,
    @JsonProperty(value = "gyldigTilOgMed", required = true) val gyldigTilOgMed: LocalDate?,
    @JsonProperty(value = "leserettigheter", required = true) leserettigheter: List<String?>?,
    @JsonProperty(value = "skriverettigheter", required = true) skriverettigheter: List<String?>?
) {
    val fullmaktsgiver: Fnr = fullmaktsgiver ?: throw NullPointerException("Mangler fullmaktsgiver")
    val fullmektig: Fnr = fullmektig ?: throw NullPointerException("Mangler fullmektig")

    init {
        if (erBlank(this.fullmaktsgiver.get()) || erBlank(this.fullmektig.get())) {
            throw IllegalArgumentException("Tom personident i representasjon")
        }
    }

    val gyldigFraOgMed: LocalDate = gyldigFraOgMed ?: throw NullPointerException("Mangler startdato")
    val leserettigheter: List<String> = validerRettigheter(leserettigheter)
    val skriverettigheter: List<String> = validerRettigheter(skriverettigheter)
}

private fun validerRettigheter(rettigheter: List<String?>?): List<String> {
    if (rettigheter == null) {
        throw IllegalArgumentException("Manglende eller ugyldige rettigheter i representasjon")
    }
    return java.util.List.copyOf(rettigheter.map { tema ->
        if (tema == null || erBlank(tema)) {
            throw IllegalArgumentException("Manglende eller ugyldige rettigheter i representasjon")
        }
        tema
    })
}

// Beholder Java String.isBlank; Kotlin isBlank regner også blant annet hardt mellomrom som blankt.
private fun erBlank(verdi: String): Boolean = verdi.codePoints().allMatch(Character::isWhitespace)
