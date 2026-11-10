package no.nav.veilarbperson.utils

import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.ArbeidssokerVisittkortData
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.ProfilertTil
import no.nav.veilarbperson.client.pdl.PdlVisittkortEtiketterData
import no.nav.veilarbperson.client.pdl.domain.Diskresjonskode
import no.nav.veilarbperson.client.representasjon.VisittkortRepresentasjonData
import no.nav.veilarbperson.domain.visittkort.*
import no.nav.veilarbperson.domain.visittkort.VisittkortKildeResultat.Hentet
import no.nav.veilarbperson.domain.visittkort.VisittkortKildeResultat.Ukjent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class VisittkortEtiketterMapperTest {
    @Test
    fun `gyldig fravaer gir null for alle 22 felt`() {
        val resultat = VisittkortEtiketterMapper.map(
            ALLE_FELTER,
            pdl = Hentet(PdlVisittkortEtiketterData()),
            representasjon = Hentet(VisittkortRepresentasjonData(false, false)),
            skjerming = Hentet(false),
            oppfolging = Hentet(oppfolging()),
            arbeidssoker = Hentet(ArbeidssokerVisittkortData(false)),
            gjeldendeVedtak = Hentet(true),
            varsel = Hentet(false),
            kvp = Hentet(false)
        )

        assertThat(resultat).isEqualTo(VisittkortEtiketter())
    }

    @Test
    fun `kildeutfall ukjent er forskjellig fra fravaer for alle 22 felt`() {
        val resultat = VisittkortEtiketterMapper.map(
            ALLE_FELTER, Ukjent, Ukjent, Ukjent, Ukjent, Ukjent, Ukjent, Ukjent, Ukjent
        )

        assertThat(resultat.ukjenteFelter().toSet()).isEqualTo(ALLE_FELTER)
        assertThat(resultat.sikkerhetstiltak?.beskrivelse).isNull()
    }

    @Test
    fun `personalia tolking og representasjon har egne enumverdier`() {
        val resultat = VisittkortEtiketterMapper.map(
            setOf("dod", "diskresjonskode", "sikkerhetstiltak", "tegnspraaktolk", "talespraaktolk",
                "vergemaalOppfolging", "fullmaktOppfolging", "skjerming"),
            pdl = Hentet(PdlVisittkortEtiketterData(true, Diskresjonskode.FORTROLIG, "Kildebeskrivelse", true, true)),
            representasjon = Hentet(VisittkortRepresentasjonData(true, true)),
            skjerming = Hentet(true)
        )

        assertThat(resultat.dod).isEqualTo(DodEtikett.DOD)
        assertThat(resultat.diskresjonskode).isEqualTo(DiskresjonskodeEtikett.KODE_7)
        assertThat(resultat.sikkerhetstiltak).isEqualTo(
            SikkerhetstiltakResultat(SikkerhetstiltakEtikett.SIKKERHETSTILTAK, "Kildebeskrivelse")
        )
        assertThat(resultat.tegnspraaktolk).isEqualTo(TegnspraaktolkEtikett.TEGNSPRAAKTOLK)
        assertThat(resultat.talespraaktolk).isEqualTo(TalespraaktolkEtikett.TALESPRAAKTOLK)
        assertThat(resultat.vergemaalOppfolging).isEqualTo(VergemaalOppfolgingEtikett.VERGEMAAL_OPPFOLGING)
        assertThat(resultat.fullmaktOppfolging).isEqualTo(FullmaktOppfolgingEtikett.FULLMAKT_OPPFOLGING)
        assertThat(resultat.skjerming).isEqualTo(SkjermingEtikett.SKJERMET)
    }

    @Test
    fun `diskresjonskoder 6 7 og 19 skilles fra ugradert`() {
        for ((kode, forventet) in mapOf(
            Diskresjonskode.UGRADERT to null,
            Diskresjonskode.FORTROLIG to DiskresjonskodeEtikett.KODE_7,
            Diskresjonskode.STRENGT_FORTROLIG to DiskresjonskodeEtikett.KODE_6,
            Diskresjonskode.STRENGT_FORTROLIG_UTLAND to DiskresjonskodeEtikett.KODE_19
        )) {
            assertThat(VisittkortEtiketterMapper.map(
                setOf("diskresjonskode"), pdl = Hentet(PdlVisittkortEtiketterData(diskresjonskode = kode))
            ).diskresjonskode).isEqualTo(forventet)
        }
    }

    @Test
    fun `bare etterspurte felt mappes og manglende etterspurt kilde feiler`() {
        val resultat = VisittkortEtiketterMapper.map(setOf("skjerming"), skjerming = Hentet(true))
        assertThat(resultat).isEqualTo(VisittkortEtiketter(skjerming = SkjermingEtikett.SKJERMET))
        assertThrows<IllegalStateException> { VisittkortEtiketterMapper.map(setOf("dod")) }
    }

    @Test
    fun `utdatert KRR krever alle fire vilkaar`() {
        for (registrert in listOf(false, true)) {
            for (kanVarsles in listOf(false, true)) {
                for (manuell in listOf(false, true)) {
                    for (reservert in listOf(false, true)) {
                        val resultat = VisittkortEtiketterMapper.map(
                            setOf("utdatertIKrr"),
                            oppfolging = Hentet(oppfolging().copy(
                                registrertKrr = registrert, kanVarsles = kanVarsles,
                                manuell = manuell, reservasjonKrr = reservert
                            ))
                        )
                        assertThat(resultat.utdatertIKrr).isEqualTo(
                            UtdatertIKrrEtikett.UTDATERT_I_KRR.takeIf {
                                registrert && !kanVarsles && !manuell && !reservert
                            }
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `oppfolgingsetiketter krever eksplisitte gyldige verdier`() {
        val resultat = VisittkortEtiketterMapper.map(
            setOf("kvp", "manuellOppfolging", "reservertKrr", "ikkeUnderOppfolging",
                "ikkeRegistrertKrr", "sykmeldt", "varsel", "iArbeidssokerregisteret"),
            oppfolging = Hentet(oppfolging().copy(
                underOppfolging = false, manuell = true,
                reservasjonKrr = true, registrertKrr = false, sykmeldt = true
            )),
            arbeidssoker = Hentet(ArbeidssokerVisittkortData(true)),
            varsel = Hentet(true),
            kvp = Hentet(true)
        )

        assertThat(resultat.kvp).isEqualTo(KvpEtikett.KVP)
        assertThat(resultat.manuellOppfolging).isEqualTo(ManuellOppfolgingEtikett.MANUELL_OPPFOLGING)
        assertThat(resultat.reservertKrr).isEqualTo(ReservertKrrEtikett.RESERVERT_KRR)
        assertThat(resultat.ikkeUnderOppfolging).isEqualTo(IkkeUnderOppfolgingEtikett.IKKE_UNDER_OPPFOLGING)
        assertThat(resultat.ikkeRegistrertKrr).isEqualTo(IkkeRegistrertKrrEtikett.IKKE_REGISTRERT_KRR)
        assertThat(resultat.sykmeldt).isEqualTo(SykmeldtEtikett.SYKMELDT)
        assertThat(resultat.varsel).isEqualTo(VarselEtikett.VARSEL)
        assertThat(resultat.iArbeidssokerregisteret)
            .isEqualTo(IArbeidssokerregisteretEtikett.I_ARBEIDSSOKERREGISTERET)
    }

    @Test
    fun `alle ni utmeldingskandidattagger er positive og ikke kildefeil`() {
        val tagger = UtmeldingskandidatEtikett.entries.filter { !it.erUkjent }
        assertThat(tagger).hasSize(9)
        for (tag in tagger) {
            val resultat = VisittkortEtiketterMapper.map(
                setOf("utmeldingskandidat"), oppfolging = Hentet(oppfolging().copy(utmeldingskandidat = tag))
            )
            assertThat(resultat.utmeldingskandidat).isEqualTo(tag)
            assertThat(resultat.ukjenteFelter()).isEmpty()
        }
    }

    @Test
    fun `KVP mappes uten andre oppfolgingsopplysninger`() {
        assertThat(VisittkortEtiketterMapper.map(setOf("kvp"), kvp = Hentet(true)).kvp)
            .isEqualTo(KvpEtikett.KVP)
        assertThat(VisittkortEtiketterMapper.map(setOf("kvp"), kvp = Hentet(false)).kvp).isNull()
        assertThat(VisittkortEtiketterMapper.map(setOf("kvp"), kvp = Ukjent).kvp)
            .isEqualTo(KvpEtikett.UKJENT_OPPFOLGING)
        assertThrows<IllegalStateException> { VisittkortEtiketterMapper.map(setOf("kvp")) }
    }

    @Test
    fun `vedtaksbehov skiller avkreftet fra ukjent i alle kombinasjoner`() {
        for (underOppfolging in listOf(false, true, null)) {
            for (harVedtak in listOf(false, true, null)) {
                val resultat = VisittkortEtiketterMapper.map(
                    setOf("trengerOppfolgingsvedtak"),
                    oppfolging = underOppfolging?.let { Hentet(oppfolging().copy(underOppfolging = it)) } ?: Ukjent,
                    gjeldendeVedtak = harVedtak?.let { Hentet(it) } ?: Ukjent
                )
                val forventet = when {
                    underOppfolging == false || harVedtak == true -> null
                    underOppfolging == null || harVedtak == null ->
                        TrengerOppfolgingsvedtakEtikett.UKJENT_OPPFOLGINGSVEDTAK
                    else -> TrengerOppfolgingsvedtakEtikett.TRENGER_OPPFOLGINGSVEDTAK
                }
                assertThat(resultat.trengerOppfolgingsvedtak).isEqualTo(forventet)
            }
        }
        assertThat(VisittkortEtiketterMapper.map(
            setOf("trengerOppfolgingsvedtak"),
            oppfolging = Hentet(oppfolging().copy(underOppfolging = false))
        ).trengerOppfolgingsvedtak).isNull()
    }

    @Test
    fun `profilering krever ikke under oppfolging men undertrykkes av gjeldende vedtak`() {
        val profilfelter = setOf("antattGodeMuligheter", "antattBehovForVeiledning", "oppgittHindringer")
        for (profilering in listOf(
            ProfilertTil.ANTATT_GODE_MULIGHETER,
            ProfilertTil.ANTATT_BEHOV_FOR_VEILEDNING,
            ProfilertTil.OPPGITT_HINDRINGER
        )) {
            val resultat = VisittkortEtiketterMapper.map(
                profilfelter,
                oppfolging = Hentet(oppfolging().copy(underOppfolging = false)),
                arbeidssoker = Hentet(ArbeidssokerVisittkortData(true, profilering)),
                gjeldendeVedtak = Hentet(false)
            )
            assertThat(listOf(
                resultat.antattGodeMuligheter, resultat.antattBehovForVeiledning, resultat.oppgittHindringer
            ).count { it != null }).isEqualTo(1)
            assertThat(resultat.ukjenteFelter()).isEmpty()
        }
        assertThat(VisittkortEtiketterMapper.map(
            profilfelter, oppfolging = Ukjent, gjeldendeVedtak = Hentet(true)
        )).isEqualTo(VisittkortEtiketter())
    }

    @Test
    fun `feilet PDL endrer ikke vellykket representasjon og beholder ingen gammel beskrivelse`() {
        val resultat = VisittkortEtiketterMapper.map(
            setOf("sikkerhetstiltak", "fullmaktOppfolging"),
            pdl = Ukjent, representasjon = Hentet(VisittkortRepresentasjonData(false, true))
        )
        assertThat(resultat.sikkerhetstiltak?.etikett)
            .isEqualTo(SikkerhetstiltakEtikett.UKJENT_SIKKERHETSTILTAK)
        assertThat(resultat.sikkerhetstiltak?.beskrivelse).isNull()
        assertThat(resultat.fullmaktOppfolging).isEqualTo(FullmaktOppfolgingEtikett.FULLMAKT_OPPFOLGING)
    }

    @Test
    fun `delvis oppfolgingsgrunnlag krever bare verdiene som faktisk brukes`() {
        val resultat = VisittkortEtiketterMapper.map(
            setOf("manuellOppfolging"),
            oppfolging = Hentet(OppfolgingEtikettgrunnlag(manuell = true))
        )
        assertThat(resultat).isEqualTo(
            VisittkortEtiketter(manuellOppfolging = ManuellOppfolgingEtikett.MANUELL_OPPFOLGING)
        )

        for (felt in setOf("manuellOppfolging", "reservertKrr", "utdatertIKrr",
            "ikkeUnderOppfolging", "ikkeRegistrertKrr", "sykmeldt", "trengerOppfolgingsvedtak")) {
            assertThrows<IllegalStateException> {
                VisittkortEtiketterMapper.map(
                    setOf(felt), oppfolging = Hentet(OppfolgingEtikettgrunnlag()),
                    gjeldendeVedtak = Hentet(false)
                )
            }
        }
    }

    @Test
    fun `profilering krever gyldig oppfolgingsgrunnlag men ikke et boolsk oppfolgingsflagg`() {
        val resultat = VisittkortEtiketterMapper.map(
            setOf("antattGodeMuligheter"),
            oppfolging = Hentet(OppfolgingEtikettgrunnlag()),
            arbeidssoker = Hentet(ArbeidssokerVisittkortData(true, ProfilertTil.ANTATT_GODE_MULIGHETER)),
            gjeldendeVedtak = Hentet(false)
        )
        assertThat(resultat.antattGodeMuligheter).isEqualTo(AntattGodeMuligheterEtikett.ANTATT_GODE_MULIGHETER)
    }

    companion object {
        val ALLE_FELTER = setOf(
            "dod", "diskresjonskode", "utmeldingskandidat", "sikkerhetstiltak", "skjerming",
            "vergemaalOppfolging", "fullmaktOppfolging", "tegnspraaktolk", "talespraaktolk", "kvp",
            "manuellOppfolging", "reservertKrr", "utdatertIKrr", "ikkeUnderOppfolging", "varsel",
            "ikkeRegistrertKrr", "trengerOppfolgingsvedtak", "sykmeldt", "iArbeidssokerregisteret",
            "antattGodeMuligheter", "antattBehovForVeiledning", "oppgittHindringer"
        )

        fun oppfolging() = OppfolgingEtikettgrunnlag(
            underOppfolging = true, manuell = false, reservasjonKrr = false,
            registrertKrr = true, kanVarsles = true, sykmeldt = false, utmeldingskandidat = null
        )
    }
}
