package no.nav.fo.veilarbperson.consumer.kodeverk;

import lombok.extern.slf4j.Slf4j;
import no.nav.fo.veilarbperson.consumer.cache.FallbackCache;
import no.nav.tjeneste.virksomhet.kodeverk.v2.HentKodeverkHentKodeverkKodeverkIkkeFunnet;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLEnkeltKodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLKodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.XMLHentKodeverkRequest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class KodeverkService {
    private final FallbackCache<String, Kodeverk> klient;
    private final KodeverkPortType kodeverkPortType;
    final ConcurrentHashMap<String, CompletableFuture<Kodeverk>> aktiveJobber = new ConcurrentHashMap<>();

    public KodeverkService(KodeverkPortType kodeverkPortType) {
        this.kodeverkPortType = kodeverkPortType;
        this.klient = new FallbackCache<>(this::hentKodeverkFraPortType, new Kodeverk.KodeverkFallback());
    }

    public String getVerdi(String kodeverkRef, String kode, String sprak) {
        return hentKodeverk(kodeverkRef)
                .getNavn(kode, sprak);
    }

    void refreshKodeverk(String kodeverkRef) {
        klient.refresh(kodeverkRef);
    }

    public Kodeverk hentKodeverk(String kodeverkRef) {
        return this.klient.get(kodeverkRef);
    }


    private Kodeverk hentKodeverkFraPortType(String kodeverkRef) throws HentKodeverkHentKodeverkKodeverkIkkeFunnet {
        XMLHentKodeverkRequest request = new XMLHentKodeverkRequest().withNavn(kodeverkRef);
        XMLKodeverk kodeverk = kodeverkPortType.hentKodeverk(request).getKodeverk();

        if (kodeverk instanceof XMLEnkeltKodeverk) {
            return new KodeverkImpl((XMLEnkeltKodeverk) kodeverk);
        } else {
            throw new RuntimeException("Kodeverk er ikke av typen XMLEnkeltKodeverk og er ikke støttet: " + kodeverkRef);
        }
    }
}
