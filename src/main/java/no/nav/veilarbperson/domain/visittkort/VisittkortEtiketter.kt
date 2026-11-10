package no.nav.veilarbperson.domain.visittkort

interface Etikettverdi {
    val erUkjent: Boolean
}

enum class DodEtikett(override val erUkjent: Boolean = false) : Etikettverdi {
    DOD, UKJENT_PERSONALIA(true)
}

enum class DiskresjonskodeEtikett(override val erUkjent: Boolean = false) : Etikettverdi {
    KODE_6, KODE_7, KODE_19, UKJENT_PERSONALIA(true)
}

enum class UtmeldingskandidatEtikett(override val erUkjent: Boolean = false) : Etikettverdi {
    ARBEIDSSOKERPERIODE_AVSLUTTET_IKKE_LEVERT_MELDEKORT,
    ARBEIDSSOKERPERIODE_AVSLUTTET_SVARTE_NEI_I_BEKREFTELSE,
    ARBEIDSSOKERPERIODE_AVSLUTTET_ANNET,
    FORLENGELSE_UTLOPT,
    AVSLUTTET_AAP_SAK,
    FOLKEREGISTERSTATUS_FORSVUNNET,
    FOLKEREGISTERSTATUS_OPPHORT,
    FOLKEREGISTERSTATUS_UKJENT,
    FOLKEREGISTERSTATUS_INGEN_STATUS,
    UKJENT_UTMELDINGSKANDIDAT(true)
}

enum class SikkerhetstiltakEtikett(override val erUkjent: Boolean = false) : Etikettverdi {
    SIKKERHETSTILTAK, UKJENT_SIKKERHETSTILTAK(true)
}

data class SikkerhetstiltakResultat(
    val etikett: SikkerhetstiltakEtikett,
    val beskrivelse: String?
)

enum class SkjermingEtikett(override val erUkjent: Boolean = false) : Etikettverdi {
    SKJERMET, UKJENT_SKJERMING(true)
}

enum class VergemaalOppfolgingEtikett(override val erUkjent: Boolean = false) : Etikettverdi {
    VERGEMAAL_OPPFOLGING, UKJENT_REPRESENTASJON(true)
}

enum class FullmaktOppfolgingEtikett(override val erUkjent: Boolean = false) : Etikettverdi {
    FULLMAKT_OPPFOLGING, UKJENT_REPRESENTASJON(true)
}

enum class TegnspraaktolkEtikett(override val erUkjent: Boolean = false) : Etikettverdi {
    TEGNSPRAAKTOLK, UKJENT_TOLKEBEHOV(true)
}

enum class TalespraaktolkEtikett(override val erUkjent: Boolean = false) : Etikettverdi {
    TALESPRAAKTOLK, UKJENT_TOLKEBEHOV(true)
}

enum class KvpEtikett(override val erUkjent: Boolean = false) : Etikettverdi {
    KVP, UKJENT_OPPFOLGING(true)
}

enum class ManuellOppfolgingEtikett(override val erUkjent: Boolean = false) : Etikettverdi {
    MANUELL_OPPFOLGING, UKJENT_OPPFOLGING(true)
}

enum class ReservertKrrEtikett(override val erUkjent: Boolean = false) : Etikettverdi {
    RESERVERT_KRR, UKJENT_KRR(true)
}

enum class UtdatertIKrrEtikett(override val erUkjent: Boolean = false) : Etikettverdi {
    UTDATERT_I_KRR, UKJENT_KRR(true)
}

enum class IkkeUnderOppfolgingEtikett(override val erUkjent: Boolean = false) : Etikettverdi {
    IKKE_UNDER_OPPFOLGING, UKJENT_OPPFOLGING(true)
}

enum class VarselEtikett(override val erUkjent: Boolean = false) : Etikettverdi {
    VARSEL, UKJENT_VARSEL(true)
}

enum class IkkeRegistrertKrrEtikett(override val erUkjent: Boolean = false) : Etikettverdi {
    IKKE_REGISTRERT_KRR, UKJENT_KRR(true)
}

enum class TrengerOppfolgingsvedtakEtikett(override val erUkjent: Boolean = false) : Etikettverdi {
    TRENGER_OPPFOLGINGSVEDTAK, UKJENT_OPPFOLGINGSVEDTAK(true)
}

