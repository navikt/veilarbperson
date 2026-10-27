package no.nav.veilarbperson.client.representasjon

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.common.types.identer.Fnr
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
class ReprFullmaktV2Data @JsonCreator constructor(
    val fullmaktsgiver: Fnr,
    val fullmektig: Fnr,
    val gyldigFraOgMed: LocalDate,
    @JsonProperty(value = "gyldigTilOgMed", required = true) val gyldigTilOgMed: LocalDate?,
    val leserettigheter: List<String>,
    val skriverettigheter: List<String>
)
