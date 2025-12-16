package no.nav.veilarbperson.domain

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.common.types.identer.Fnr

data class PersonFraPdlRequest(

    @field:Schema(description = "Fødselsnummeret til en oppfølgingsbruker", example = "10108000398")
    val fnr: Fnr,

    @field:Schema(
        description = "Behandlingsnummer (valgfritt) for behandlingsgrunnlag i PDL",
        example = "B555",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    val behandlingsnummer: String?
)
