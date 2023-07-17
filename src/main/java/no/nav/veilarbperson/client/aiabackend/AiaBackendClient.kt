package no.nav.veilarbperson.client.aiabackend

import no.nav.common.health.HealthCheck
import okhttp3.Response

interface AiaBackendClient : HealthCheck {
    fun hentEndringIRegistreringsdata(endringIRegistreringsdataRequestDTO: EndringIRegistreringsdataRequestDTO) : Response
}
