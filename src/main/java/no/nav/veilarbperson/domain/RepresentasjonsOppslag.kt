package no.nav.veilarbperson.domain

import no.nav.veilarbperson.client.representasjon.ReprKanRepresenteresAvData

sealed interface RepresentasjonsOppslag {
    data class Hentet(
        val data: ReprKanRepresenteresAvData
    ) : RepresentasjonsOppslag

    data object Ukjent : RepresentasjonsOppslag
}