package no.nav.fo.veilarbperson.consumer.kodeverk;

import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLEnkeltKodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLKodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.XMLHentKodeverkRequest;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@Slf4j
public class KodeverkFetcher {
    private final KodeverkPortType kodeverkPortType;
    final ConcurrentHashMap<String, CompletableFuture<Optional<Kodeverk>>> aktiveJobber = new ConcurrentHashMap<>();
    private ForkJoinPool pool = new ForkJoinPool(1);

    public KodeverkFetcher(KodeverkPortType kodeverkPortType) {
        this.kodeverkPortType = kodeverkPortType;
    }

    public Optional<Kodeverk> hentKodeverk(String kodeverkRef) {
        CompletableFuture<Optional<Kodeverk>> task = aktiveJobber.computeIfAbsent(kodeverkRef, (nyKodeverkRef) -> {
            return CompletableFuture.supplyAsync(lagFetcher(nyKodeverkRef), pool).handle(settRiktigTaskStatus());
        });

        if (task.isCompletedExceptionally()) {
            return Optional.empty();
        }
        return task.getNow(Optional.empty());
    }

    public String getVerdi(String kodeverkRef, String kode, String sprak) {
        return hentKodeverk(kodeverkRef)
                .flatMap((kodeverk) -> kodeverk.getNavn(kode, sprak))
                .orElse(kode);
    }

    public void refreshKodeverk(String kodeverkRef) {
        CompletableFuture<Optional<Kodeverk>> task = CompletableFuture.supplyAsync(lagFetcher(kodeverkRef), pool)
                .handle(settRiktigTaskStatus());

        task.handle((maybeKodeverk, exception) -> {
            aktiveJobber.put(kodeverkRef, task);
            return maybeKodeverk;
        });
    }

    private Supplier<Optional<Kodeverk>> lagFetcher(String kodeverkRef) {
        return () -> {
            XMLHentKodeverkRequest request = new XMLHentKodeverkRequest();
            request.setNavn(kodeverkRef);

            try {
                XMLKodeverk kodeverk = kodeverkPortType.hentKodeverk(request).getKodeverk();
                if (kodeverk instanceof XMLEnkeltKodeverk) {
                    return Optional.of(new Kodeverk((XMLEnkeltKodeverk) kodeverk));
                } else {
                    log.warn("Kodeverk er ikke av typen XMLEnkeltKodeverk og er ikke støttet: " + kodeverkRef);
                    return Optional.empty();
                }
            } catch (Exception e) {
                log.error("Feil ved henting av kodeverk", e);
                return Optional.empty();
            }
        };
    }

    private BiFunction<Optional<Kodeverk>, Throwable, Optional<Kodeverk>> settRiktigTaskStatus() {
        return (maybeNyttKodeverk, exception) -> {
            if (exception != null) {
                throw new RuntimeException(exception);
            } else if (!maybeNyttKodeverk.isPresent()) {
                throw new RuntimeException("Feilet ved oppdatering av kodeverk");
            }
            return maybeNyttKodeverk;
        };
    }
}
