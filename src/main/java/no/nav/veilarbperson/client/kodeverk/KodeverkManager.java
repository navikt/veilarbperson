package no.nav.veilarbperson.client.kodeverk;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KodeverkManager {

    private static final String NORSK_SPRAK = "nb";

    private final KodeverkService kodeverkService;

    public KodeverkManager(KodeverkService kodeverkService) {
        this.kodeverkService = kodeverkService;
    }

    public String getBeskrivelseForLandkode(String kode) {
        return getBeskrivelseForKode("Landkoder", kode, NORSK_SPRAK);
    }

    public String getBeskrivelseForSivilstand(String kode) {
        return getBeskrivelseForKode("Sivilstander", kode, NORSK_SPRAK);
    }


    public String getPoststed(String postnummer) {
        return getBeskrivelseForKode("Postnummer", postnummer, NORSK_SPRAK);
    }

    private String getBeskrivelseForKode(String kodeverkRef, String kode, String spraak) {
        return kodeverkService.getVerdi(kodeverkRef, kode, spraak);
    }
}
