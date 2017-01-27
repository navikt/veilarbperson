package no.nav.fo.veilarbperson.consumer.kodeverk;

import no.nav.tjeneste.virksomhet.kodeverk.v2.HentKodeverkHentKodeverkKodeverkIkkeFunnet;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.XMLHentKodeverkRequest;

import java.util.Optional;

public class KodeverkManager {

    private static final String NORSK_SPRAK = "nb";

    private final KodeverkService kodeverkService;

    public KodeverkManager(KodeverkService kodeverkService) {
        this.kodeverkService = kodeverkService;
    }

    public Optional<String> getBeskrivelseForLandkode(String kode) {
        return getBeskrivelseForKode("Landkoder", kode, NORSK_SPRAK);
    }

    public Optional<String> getBeskrivelseForSivilstand(String kode) {
        return getBeskrivelseForKode("Sivilstander", kode, NORSK_SPRAK);
    }


    public Optional<String> getPoststed(String postnummer) {
        return getBeskrivelseForKode("Postnummer", postnummer, NORSK_SPRAK);
    }

    private Optional<String> getBeskrivelseForKode(String kodeverkRef, String kode, String spraak) {
        XMLHentKodeverkRequest kodeverkRequest = new XMLHentKodeverkRequest()
                .withNavn(kodeverkRef);
        try {
            Kodeverk kodeverk = kodeverkService.hentKodeverk(kodeverkRequest);
            return kodeverk.getNavn(kode, spraak);
        } catch (HentKodeverkHentKodeverkKodeverkIkkeFunnet hentKodeverkHentKodeverkKodeverkIkkeFunnet) {
            return Optional.empty();
        }
    }

}
