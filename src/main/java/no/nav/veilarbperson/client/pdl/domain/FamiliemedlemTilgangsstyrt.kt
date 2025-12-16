package no.nav.veilarbperson.client.pdl.domain

import java.time.LocalDate

data class FamiliemedlemTilgangsstyrt(
    val fornavn: String? = null,
    val fodselsdato: LocalDate? = null,
    val erDod: Boolean? = null,
    val alder: Int? = null,
    val erEgenAnsatt: Boolean? = null,
    val harVeilederTilgang: Boolean = false,
    val gradering: String? = null, //diskresjonskode
    val relasjonsBosted: RelasjonsBosted? = null
)
