package no.nav.veilarbperson.service

import no.nav.common.types.identer.Fnr
import no.nav.veilarbperson.client.digdir.DigdirClient
import no.nav.veilarbperson.client.digdir.KRRPostPersonerRequest
import no.nav.veilarbperson.client.digdir.KRRPostPersonerResponse
import no.nav.veilarbperson.client.nom.SkjermetClient
import no.nav.veilarbperson.client.pdl.PdlClient
import no.nav.veilarbperson.client.pdl.PdlUtilgjengeligException
import no.nav.veilarbperson.client.pdl.domain.PdlRequest
import no.nav.veilarbperson.client.representasjon.RepresentasjonClient
import no.nav.veilarbperson.domain.PersonVisittkortData
import no.nav.veilarbperson.domain.RepresentasjonsOppslag
import no.nav.veilarbperson.domain.TolkeOppslag
import no.nav.veilarbperson.utils.PersonDataMapper
import no.nav.veilarbperson.utils.PersonVisittkortDataMapper
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.io.IOException
import java.time.ZonedDateTime

@Service
class PersonVisittkortService(
    private val pdlClient: PdlClient,
    private val digdirClient: DigdirClient,
    private val skjermetClient: SkjermetClient,
    private val representasjonsClient: RepresentasjonClient
) {

    private val log = LoggerFactory.getLogger(PersonVisittkortService::class.java)

    fun hentVisittkortData(fnr: Fnr, behandlingsnummer: String?): PersonVisittkortData  {
        val person = pdlClient.hentPersonVisittkort(PdlRequest(fnr, behandlingsnummer))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val erSkjermet = skjermetClient.hentSkjermet(fnr)
        val (krrTelefon, krrTelefonOppdatert) = hentKRRTelefon(fnr)
        val representeresAvData = hentRepresentasjon(fnr)
        val tilrettelagtKommunikasjonData = hentTolk(fnr, behandlingsnummer)

        return PersonVisittkortDataMapper.tilPersonVisittkortData(person, erSkjermet, krrTelefon, krrTelefonOppdatert, representeresAvData,
            tilrettelagtKommunikasjonData
        )
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
                    ?.let { ZonedDateTime.parse(it).format(PersonDataMapper.frontendDatoformat) }
            }
        } catch (e: java.lang.Exception) {
            log.warn("Kunne ikke hente telefon fra KRR, fortsetter uten KRR-telefon", e)
        }
        return Pair(krrTelefon, krrTelefonOppdatert)}

    private fun hentTolk(fnr: Fnr, behandlingsnummer: String?): TolkeOppslag {
        return try {
            TolkeOppslag.Hentet(
                pdlClient.hentTilrettelagtKommunikasjonVisittkort(PdlRequest(fnr, behandlingsnummer))
            )
        } catch (_: IOException) {
            log.warn("Transportfeil ved tolkeoppslag i PDL. Fortsetter med ukjent tolkebehov")
            TolkeOppslag.Ukjent
        } catch (_: PdlUtilgjengeligException) {
            log.warn("Tolkeoppslaget i PDL er utilgjengelig. Fortsetter med ukjent tolkebehov")
            TolkeOppslag.Ukjent
        }
    }

    private fun hentRepresentasjon(fnr: Fnr): RepresentasjonsOppslag {
        return try {
            RepresentasjonsOppslag.Hentet(representasjonsClient.hentRepresenteresAv(fnr.get())
            )
        } catch (_: IOException) {
            TODO("Legg en fast advarsel uten persondata, " +
            "og returner RepresentasjonsOppslag.Ukjent")
        } catch (e: ResponseStatusException) {
            TODO("Ved HTTP 5xx: logg og returner Ukjent." +
            "For andre statuser: kast e videre")
        }
    }
}