package no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret

import no.nav.common.health.HealthCheck
import java.util.*

interface OppslagArbeidssoekerregisteretClient: HealthCheck {

    fun hentArbeidssokerPerioder(identitetsnummer: String): List<ArbeidssokerperiodeResponse>?

    fun hentOpplysningerOmArbeidssoeker(
        identitetsnummer: String,
        periodeId: UUID
    ): List<OpplysningerOmArbeidssoekerResponse>?

    fun hentProfilering(identitetsnummer: String, periodeId: UUID): List<ProfileringResponse>?
}