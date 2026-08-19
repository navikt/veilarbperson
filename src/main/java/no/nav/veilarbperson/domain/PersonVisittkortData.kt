package no.nav.veilarbperson.domain

import no.nav.veilarbperson.client.pdl.domain.Telefon

data class PersonVisittkortData(
    val fornavn: String? = null,
    val mellomnavn: String? = null,
    val etternavn: String? = null,
    val fodselsdato: String? = null,
    val dodsdato: String? = null,
    val kjonn: String? = null,
    val diskresjonskode: String? = null,
    val egenAnsatt: Boolean = false,
    val sikkerhetstiltak: String? = null,
    val telefon: List<Telefon> = emptyList()
)
