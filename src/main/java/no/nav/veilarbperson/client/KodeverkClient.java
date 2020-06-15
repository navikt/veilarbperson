package no.nav.veilarbperson.client;

import no.nav.veilarbperson.client.kodeverk.Kodeverk;

public interface KodeverkClient {

    String getVerdi(String kodeverkRef, String kode, String sprak);

    Kodeverk hentKodeverk(String kodeverkRef);

}
