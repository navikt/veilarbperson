package no.nav.veilarbperson.domain.visittkort

import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.ProfilertTil
import no.nav.veilarbperson.client.pdl.PdlVisittkortEtiketterData
import no.nav.veilarbperson.client.representasjon.VisittkortRepresentasjonData

sealed interface VisittkortKildeResultat<out T> {
    data class Hentet<T>(val data: T) : VisittkortKildeResultat<T>
    data object Ukjent : VisittkortKildeResultat<Nothing>
}

data class VisittkortEtikettgrunnlag(
    val pdl: VisittkortKildeResultat<PdlVisittkortEtiketterData>? = null,
    val representasjon: VisittkortKildeResultat<VisittkortRepresentasjonData>? = null,
    val skjerming: VisittkortKildeResultat<Boolean>? = null,
    val oppfolging: VisittkortKildeResultat<OppfolgingEtikettgrunnlag>? = null,
    val arbeidssoker: VisittkortKildeResultat<Boolean>? = null,
    val gjeldendeVedtak: VisittkortKildeResultat<Boolean>? = null,
    val varsel: VisittkortKildeResultat<Boolean>? = null,
    val profilering: VisittkortKildeResultat<ProfilertTil?>? = null
)

data class OppfolgingEtikettgrunnlag(
    val underOppfolging: Boolean? = null,
    val manuell: Boolean? = null,
    val reservasjonKrr: Boolean? = null,
    val registrertKrr: Boolean? = null,
    val kanVarsles: Boolean? = null,
    val sykmeldt: Boolean? = null,
    val utmeldingskandidat: UtmeldingskandidatEtikett? = null,
    val kvp: Boolean? = null
)
