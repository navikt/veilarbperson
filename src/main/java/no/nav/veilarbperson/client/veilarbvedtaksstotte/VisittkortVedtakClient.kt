package no.nav.veilarbperson.client.veilarbvedtaksstotte

import no.nav.common.types.identer.Fnr

fun interface VisittkortVedtakClient {
    fun harGjeldendeVedtak(fnr: Fnr): Boolean
}
