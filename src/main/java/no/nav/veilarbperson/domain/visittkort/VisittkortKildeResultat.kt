package no.nav.veilarbperson.domain.visittkort

sealed interface VisittkortKildeResultat<out T> {
    data class Hentet<T>(val data: T) : VisittkortKildeResultat<T>
    data object Ukjent : VisittkortKildeResultat<Nothing>
}

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
