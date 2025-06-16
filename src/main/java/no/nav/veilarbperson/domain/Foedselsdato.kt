package no.nav.veilarbperson.domain

import java.time.LocalDate

data class Foedselsdato(
    val foedselsdato: LocalDate? = null,
    val foedselsaar: Int
)
