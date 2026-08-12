package no.nav.veilarbperson.service

import no.nav.common.types.identer.Fnr
import no.nav.veilarbperson.client.digdir.DigdirClient
import no.nav.veilarbperson.client.digdir.KRRPostPersonerRequest
import no.nav.veilarbperson.client.digdir.KRRPostPersonerResponse
import no.nav.veilarbperson.client.nom.SkjermetClient
import no.nav.veilarbperson.client.pdl.PdlClient
import no.nav.veilarbperson.client.pdl.domain.PdlRequest
import no.nav.veilarbperson.domain.PersonVisittkortData
import no.nav.veilarbperson.utils.PersonVisittkortDataMapper
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class PersonVisittkortService(
    private val pdlClient: PdlClient,
    private val digdirClient: DigdirClient,
    private val skjermetClient: SkjermetClient? = null
) {

    private val log = LoggerFactory.getLogger(PersonVisittkortService::class.java)

    fun hentVisittkortData(fnr: Fnr, behandlingsnummer: String?): PersonVisittkortData  {
        val person = pdlClient.hentPerson(PdlRequest(fnr, behandlingsnummer))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val erSkjermet = hentSkjermet(fnr)
        val (krrTelefon, krrTelefonOppdatert) = hentKRRTelefon(fnr)

        return PersonVisittkortDataMapper.tilPersonVisittkortData(person, erSkjermet, krrTelefon, krrTelefonOppdatert)
    }
    private fun hentSkjermet(fnr: Fnr): Boolean {
        var erSkjermet = false
        try {
            erSkjermet = skjermetClient!!.hentSkjermet(fnr)
        } catch (e: Exception) {
            log.warn("Kunne ikke hente skjermet, fortsetter som ikke skjermet", e)
        }
        return erSkjermet
    }

    private fun hentKRRTelefon(fnr: Fnr): Pair<String?, String?> {var krrTelefon: String? = null
        var krrTelefonOppdatert: String? = null
        try {
            val krrResponse: KRRPostPersonerResponse? = digdirClient.hentKontaktInfo(
                KRRPostPersonerRequest(setOf(fnr.get()))
            )
            val digdirInfo = if (krrResponse != null)
                krrResponse.personer.get(fnr.get())
            else
                null
            if (digdirInfo != null) {
                krrTelefon = digdirInfo.mobiltelefonnummer
                krrTelefonOppdatert = digdirInfo.mobiltelefonnummerOppdatert
            }
        } catch (e: java.lang.Exception) {
            log.warn("Kunne ikke hente telefon fra KRR, fortsetter uten KRR-telefon", e)
        }
        return Pair(krrTelefon, krrTelefonOppdatert)}
}