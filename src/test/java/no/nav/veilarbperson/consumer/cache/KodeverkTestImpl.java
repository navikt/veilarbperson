package no.nav.veilarbperson.consumer.cache;

import no.nav.veilarbperson.client.kodeverk.Kodeverk;

public class KodeverkTestImpl implements Kodeverk {
    @Override
    public String getNavn(String kode, String sprak) {
        if ("NOR".equals(kode)) {
            return "Norge";
        }
        return kode;
    }
}
