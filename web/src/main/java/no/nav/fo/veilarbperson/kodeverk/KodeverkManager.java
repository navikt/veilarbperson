package no.nav.fo.veilarbperson.kodeverk;

import no.nav.tjeneste.virksomhet.kodeverk.v2.HentKodeverkHentKodeverkKodeverkIkkeFunnet;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.XMLHentKodeverkRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class KodeverkManager {

    private static final String NORSK_SPRAK = "nb";

    @Autowired
    private KodeverkService kodeverkService;

    public Optional<String> getBeskrivelseForLandkode(String kode) {
        return getBeskrivelseForKode("Landkoder", kode, NORSK_SPRAK);
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
