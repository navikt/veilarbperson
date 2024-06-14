package no.nav.veilarbperson.domain

import no.nav.common.types.identer.Fnr

data class PersonRequest(
    val fnr: Fnr,
    val behandlingsnummer: String?
)
