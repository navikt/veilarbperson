package no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret

import no.nav.common.health.HealthCheckResult
import no.nav.common.health.HealthCheckUtils
import no.nav.common.rest.client.RestClient
import no.nav.common.rest.client.RestUtils
import no.nav.common.utils.UrlUtils
import no.nav.veilarbperson.client.VisittkortKildeHttpClient
import no.nav.veilarbperson.client.VisittkortKildeKontraktException
import no.nav.veilarbperson.client.kildeFelt
import no.nav.veilarbperson.client.kildeListe
import no.nav.veilarbperson.client.kildeObjekt
import no.nav.veilarbperson.client.kildeTekst
import no.nav.veilarbperson.utils.objectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.http.HttpHeaders
import tools.jackson.core.JacksonException
import tools.jackson.databind.JsonNode
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import java.util.*
import java.util.function.Supplier

const val NAV_CONSUMER_ID = "Nav-Consumer-Id"

data class ArbeidssokerVisittkortData(val registrert: Boolean, val profilering: ProfilertTil? = null)

class OppslagArbeidssoekerregisteretClientImpl @JvmOverloads constructor(
    private val url: String,
    private val machineToMachinetokenSupplier: Supplier<String>,
    private val consumerId: String,
    private val client: OkHttpClient = RestClient.baseClient()
): OppslagArbeidssoekerregisteretClient {

    private val httpClient = VisittkortKildeHttpClient(client)

    override fun hentArbeidssokerPerioder(identitetsnummer: String): List<ArbeidssokerperiodeResponse>? =
        post("/api/v1/veileder/arbeidssoekerperioder", ArbeidssoekerperiodeRequest(identitetsnummer), ::validerPeriode)

    override fun hentOpplysningerOmArbeidssoeker(
        identitetsnummer: String,
        periodeId: UUID
    ): List<OpplysningerOmArbeidssoekerResponse>? =
        post(
            "/api/v1/veileder/opplysninger-om-arbeidssoeker",
            OpplysningerOmArbeidssoekerRequest(identitetsnummer, periodeId),
            ::validerOpplysninger
        )

    override fun hentProfilering(identitetsnummer: String, periodeId: UUID): List<ProfileringResponse>? =
        post("/api/v1/veileder/profilering", ProfileringRequest(identitetsnummer, periodeId), ::validerProfilering)

    override fun checkHealth(): HealthCheckResult {
        return HealthCheckUtils.pingUrl(UrlUtils.joinPaths(url, "/internal/isReady"), client)
    }

    private inline fun <reified T : Any> post(
        path: String,
        body: Any,
        valider: (JsonNode) -> Unit
    ): List<T>? {
        val request: Request = Request.Builder()
            .url(UrlUtils.joinPaths(url, path))
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${machineToMachinetokenSupplier.get()}")
            .header(HttpHeaders.ACCEPT, "application/json")
            .header(NAV_CONSUMER_ID, consumerId)
            .post(RestUtils.toJsonRequestBody(body))
            .build()

        return httpClient.hent(request, setOf(404))?.kildeListe()?.map {
            try {
                valider(it.kildeObjekt())
                objectMapper.treeToValue(it, T::class.java) ?: throw VisittkortKildeKontraktException()
            } catch (_: JacksonException) {
                throw VisittkortKildeKontraktException()
            }
        }
    }
}

private fun validerPeriode(periode: JsonNode) {
    periode.kildeFelt("periodeId").validerUuid()
    periode.kildeFelt("startet").validerTidspunkt()
    periode.get("avsluttet")?.takeUnless { it.isNull }?.validerTidspunkt()
}

// Kotlins null-sjekk dekker ikke elementer i lister og kart, så de valideres før dekoding.
private fun validerOpplysninger(opplysninger: JsonNode) {
    opplysninger.kildeFelt("opplysningerOmArbeidssoekerId").validerUuid()
    opplysninger.kildeFelt("sendtInnAv").validerTidspunkt()
    opplysninger.kildeFelt("jobbsituasjon").kildeListe().forEach { jobbsituasjon ->
        jobbsituasjon.kildeObjekt().kildeFelt("detaljer").kildeObjekt().forEach { it.kildeTekst() }
    }
}

