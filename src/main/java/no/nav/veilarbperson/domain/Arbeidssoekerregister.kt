package no.nav.veilarbperson.domain

import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.OpplysningerOmArbeidssoekerResponse
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.ProfileringResponse

data class OpplysningerOmArbeidssoekerMedProfilering(
    val opplysningerOmArbeidssoeker: OpplysningerOmArbeidssoekerResponse? = null,
    val profilering: ProfileringResponse ? = null
)



fun OpplysningerOmArbeidssoekerResponse.mapToOpplyasningerOmArbeidssoekerMedNuskode(): OpplysningerOmArbeidssoekerResponse {
    val utdanning = this.utdanning?.copy(nus = mapNuskodeTilUtdanningsnivaa(this.utdanning.nus))
    return this.copy(utdanning = utdanning)
}


fun mapNuskodeTilUtdanningsnivaa(nusKode: String?): String {
    return when (nusKode) {
        "0" -> "INGEN_UTDANNING"
        "2" -> "GRUNNSKOLE"
        "3" -> "VIDEREGAENDE_GRUNNUTDANNING"
        "4" -> "VIDEREGAENDE_FAGBREV_SVENNEBREV"
        "6" -> "HOYERE_UTDANNING_1_TIL_4"
        "7" -> "HOYERE_UTDANNING_5_ELLER_MER"
        else -> "INGEN_SVAR"
    }
}