package no.nav.veilarbperson.client.digdir

import no.nav.common.health.HealthCheck

interface DigdirClient : HealthCheck {
    fun hentKontaktInfo(personidenter: KRRPostPersonerRequest): KRRPostPersonerResponse?
}

data class KRRPostPersonerRequest(
    val personidenter: Set<String>
)

data class KRRPostPersonerResponse(
    val personer: Map<String, DigdirKontaktinfo>,
    val feil: Map<String, String>?
)

data class DigdirKontaktinfo(
    val personident: String?,
    val aktiv: Boolean,
    val kanVarsles: Boolean,
    val reservasjonOppdatert: String?,
    val reservert: Boolean,
    val spraak: String?,
    val spraakOppdatert: String?,
    val epostadresse: String?,
    val epostadresseOppdatert: String?,
    val epostadresseVerifisert: String?,
    val mobiltelefonnummer: String?,
    val mobiltelefonnummerOppdatert: String?,
    val mobiltelefonnummerVerifisert: String?)




