package no.nav.veilarbperson.domain

import no.nav.common.types.identer.Fnr
import java.util.*

data class PdlRequest(
    val fnr: Fnr,
    val behandlingsnummer: String?
)
