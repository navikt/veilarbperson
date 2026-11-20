package no.nav.veilarbperson.service

import no.nav.common.types.identer.Fnr
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.ArbeidssokerVisittkortData
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.ArbeidssokerperiodeResponse
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.OppslagArbeidssoekerregisteretClient
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.OpplysningerOmArbeidssoekerResponse
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.ProfileringResponse
import no.nav.veilarbperson.domain.OpplysningerOmArbeidssoekerMedProfilering
import no.nav.veilarbperson.domain.mapToOpplyasningerOmArbeidssoekerMedNuskode
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class OppslagArbeidssoekerregisteretService(
    private val oppslagArbeidssoekerregisteretClient: OppslagArbeidssoekerregisteretClient
) {

    fun hentSisteArbeidssoekerPeriode(fnr: Fnr): ArbeidssokerperiodeResponse? = oppslagArbeidssoekerregisteretClient.hentArbeidssokerPerioder(fnr.get())?.find { it.avsluttet == null }

    fun hentSisteOpplysningerOmArbeidssoekerMedProfilering(fnr: Fnr): OpplysningerOmArbeidssoekerMedProfilering? {
        val aktivArbeidssoekerperiode = hentSisteArbeidssoekerPeriode(fnr)

        if (aktivArbeidssoekerperiode == null) {
            throw ResponseStatusException(HttpStatusCode.valueOf(204),"Fant ingen aktiv arbeidssøkerperiode for bruker")
        }

        val sisteOpplysningerOmArbeidssoeker = hentSisteOpplysninger(fnr, aktivArbeidssoekerperiode)

        if (sisteOpplysningerOmArbeidssoeker == null) {
            return OpplysningerOmArbeidssoekerMedProfilering(aktivArbeidssoekerperiode.startet.tidspunkt, null, null)
        }

        val sisteProfilering = hentSisteMatchendeProfilering(fnr, aktivArbeidssoekerperiode, sisteOpplysningerOmArbeidssoeker)

        return OpplysningerOmArbeidssoekerMedProfilering(aktivArbeidssoekerperiode.startet.tidspunkt, sisteOpplysningerOmArbeidssoeker.mapToOpplyasningerOmArbeidssoekerMedNuskode(), sisteProfilering)
    }

    fun hentVisittkortData(fnr: Fnr, medProfilering: Boolean): ArbeidssokerVisittkortData {
        val aktivPeriode = hentSisteArbeidssoekerPeriode(fnr) ?: return ArbeidssokerVisittkortData(false)
        if (!medProfilering) return ArbeidssokerVisittkortData(true)

        val sisteOpplysninger = hentSisteOpplysninger(fnr, aktivPeriode) ?: return ArbeidssokerVisittkortData(true)
        val profilering = hentSisteMatchendeProfilering(fnr, aktivPeriode, sisteOpplysninger)
        return ArbeidssokerVisittkortData(true, profilering?.profilertTil)
    }

    private fun hentSisteOpplysninger(
        fnr: Fnr,
        periode: ArbeidssokerperiodeResponse
    ): OpplysningerOmArbeidssoekerResponse? =
        oppslagArbeidssoekerregisteretClient.hentOpplysningerOmArbeidssoeker(fnr.get(), periode.periodeId)
            ?.maxByOrNull { it.sendtInnAv.tidspunkt }

    private fun hentSisteMatchendeProfilering(
        fnr: Fnr,
        periode: ArbeidssokerperiodeResponse,
        opplysninger: OpplysningerOmArbeidssoekerResponse
    ): ProfileringResponse? =
        oppslagArbeidssoekerregisteretClient.hentProfilering(fnr.get(), periode.periodeId)
            ?.filter { it.opplysningerOmArbeidssoekerId == opplysninger.opplysningerOmArbeidssoekerId }
            ?.maxByOrNull { it.sendtInnAv.tidspunkt }
}