private fun validerProfilering(profilering: JsonNode) {
    profilering.kildeFelt("opplysningerOmArbeidssoekerId").validerUuid()
    profilering.kildeFelt("sendtInnAv").validerTidspunkt()
    val verdi = profilering.kildeFelt("profilertTil").kildeTekst()
    if (ProfilertTil.entries.none { it.name == verdi }) throw VisittkortKildeKontraktException()
}

// Jackson tillater ellers blant annet tall som tidspunkt og enum-ordinaler.
private fun JsonNode.validerTidspunkt() {
    try {
        ZonedDateTime.parse(kildeObjekt().kildeFelt("tidspunkt").kildeTekst())
    } catch (_: DateTimeParseException) {
        throw VisittkortKildeKontraktException()
    }
}

private fun JsonNode.validerUuid() {
    try {
        UUID.fromString(kildeTekst())
    } catch (_: IllegalArgumentException) {
        throw VisittkortKildeKontraktException()
    }
}

// Arbeidssøkerperioder typer
data class ArbeidssoekerperiodeRequest(val identitetsnummer: String)

data class ArbeidssokerperiodeResponse(
    val periodeId: UUID,
    val startet: MetadataResponse,
    val avsluttet: MetadataResponse?
)

// Opplysninger om arbeidssøker typer
data class OpplysningerOmArbeidssoekerRequest(val identitetsnummer: String, val periodeId: UUID)

data class OpplysningerOmArbeidssoekerResponse(
    val opplysningerOmArbeidssoekerId: UUID,
    val periodeId: UUID,
    val sendtInnAv: MetadataResponse,
    val utdanning: UtdanningResponse?,
    val helse: HelseResponse?,
    val annet: AnnetResponse?,
    val jobbsituasjon: List<BeskrivelseMedDetaljerResponse>
)

data class BeskrivelseMedDetaljerResponse(
    val beskrivelse: JobbSituasjonBeskrivelseResponse,
    val detaljer: Map<String, String>
)

enum class JobbSituasjonBeskrivelseResponse {
    UKJENT_VERDI,
    UDEFINERT,
    HAR_SAGT_OPP,
    HAR_BLITT_SAGT_OPP,
    ER_PERMITTERT,
    ALDRI_HATT_JOBB,
    IKKE_VAERT_I_JOBB_SISTE_2_AAR,
    AKKURAT_FULLFORT_UTDANNING,
    VIL_BYTTE_JOBB,
    USIKKER_JOBBSITUASJON,
    MIDLERTIDIG_JOBB,
    DELTIDSJOBB_VIL_MER,
    NY_JOBB,
    KONKURS,
    ANNET
}

data class AnnetResponse(
    val andreForholdHindrerArbeid: JaNeiVetIkke?
)

data class HelseResponse(
    val helsetilstandHindrerArbeid: JaNeiVetIkke
)

data class UtdanningResponse(
    val nus: String,    // NUS = Standard for utdanningsgruppering (https://www.ssb.no/klass/klassifikasjoner/36/)
    val bestaatt: JaNeiVetIkke?,
    val godkjent: JaNeiVetIkke?
)

// Profilering typer
data class ProfileringRequest(val identitetsnummer: String, val periodeId: UUID)

data class ProfileringResponse(
    val profileringId: UUID,
    val periodeId: UUID,
    val opplysningerOmArbeidssoekerId: UUID,
    val sendtInnAv: MetadataResponse,
    val profilertTil: ProfilertTil,
    val jobbetSammenhengendeSeksAvTolvSisteManeder: Boolean?,
    val alder: Int?
)

enum class ProfilertTil {
    UKJENT_VERDI,
    UDEFINERT,
    ANTATT_GODE_MULIGHETER,
    ANTATT_BEHOV_FOR_VEILEDNING,
    OPPGITT_HINDRINGER
}

// Felles typer
enum class JaNeiVetIkke {
    JA, NEI, VET_IKKE
}

data class MetadataResponse(
    val tidspunkt: ZonedDateTime,
    val utfoertAv: BrukerResponse,
    val kilde: String,
    val aarsak: String
)

data class BrukerResponse(
    val type: BrukerType,
    val id: String?
)

enum class BrukerType {
    UKJENT_VERDI, UDEFINERT, VEILEDER, SYSTEM, SLUTTBRUKER
}
