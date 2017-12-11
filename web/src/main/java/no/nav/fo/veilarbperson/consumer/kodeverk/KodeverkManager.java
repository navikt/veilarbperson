package no.nav.fo.veilarbperson.consumer.kodeverk;

import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.kodeverk.v2.HentKodeverkHentKodeverkKodeverkIkkeFunnet;
import org.springframework.remoting.soap.SoapFaultException;

import java.util.Optional;

@Slf4j
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
        KodeverkRequestDO kodeverkRequest = new KodeverkRequestDO();
        kodeverkRequest.setNavn(kodeverkRef);

        try {
            Kodeverk kodeverk = kodeverkService.hentKodeverk(kodeverkRequest);
            return kodeverk.getNavn(kode, spraak);
        } catch (HentKodeverkHentKodeverkKodeverkIkkeFunnet hentKodeverkHentKodeverkKodeverkIkkeFunnet) {
            return Optional.empty();
        } catch (SoapFaultException ukjentFeilHosKodeverk) {
            log.error("Ukjent feil fra kodeverk: ", ukjentFeilHosKodeverk);
            return Optional.of(kode);
        }
    }

}
