package no.nav.veilarbperson.client.ensligforsorger

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.common.health.HealthCheck
import no.nav.common.types.identer.Fnr
import java.time.LocalDate

interface EnsligForsorgerClient : HealthCheck {
    fun hentEnsligForsorgerOvergangsstonad(fnr: Fnr): OvergangsstonadResponseDto?
}

data class OvergangsstonadBarn(
    @JsonProperty("personIdent")
    val personIdent: String,
    @JsonProperty("fødselTermindato")
    val fodselTermindato: LocalDate
)

data class OvergangsstonadPeriode(
    @JsonProperty("stønadFraOgMed")
    val stonadFraOgMed: LocalDate,
    @JsonProperty("stønadTilOgMed")
    val stonadTilOgMed: LocalDate,
    @JsonProperty("aktivitet")
    val aktivitet: Aktivitetstype,
    @JsonProperty("periodeType")
    val periodeType: Periodetype,
    @JsonProperty("barn")
    val barn: List<OvergangsstonadBarn>,
    @JsonProperty("behandlingId")
    val behandlingId: Long,
    @JsonProperty("harAktivitetsplikt")
    val harAktivitetsplikt: Boolean
)

data class OvergangsstonadData(
    @JsonProperty("personIdent")
    val personIdent: List<String>,
    @JsonProperty("perioder")
    val perioder: List<OvergangsstonadPeriode>
)


data class OvergangsstonadResponseDto(
    @JsonProperty("data")
    val data: OvergangsstonadData,
    @JsonProperty("status")
    val status: String,
    @JsonProperty("melding")
    val melding: String,
    @JsonProperty("frontendFeilmelding")
    val frontendFeilmelding: String?,
    @JsonProperty("stacktrace")
    val stacktrace: String?,
    @JsonProperty("callId")
    val callId: String?
)

