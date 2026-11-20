package no.nav.veilarbperson.client.veilarbdialog

import no.nav.common.types.identer.Fnr

fun interface VisittkortVarselClient {
    fun harGjeldendeEskaleringsvarsel(fnr: Fnr): Boolean
}
