package no.nav.veilarbperson.client.representasjon

import java.io.IOException

interface RepresentasjonClient {
    @Throws(IOException::class)
    fun hentFullmakt(personIdent: String?): List<ReprFullmaktV2Data>
}

internal class PersonIdentDTO(val ident: String?)
