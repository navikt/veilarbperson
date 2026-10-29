package no.nav.veilarbperson.client.representasjon

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.common.types.identer.Fnr

@JsonIgnoreProperties(ignoreUnknown = true)
class ReprKanRepresenteresAvData @JsonCreator constructor(
    val fullmakt: List<Fullmakt>,
    val vergemaal: List<Vergemaal>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Fullmakt(
    val fullmaktsgiver: Fnr,
    val fullmektig: Fnr,
    val leserettigheter: List<String>,
    val skriverettigheter: List<String>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Vergemaal(
    val vergehaver: Fnr,
    val verge: Fnr,
    val leserettigheter: List<String>,
    val skriverettigheter: List<String>
)