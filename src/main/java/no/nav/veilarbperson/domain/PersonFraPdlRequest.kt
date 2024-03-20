package no.nav.veilarbperson.domain

import no.nav.common.types.identer.Fnr

data class PersonFraPdlRequest(
    val fnr: Fnr,
    val behandlingsnummer: String?
)
