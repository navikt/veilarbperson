package no.nav.veilarbperson.utils

import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.ArbeidssokerVisittkortData
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.ProfilertTil
import no.nav.veilarbperson.client.pdl.PdlVisittkortEtiketterData
import no.nav.veilarbperson.client.pdl.domain.Diskresjonskode
import no.nav.veilarbperson.client.representasjon.VisittkortRepresentasjonData
import no.nav.veilarbperson.domain.visittkort.*
import no.nav.veilarbperson.domain.visittkort.VisittkortKildeResultat.Hentet
import no.nav.veilarbperson.domain.visittkort.VisittkortKildeResultat.Ukjent

object VisittkortEtiketterMapper {
    fun map(
        felter: Set<String>,
        pdl: VisittkortKildeResultat<PdlVisittkortEtiketterData>? = null,
        representasjon: VisittkortKildeResultat<VisittkortRepresentasjonData>? = null,
        skjerming: VisittkortKildeResultat<Boolean>? = null,
        oppfolging: VisittkortKildeResultat<OppfolgingEtikettgrunnlag>? = null,
        arbeidssoker: VisittkortKildeResultat<ArbeidssokerVisittkortData>? = null,
        gjeldendeVedtak: VisittkortKildeResultat<Boolean>? = null,
        varsel: VisittkortKildeResultat<Boolean>? = null,
        kvp: VisittkortKildeResultat<Boolean>? = null
    ): VisittkortEtiketter {
        fun <T, E> etikett(
            felt: String,
            kilde: VisittkortKildeResultat<T>?,
            ukjent: E,
            regel: (T) -> E?
        ): E? {
            if (felt !in felter) return null
            return when (kilde) {
                is Hentet -> regel(kilde.data)
                Ukjent -> ukjent
                null -> error("Missing required visittkort source")
            }
        }

        fun profilering(verdi: ProfilertTil): Sannhet =
            oppfolging.sannhet { true }.og {
                gjeldendeVedtak.sannhet { !it }
            }.og {
                arbeidssoker.sannhet { it.profilering == verdi }
            }

        return VisittkortEtiketter(
            dod = etikett("dod", pdl, DodEtikett.UKJENT_PERSONALIA) {
                DodEtikett.DOD.takeIf { _ -> it.dod }
            },
            diskresjonskode = etikett("diskresjonskode", pdl, DiskresjonskodeEtikett.UKJENT_PERSONALIA) {
                when (it.diskresjonskode) {
                    null, Diskresjonskode.UGRADERT -> null
                    Diskresjonskode.FORTROLIG -> DiskresjonskodeEtikett.KODE_7
                    Diskresjonskode.STRENGT_FORTROLIG -> DiskresjonskodeEtikett.KODE_6
                    Diskresjonskode.STRENGT_FORTROLIG_UTLAND -> DiskresjonskodeEtikett.KODE_19
                }
            },
            sikkerhetstiltak = etikett(
                "sikkerhetstiltak", pdl,
                SikkerhetstiltakResultat(SikkerhetstiltakEtikett.UKJENT_SIKKERHETSTILTAK, null)
            ) {
                it.sikkerhetstiltak?.let { tekst ->
                    SikkerhetstiltakResultat(SikkerhetstiltakEtikett.SIKKERHETSTILTAK, tekst)
                }
            },
            tegnspraaktolk = etikett("tegnspraaktolk", pdl, TegnspraaktolkEtikett.UKJENT_TOLKEBEHOV) {
                TegnspraaktolkEtikett.TEGNSPRAAKTOLK.takeIf { _ -> it.tegnspraaktolk }
            },
            talespraaktolk = etikett("talespraaktolk", pdl, TalespraaktolkEtikett.UKJENT_TOLKEBEHOV) {
                TalespraaktolkEtikett.TALESPRAAKTOLK.takeIf { _ -> it.talespraaktolk }
            },
            vergemaalOppfolging = etikett(
                "vergemaalOppfolging", representasjon, VergemaalOppfolgingEtikett.UKJENT_REPRESENTASJON
            ) {
                VergemaalOppfolgingEtikett.VERGEMAAL_OPPFOLGING.takeIf { _ -> it.vergemaalOppfolging }
            },
            fullmaktOppfolging = etikett(
                "fullmaktOppfolging", representasjon, FullmaktOppfolgingEtikett.UKJENT_REPRESENTASJON
            ) {
                FullmaktOppfolgingEtikett.FULLMAKT_OPPFOLGING.takeIf { _ -> it.fullmaktOppfolging }
            },
            skjerming = etikett("skjerming", skjerming, SkjermingEtikett.UKJENT_SKJERMING) {
                SkjermingEtikett.SKJERMET.takeIf { _ -> it }
            },
            utmeldingskandidat = etikett(
                "utmeldingskandidat", oppfolging, UtmeldingskandidatEtikett.UKJENT_UTMELDINGSKANDIDAT
            ) { it.utmeldingskandidat },
            kvp = etikett("kvp", kvp, KvpEtikett.UKJENT_OPPFOLGING) {
                KvpEtikett.KVP.takeIf { _ -> it }
            },
            manuellOppfolging = etikett(
                "manuellOppfolging", oppfolging, ManuellOppfolgingEtikett.UKJENT_OPPFOLGING
            ) {
                ManuellOppfolgingEtikett.MANUELL_OPPFOLGING.takeIf { _ -> it.manuell.paakrevd() }
            },
            reservertKrr = etikett("reservertKrr", oppfolging, ReservertKrrEtikett.UKJENT_KRR) {
                ReservertKrrEtikett.RESERVERT_KRR.takeIf { _ -> it.reservasjonKrr.paakrevd() }
            },
            utdatertIKrr = etikett("utdatertIKrr", oppfolging, UtdatertIKrrEtikett.UKJENT_KRR) {
                UtdatertIKrrEtikett.UTDATERT_I_KRR.takeIf { _ ->
                    it.registrertKrr.paakrevd() && !it.kanVarsles.paakrevd() &&
                        !it.manuell.paakrevd() && !it.reservasjonKrr.paakrevd()
                }
            },
            ikkeUnderOppfolging = etikett(
                "ikkeUnderOppfolging", oppfolging, IkkeUnderOppfolgingEtikett.UKJENT_OPPFOLGING
            ) {
                IkkeUnderOppfolgingEtikett.IKKE_UNDER_OPPFOLGING.takeIf { _ -> !it.underOppfolging.paakrevd() }
            },
            ikkeRegistrertKrr = etikett("ikkeRegistrertKrr", oppfolging, IkkeRegistrertKrrEtikett.UKJENT_KRR) {
                IkkeRegistrertKrrEtikett.IKKE_REGISTRERT_KRR.takeIf { _ -> !it.registrertKrr.paakrevd() }
            },
            sykmeldt = etikett("sykmeldt", oppfolging, SykmeldtEtikett.UKJENT_OPPFOLGING) {
                SykmeldtEtikett.SYKMELDT.takeIf { _ -> it.sykmeldt.paakrevd() }
            },
            varsel = etikett("varsel", varsel, VarselEtikett.UKJENT_VARSEL) {
                VarselEtikett.VARSEL.takeIf { _ -> it }
            },
            iArbeidssokerregisteret = etikett(
                "iArbeidssokerregisteret", arbeidssoker, IArbeidssokerregisteretEtikett.UKJENT_ARBEIDSSOKERREGISTER
            ) {
                IArbeidssokerregisteretEtikett.I_ARBEIDSSOKERREGISTERET.takeIf { _ -> it.registrert }
            },
            trengerOppfolgingsvedtak = if ("trengerOppfolgingsvedtak" in felter) {
                oppfolging.sannhet { it.underOppfolging.paakrevd() }.og {
                    gjeldendeVedtak.sannhet { !it }
                }.etikett(
                    TrengerOppfolgingsvedtakEtikett.TRENGER_OPPFOLGINGSVEDTAK,
                    TrengerOppfolgingsvedtakEtikett.UKJENT_OPPFOLGINGSVEDTAK
                )
            } else null,
            antattGodeMuligheter = if ("antattGodeMuligheter" in felter) {
                profilering(ProfilertTil.ANTATT_GODE_MULIGHETER).etikett(
                    AntattGodeMuligheterEtikett.ANTATT_GODE_MULIGHETER,
                    AntattGodeMuligheterEtikett.UKJENT_PROFILERING
                )
            } else null,
            antattBehovForVeiledning = if ("antattBehovForVeiledning" in felter) {
                profilering(ProfilertTil.ANTATT_BEHOV_FOR_VEILEDNING).etikett(
                    AntattBehovForVeiledningEtikett.ANTATT_BEHOV_FOR_VEILEDNING,
                    AntattBehovForVeiledningEtikett.UKJENT_PROFILERING
                )
            } else null,
            oppgittHindringer = if ("oppgittHindringer" in felter) {
                profilering(ProfilertTil.OPPGITT_HINDRINGER).etikett(
                    OppgittHindringerEtikett.OPPGITT_HINDRINGER,
                    OppgittHindringerEtikett.UKJENT_PROFILERING
                )
            } else null
        )
    }

    private fun Boolean?.paakrevd(): Boolean = checkNotNull(this) { "Missing required follow-up field" }

    private enum class Sannhet {
        JA, NEI, UKJENT;

        fun og(annen: () -> Sannhet): Sannhet {
            if (this == NEI) return NEI
            val resultat = annen()
            return when {
                resultat == NEI -> NEI
                this == UKJENT || resultat == UKJENT -> UKJENT
                else -> JA
            }
        }

        fun <E> etikett(positiv: E, ukjent: E): E? = when (this) {
            JA -> positiv
            NEI -> null
            UKJENT -> ukjent
        }
    }

    private fun <T> VisittkortKildeResultat<T>?.sannhet(regel: (T) -> Boolean): Sannhet = when (this) {
        is Hentet -> if (regel(data)) Sannhet.JA else Sannhet.NEI
        Ukjent -> Sannhet.UKJENT
        null -> error("Missing required visittkort source")
    }
}
