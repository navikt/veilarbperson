package no.nav.veilarbperson.client.pdl

import no.nav.veilarbperson.client.VisittkortKildeKontraktException
import no.nav.veilarbperson.client.kildeFelt
import no.nav.veilarbperson.client.kildeListe
import no.nav.veilarbperson.client.kildeObjekt
import no.nav.veilarbperson.client.kildeTekst
import no.nav.veilarbperson.client.pdl.domain.Diskresjonskode
import no.nav.veilarbperson.client.valgfriKildeTekst
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import tools.jackson.databind.JsonNode
import java.time.LocalDate
import java.time.format.DateTimeParseException

data class PdlVisittkortEtiketterData(
    val dod: Boolean = false,
    val diskresjonskode: Diskresjonskode? = null,
    val sikkerhetstiltak: String? = null,
    val tegnspraaktolk: Boolean = false,
    val talespraaktolk: Boolean = false
) {
    companion object {
        @JvmStatic
        fun fraRespons(respons: JsonNode?, felter: Set<String>): PdlVisittkortEtiketterData {
            val root = respons?.kildeObjekt() ?: throw VisittkortKildeKontraktException()
            val errors = root.get("errors")
            if (errors != null && !errors.isNull) {
                val feil = errors.kildeListe()
                if (feil.isEmpty()) throw VisittkortKildeKontraktException()
                if (feil.any {
                    it.path("extensions").path("code").stringValueOpt().orElse(null) in setOf("unauthorized", "forbidden") ||
                        it.path("message").stringValueOpt().orElse(null) == "Ikke tilgang til å se person"
                }) {
                    throw ResponseStatusException(HttpStatus.FORBIDDEN)
                }
                throw VisittkortKildeKontraktException()
            }
            val data = root.kildeFelt("data").kildeObjekt().kildeFelt("hentPerson")
            if (data.isNull) throw ResponseStatusException(HttpStatus.NOT_FOUND)
            data.kildeObjekt()

            val dod = if ("dod" in felter) {
                data.kildeFelt("doedsfall").kildeListe().firstOrNull()?.kildeObjekt()
                    ?.kildeFelt("doedsdato")?.valgfriKildeTekst()?.let {
                        try {
                            LocalDate.parse(it)
                            true
                        } catch (_: DateTimeParseException) {
                            throw VisittkortKildeKontraktException()
                        }
                    } ?: false
            } else false
            val diskresjonskode = if ("diskresjonskode" in felter) {
                data.kildeFelt("adressebeskyttelse").kildeListe().firstOrNull()?.kildeObjekt()
                    ?.kildeFelt("gradering")?.kildeTekst()?.let { kode ->
                        Diskresjonskode.values().firstOrNull { it.kode == kode }
                            ?: throw VisittkortKildeKontraktException()
                    }
            } else null
            val sikkerhetstiltak = if ("sikkerhetstiltak" in felter) {
                data.kildeFelt("sikkerhetstiltak").kildeListe().firstOrNull()?.kildeObjekt()
                    ?.kildeFelt("beskrivelse")?.valgfriKildeTekst()?.takeIf { it.isNotEmpty() }
            } else null
            val tolk = if (felter.any { it in setOf("tegnspraaktolk", "talespraaktolk") }) {
                data.kildeFelt("tilrettelagtKommunikasjon").kildeListe().firstOrNull()?.kildeObjekt()
            } else null
            return PdlVisittkortEtiketterData(
                dod, diskresjonskode, sikkerhetstiltak,
                "tegnspraaktolk" in felter && tolk.harTolk("tegnspraaktolk"),
                "talespraaktolk" in felter && tolk.harTolk("talespraaktolk")
            )
        }

        private fun JsonNode?.harTolk(felt: String): Boolean {
            if (this == null) return false
            val tolk = kildeFelt(felt)
            if (tolk.isNull) return false
            return tolk.kildeObjekt().kildeFelt("spraak").valgfriKildeTekst()?.isNotEmpty() == true
        }
    }
}
