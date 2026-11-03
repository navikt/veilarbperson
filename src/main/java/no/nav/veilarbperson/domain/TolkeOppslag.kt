package no.nav.veilarbperson.domain

import no.nav.veilarbperson.client.pdl.HentPerson

sealed interface TolkeOppslag {
    data class Hentet(
        val data: HentPerson.HentSpraakTolk
    ) : TolkeOppslag

    data object Ukjent : TolkeOppslag
}
