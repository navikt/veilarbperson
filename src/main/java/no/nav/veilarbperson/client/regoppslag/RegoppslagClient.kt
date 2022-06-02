package no.nav.veilarbperson.client.regoppslag

import no.nav.common.health.HealthCheck
import no.nav.common.types.identer.Fnr

interface RegoppslagClient : HealthCheck {
    fun hentPostadresse(fnr: Fnr): RegoppslagResponseDTO
}

data class RegoppslagRequestDTO(val ident: String, val tema: String)

data class RegoppslagResponseDTO(
    val navn: String,
    val adresse: Adresse
) {
    data class Adresse(
        val type: AdresseType,
        val adresselinje1: String,
        val adresselinje2: String?,
        val adresselinje3: String?,
        val postnummer: String?,
        val poststed: String?,
        val landkode: String,
        val land: String
    )

    enum class AdresseType {
        NORSKPOSTADRESSE, UTENLANDSKPOSTADRESSE
    }
}
