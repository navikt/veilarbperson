package no.nav.veilarbperson.service

import io.micrometer.core.instrument.MeterRegistry
import no.nav.common.types.identer.Fnr
import no.nav.veilarbperson.client.VisittkortKildeIkkeKonfigurertException
import no.nav.veilarbperson.client.VisittkortKildeUtilgjengeligException
import no.nav.veilarbperson.client.nom.SkjermetClient
import no.nav.veilarbperson.client.pdl.PdlClient
import no.nav.veilarbperson.client.pdl.domain.PdlRequest
import no.nav.veilarbperson.client.representasjon.RepresentasjonClient
import no.nav.veilarbperson.client.veilarbdialog.VisittkortVarselClient
import no.nav.veilarbperson.client.veilarboppfolging.VeilarboppfolgingClient
import no.nav.veilarbperson.client.veilarbvedtaksstotte.VisittkortVedtakClient
import no.nav.veilarbperson.domain.visittkort.VisittkortEtiketter
import no.nav.veilarbperson.domain.visittkort.VisittkortKildeResultat
import no.nav.veilarbperson.domain.visittkort.VisittkortKildeResultat.Hentet
import no.nav.veilarbperson.domain.visittkort.VisittkortKildeResultat.Ukjent
import no.nav.veilarbperson.utils.VisittkortEtiketterMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.IOException
import java.util.concurrent.TimeUnit

@Service
class VisittkortEtiketterService(
    private val pdlClient: PdlClient,
    private val representasjonClient: RepresentasjonClient,
    private val skjermetClient: SkjermetClient,
    private val oppfolgingClient: VeilarboppfolgingClient,
    private val arbeidssokerService: OppslagArbeidssoekerregisteretService,
    private val vedtakClient: VisittkortVedtakClient?,
    private val varselClient: VisittkortVarselClient?,
    private val meterRegistry: MeterRegistry
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun hent(fnr: Fnr, behandlingsnummer: String?, felter: Set<String>): VisittkortEtiketter {
        val start = System.nanoTime()
        return try {
            hentEtiketter(fnr, behandlingsnummer, felter)
        } finally {
            meterRegistry.timer("visittkort_etiketter_varighet")
                .record(System.nanoTime() - start, TimeUnit.NANOSECONDS)
        }
    }

    private fun hentEtiketter(fnr: Fnr, behandlingsnummer: String?, felter: Set<String>): VisittkortEtiketter {
        val trengerProfilering = felter.any { it in PROFILERING_FELTER }
        val trengerVedtaksbehov = "trengerOppfolgingsvedtak" in felter
        val oppfolgingsfelter = felter.intersect(
            OPPFOLGING_FELTER + PROFILERING_FELTER + "trengerOppfolgingsvedtak"
        )
        val varselKilde = if ("varsel" in felter) krevKonfigurert(varselClient) else null
        val pdl = if (felter.any { it in PDL_FELTER }) {
            val nummer = requireNotNull(behandlingsnummer) { "Behandlingsnummer mangler" }
            kilde("pdl") { pdlClient.hentVisittkortEtiketter(PdlRequest(fnr, nummer), felter.intersect(PDL_FELTER)) }
        } else null
        val representasjon = if (felter.any { it in REPRESENTASJON_FELTER }) {
            kilde("representasjon") { representasjonClient.hentOppfolgingsrepresentasjon(fnr) }
        } else null
        val skjerming = if ("skjerming" in felter) {
            kilde("skjerming") { requireNotNull(skjermetClient.hentSkjermet(fnr)) }
        } else null
        val oppfolging = if (oppfolgingsfelter.isNotEmpty()) {
            kilde("oppfolging") { oppfolgingClient.hentEtikettgrunnlag(fnr, oppfolgingsfelter) }
        } else null
        val kvp = if ("kvp" in felter) {
            when (oppfolging) {
                is Hentet -> Hentet(checkNotNull(oppfolging.data.kvp) { "Felt oppfolging mangler" })
                Ukjent -> Ukjent
                null -> error("Mangler påkrevd kilde")
            }
        } else null
        val vedtak = if (trengerProfilering ||
            (trengerVedtaksbehov && (oppfolging !is Hentet ||
                checkNotNull(oppfolging.data.underOppfolging) { "Felt oppfolging mangler" }))) {
            kilde("vedtak") { krevKonfigurert(vedtakClient).harGjeldendeVedtak(fnr) }
        } else null
        val hentProfilering = trengerProfilering && !(vedtak is Hentet && vedtak.data)
        val arbeidssoker = if ("iArbeidssokerregisteret" in felter || hentProfilering) {
            kilde("arbeidssokerregister") { arbeidssokerService.hentVisittkortData(fnr, hentProfilering) }
        } else null
        val varsel = varselKilde?.let { kilde("dialog") { it.harGjeldendeEskaleringsvarsel(fnr) } }

        return VisittkortEtiketterMapper.map(
            felter, pdl, representasjon, skjerming, oppfolging, arbeidssoker, vedtak, varsel, kvp
        ).also { resultat ->
            resultat.ukjenteFelter().forEach {
                meterRegistry.counter("visittkort_etikett_ukjent", "etikett", it).increment()
            }
        }
    }

    private fun <T : Any> krevKonfigurert(client: T?): T =
        client ?: throw VisittkortKildeIkkeKonfigurertException()

    private fun <T> kilde(navn: String, oppslag: () -> T): VisittkortKildeResultat<T> {
        val start = System.nanoTime()
        return try {
            Hentet(oppslag())
        } catch (_: IOException) {
            utilgjengelig(navn)
        } catch (_: VisittkortKildeUtilgjengeligException) {
            utilgjengelig(navn)
        } finally {
            meterRegistry.timer("visittkort_kilde_varighet", "kilde", navn)
                .record(System.nanoTime() - start, TimeUnit.NANOSECONDS)
        }
    }

    private fun utilgjengelig(kilde: String): VisittkortKildeResultat<Nothing> {
        log.warn("Visittkort kilde(r) utilgjengelig: {}", kilde)
        meterRegistry.counter("visittkort_kilde_utilgjengelig", "kilde", kilde).increment()
        return Ukjent
    }

    companion object {
        val PDL_FELTER = setOf("dod", "diskresjonskode", "sikkerhetstiltak", "tegnspraaktolk", "talespraaktolk")
        val REPRESENTASJON_FELTER = setOf("vergemaalOppfolging", "fullmaktOppfolging")
        val PROFILERING_FELTER = setOf("antattGodeMuligheter", "antattBehovForVeiledning", "oppgittHindringer")
        val OPPFOLGING_FELTER = setOf(
            "kvp", "utmeldingskandidat", "manuellOppfolging", "reservertKrr", "utdatertIKrr",
            "ikkeUnderOppfolging", "ikkeRegistrertKrr", "sykmeldt"
        )
    }
}
