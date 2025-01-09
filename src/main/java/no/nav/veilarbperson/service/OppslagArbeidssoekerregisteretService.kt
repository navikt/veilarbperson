package no.nav.veilarbperson.service

import no.nav.common.types.identer.Fnr
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.ArbeidssokerperiodeResponse
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.OppslagArbeidssoekerregisteretClient
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
        val aktivArbeidssoekerperiode = oppslagArbeidssoekerregisteretClient.hentArbeidssokerPerioder(fnr.get())
            ?.find { it.avsluttet == null }

        if (aktivArbeidssoekerperiode == null) {
            throw ResponseStatusException(HttpStatusCode.valueOf(204),"Fant ingen aktiv arbeidssøkerperiode for bruker")
        }

        val sisteOpplysningerOmArbeidssoeker = oppslagArbeidssoekerregisteretClient.hentOpplysningerOmArbeidssoeker(
            fnr.get(),
            aktivArbeidssoekerperiode.periodeId
        )?.maxByOrNull {
            it.sendtInnAv.tidspunkt
        }

        if (sisteOpplysningerOmArbeidssoeker == null) {
            return OpplysningerOmArbeidssoekerMedProfilering(aktivArbeidssoekerperiode.startet.tidspunkt, null, null)
        }

        val sisteProfilering =
            oppslagArbeidssoekerregisteretClient.hentProfilering(fnr.get(), aktivArbeidssoekerperiode.periodeId)
                ?.filter { it.opplysningerOmArbeidssoekerId == sisteOpplysningerOmArbeidssoeker.opplysningerOmArbeidssoekerId }
                ?.maxByOrNull { it.sendtInnAv.tidspunkt }

        return OpplysningerOmArbeidssoekerMedProfilering(aktivArbeidssoekerperiode.startet.tidspunkt, sisteOpplysningerOmArbeidssoeker.mapToOpplyasningerOmArbeidssoekerMedNuskode(), sisteProfilering)
    }
}