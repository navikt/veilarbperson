package no.nav.veilarbperson.client.representasjon

import no.nav.common.types.identer.Fnr
import java.io.IOException

interface RepresentasjonClient {
    @Throws(IOException::class)
    fun hentFullmakt(personIdent: String?): List<ReprFullmaktV2Data>

    fun hentOppfolgingsrepresentasjon(fnr: Fnr): VisittkortRepresentasjonData
}

internal class PersonIdentDTO(val ident: String?)

data class VisittkortRepresentasjonData(val vergemaalOppfolging: Boolean, val fullmaktOppfolging: Boolean)