enum class SykmeldtEtikett(override val erUkjent: Boolean = false) : Etikettverdi {
    SYKMELDT, UKJENT_OPPFOLGING(true)
}

enum class IArbeidssokerregisteretEtikett(override val erUkjent: Boolean = false) : Etikettverdi {
    I_ARBEIDSSOKERREGISTERET, UKJENT_ARBEIDSSOKERREGISTER(true)
}

enum class AntattGodeMuligheterEtikett(override val erUkjent: Boolean = false) : Etikettverdi {
    ANTATT_GODE_MULIGHETER, UKJENT_PROFILERING(true)
}

enum class AntattBehovForVeiledningEtikett(override val erUkjent: Boolean = false) : Etikettverdi {
    ANTATT_BEHOV_FOR_VEILEDNING, UKJENT_PROFILERING(true)
}

enum class OppgittHindringerEtikett(override val erUkjent: Boolean = false) : Etikettverdi {
    OPPGITT_HINDRINGER, UKJENT_PROFILERING(true)
}

data class VisittkortEtiketter(
    val dod: DodEtikett? = null,
    val diskresjonskode: DiskresjonskodeEtikett? = null,
    val utmeldingskandidat: UtmeldingskandidatEtikett? = null,
    val sikkerhetstiltak: SikkerhetstiltakResultat? = null,
    val skjerming: SkjermingEtikett? = null,
    val vergemaalOppfolging: VergemaalOppfolgingEtikett? = null,
    val fullmaktOppfolging: FullmaktOppfolgingEtikett? = null,
    val tegnspraaktolk: TegnspraaktolkEtikett? = null,
    val talespraaktolk: TalespraaktolkEtikett? = null,
    val kvp: KvpEtikett? = null,
    val manuellOppfolging: ManuellOppfolgingEtikett? = null,
    val reservertKrr: ReservertKrrEtikett? = null,
    val utdatertIKrr: UtdatertIKrrEtikett? = null,
    val ikkeUnderOppfolging: IkkeUnderOppfolgingEtikett? = null,
    val varsel: VarselEtikett? = null,
    val ikkeRegistrertKrr: IkkeRegistrertKrrEtikett? = null,
    val trengerOppfolgingsvedtak: TrengerOppfolgingsvedtakEtikett? = null,
    val sykmeldt: SykmeldtEtikett? = null,
    val iArbeidssokerregisteret: IArbeidssokerregisteretEtikett? = null,
    val antattGodeMuligheter: AntattGodeMuligheterEtikett? = null,
    val antattBehovForVeiledning: AntattBehovForVeiledningEtikett? = null,
    val oppgittHindringer: OppgittHindringerEtikett? = null
) {
    fun ukjenteFelter(): List<String> = listOf(
        "dod" to dod,
        "diskresjonskode" to diskresjonskode,
        "utmeldingskandidat" to utmeldingskandidat,
        "sikkerhetstiltak" to sikkerhetstiltak?.etikett,
        "skjerming" to skjerming,
        "vergemaalOppfolging" to vergemaalOppfolging,
        "fullmaktOppfolging" to fullmaktOppfolging,
        "tegnspraaktolk" to tegnspraaktolk,
        "talespraaktolk" to talespraaktolk,
        "kvp" to kvp,
        "manuellOppfolging" to manuellOppfolging,
        "reservertKrr" to reservertKrr,
        "utdatertIKrr" to utdatertIKrr,
        "ikkeUnderOppfolging" to ikkeUnderOppfolging,
        "varsel" to varsel,
        "ikkeRegistrertKrr" to ikkeRegistrertKrr,
        "trengerOppfolgingsvedtak" to trengerOppfolgingsvedtak,
        "sykmeldt" to sykmeldt,
        "iArbeidssokerregisteret" to iArbeidssokerregisteret,
        "antattGodeMuligheter" to antattGodeMuligheter,
        "antattBehovForVeiledning" to antattBehovForVeiledning,
        "oppgittHindringer" to oppgittHindringer
    ).filter { (_, verdi) -> verdi?.erUkjent == true }.map { it.first }
}
