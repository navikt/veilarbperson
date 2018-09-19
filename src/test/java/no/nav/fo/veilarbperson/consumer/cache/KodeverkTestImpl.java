package no.nav.fo.veilarbperson.consumer.cache;

import no.nav.fo.veilarbperson.consumer.kodeverk.Kodeverk;

public class KodeverkTestImpl implements Kodeverk {
    @Override
    public String getNavn(String kode, String sprak) {
        if ("NOR".equals(kode)) {
            return "Norge";
        }
        return kode;
    }
}
