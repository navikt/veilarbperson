package no.nav.veilarbperson.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

@Slf4j
public class FallbackCache<REQUESTTYPE, DATATYPE> {
    private final Fetcher<REQUESTTYPE, DATATYPE> fetcher;
    private final DATATYPE fallback;
    final ConcurrentHashMap<REQUESTTYPE, CompletableFuture<DATATYPE>> cache = new ConcurrentHashMap<>();
    private ForkJoinPool executorPool = new ForkJoinPool(1);

    public FallbackCache(Fetcher<REQUESTTYPE, DATATYPE> fetcher, DATATYPE fallback) {
        this.fetcher = fetcher;
        this.fallback = fallback;
    }

    public DATATYPE get(REQUESTTYPE request) {
        CompletableFuture<DATATYPE> data = cache.computeIfAbsent(request, this::getFromFetcher);

        try {
            return data.getNow(fallback);
        } catch (CompletionException e) {
            log.warn("Data completed exceptionally, falling back to default: ", e);
            return fallback;
        }
    }

    public void refresh(REQUESTTYPE request) {
        final CompletableFuture<DATATYPE> newData = getFromFetcher(request);

        newData.thenRun(() -> {
            cache.put(request, newData);
        });
    }

    private CompletableFuture<DATATYPE> getFromFetcher(final REQUESTTYPE request) {
        CompletableFuture<DATATYPE> data = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            try {
                data.complete(fetcher.fetch(request));
            } catch (Exception e) {
                data.completeExceptionally(e);
            }
        }, executorPool);

        return data;
    }

    public interface Fetcher<REQUESTTYPE, DATATYPE> {
        DATATYPE fetch(REQUESTTYPE request) throws Exception;
    }
}
