package no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret

import no.nav.common.health.HealthCheckResult
import no.nav.common.health.HealthCheckUtils
import no.nav.common.rest.client.RestClient
import no.nav.common.rest.client.RestUtils
import no.nav.common.utils.UrlUtils
import no.nav.veilarbperson.utils.deserializeJsonOrThrow
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import java.time.ZonedDateTime
import java.util.*
import java.util.function.Supplier

const val NAV_CONSUMER_ID = "Nav-Consumer-Id"

class OppslagArbeidssoekerregisteretClientImpl(
    private val url: String,
    private val machineToMachinetokenSupplier: Supplier<String>,
    private val consumerId: String
): OppslagArbeidssoekerregisteretClient {

    private val client: OkHttpClient = RestClient.baseClient()

    override fun hentArbeidssokerPerioder(identitetsnummer: String): List<ArbeidssokerperiodeResponse>? {
        val request: Request = Request.Builder()
            .url(UrlUtils.joinPaths(url, "/api/v1/veileder/arbeidssoekerperioder"))
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${machineToMachinetokenSupplier.get()}")
            .header(NAV_CONSUMER_ID, consumerId)
            .post(RestUtils.toJsonRequestBody(ArbeidssoekerperiodeRequest(identitetsnummer)))
            .build()

        client.newCall(request).execute().use { response ->
            if (response.code == HttpStatus.NOT_FOUND.value()) {
                return null
            }

            RestUtils.throwIfNotSuccessful(response)

            return response.deserializeJsonOrThrow()
        }
    }

    override fun hentOpplysningerOmArbeidssoeker(
        identitetsnummer: String,
        periodeId: UUID
    ): List<OpplysningerOmArbeidssoekerResponse>? {
        val request: Request = Request.Builder()
            .url(UrlUtils.joinPaths(url, "/api/v1/veileder/opplysninger-om-arbeidssoeker"))
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${machineToMachinetokenSupplier.get()}")
            .header(NAV_CONSUMER_ID, consumerId)
            .post(RestUtils.toJsonRequestBody(OpplysningerOmArbeidssoekerRequest(identitetsnummer, periodeId)))
            .build()

        client.newCall(request).execute().use { response ->
            if (response.code == HttpStatus.NOT_FOUND.value()) {
                return null
            }

            RestUtils.throwIfNotSuccessful(response)

            return response.deserializeJsonOrThrow()
        }
    }

    override fun hentProfilering(identitetsnummer: String, periodeId: UUID): List<ProfileringResponse>? {
        val request: Request = Request.Builder()
            .url(UrlUtils.joinPaths(url, "/api/v1/veileder/profilering"))
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${machineToMachinetokenSupplier.get()}")
            .header(NAV_CONSUMER_ID, consumerId)
            .post(RestUtils.toJsonRequestBody(ProfileringRequest(identitetsnummer, periodeId)))
            .build()

        client.newCall(request).execute().use { response ->
            if (response.code == HttpStatus.NOT_FOUND.value()) {
                return null
            }

            RestUtils.throwIfNotSuccessful(response)

            return response.deserializeJsonOrThrow()
        }
    }

    override fun checkHealth(): HealthCheckResult {
        return HealthCheckUtils.pingUrl(UrlUtils.joinPaths(url, "/internal/isReady"), client)
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
