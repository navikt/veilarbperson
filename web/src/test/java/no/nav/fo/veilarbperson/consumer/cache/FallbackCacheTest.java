package no.nav.fo.veilarbperson.consumer.cache;

import no.nav.fo.veilarbperson.consumer.kodeverk.Kodeverk;
import no.nav.fo.veilarbperson.consumer.kodeverk.KodeverkImpl;
import no.nav.fo.veilarbperson.consumer.kodeverk.KodeverkService;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLEnkeltKodeverk;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.net.SocketTimeoutException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class FallbackCacheTest {

    @Test
    public void dummyvalueBeforeInitialFetchIsComplete() throws InterruptedException {
        Kodeverk kodeverk = new KodeverkImpl(new XMLEnkeltKodeverk());
        KodeverkService kodeverkServiceMock = mock(KodeverkService.class);
        when(kodeverkServiceMock.hentKodeverk(anyString())).then(delay(kodeverk, 100));

        FallbackCache<String, Kodeverk> klient = new FallbackCache<>(kodeverkServiceMock::hentKodeverk, new Kodeverk.KodeverkFallback());

        // Begge kallene skjer før vi får data
        Kodeverk kodeverk1 = klient.get("land");
        Kodeverk kodeverk2 = klient.get("land");

        // Vi venter på at porttype skal kalles
        Thread.sleep(200);

        assertThat(kodeverk1.getClass()).isEqualTo(Kodeverk.KodeverkFallback.class);
        assertThat(kodeverk2.getClass()).isEqualTo(Kodeverk.KodeverkFallback.class);
        verify(kodeverkServiceMock, times(1)).hentKodeverk(anyString());
    }

    @Test
    public void realValueWhenInitialFetchIsComplete() throws InterruptedException {
        Kodeverk kodeverk = new KodeverkImpl(new XMLEnkeltKodeverk());
        KodeverkService kodeverkServiceMock = mock(KodeverkService.class);
        when(kodeverkServiceMock.hentKodeverk(anyString())).then(delay(kodeverk, 100));
        FallbackCache<String, Kodeverk> klient = new FallbackCache<>(kodeverkServiceMock::hentKodeverk, new Kodeverk.KodeverkFallback());

        Kodeverk kodeverk1 = klient.get("land");

        Thread.sleep(200);
        Kodeverk kodeverk2 = klient.get("land");
        // Ekstra kall fører ikke til kall mot porttype
        klient.get("land");
        klient.get("land");

        assertThat(kodeverk1.getClass()).isEqualTo(Kodeverk.KodeverkFallback.class);
        assertThat(kodeverk2.getClass()).isEqualTo(KodeverkImpl.class);
        verify(kodeverkServiceMock, times(1)).hentKodeverk(anyString());
    }

    @Test
    public void fallbackIfInitialFetchFails() throws InterruptedException {
        KodeverkService kodeverkServiceMock = mock(KodeverkService.class);
        when(kodeverkServiceMock.hentKodeverk(anyString())).thenThrow(SocketTimeoutException.class);
        FallbackCache<String, Kodeverk> klient = new FallbackCache<>(kodeverkServiceMock::hentKodeverk, new Kodeverk.KodeverkFallback());

        Kodeverk kodeverk1 = klient.get("land");

        // Ekstra kall her fører ikke til kall mot PortType
        klient.get("land");
        klient.get("land");

        // Vi venter på at porttype skal kalles
        Thread.sleep(100);

        assertThat(kodeverk1.getClass()).isEqualTo(Kodeverk.KodeverkFallback.class);
        verify(kodeverkServiceMock, times(1)).hentKodeverk(anyString());
    }

    @Test
    public void recoverFromInitalFail() throws InterruptedException {
        KodeverkService kodeverkServiceMock = mock(KodeverkService.class);
        when(kodeverkServiceMock.hentKodeverk(anyString()))
                .thenThrow(SocketTimeoutException.class)
                .thenCallRealMethod();

        FallbackCache<String, Kodeverk> klient = new FallbackCache<>(kodeverkServiceMock::hentKodeverk, new Kodeverk.KodeverkFallback());

        // Initiell last (gir alltid fallback pga manglende data)
        Kodeverk kodeverk1 = klient.get("land");
        Thread.sleep(100);

        // Første request fikk Exception, så fortsatt fallback
        Kodeverk kodeverk2 = klient.get("land");
        klient.refresh("land");
        Thread.sleep(100);

        // Andre request funka, så nå får vi data
        Kodeverk kodeverk3 = klient.get("land");

        assertThat(kodeverk1.getClass()).isEqualTo(Kodeverk.KodeverkFallback.class);
        assertThat(kodeverk2.getClass()).isEqualTo(Kodeverk.KodeverkFallback.class);
        assertThat(kodeverk3.getClass()).isEqualTo(Kodeverk.class);
        assertThat(kodeverk1.getNavn("land", "NOR")).isEqualTo("NOR");
        assertThat(kodeverk2.getNavn("land", "NOR")).isEqualTo("NOR");
        assertThat(kodeverk3.getNavn("land", "NOR")).isEqualTo("Norge");
    }

    @Test
    public void retainDataIfRefreshFails() throws InterruptedException {
        KodeverkService kodeverkServiceMock = mock(KodeverkService.class);
        Kodeverk kodeverkMock = mock(Kodeverk.class);
        when(kodeverkServiceMock.hentKodeverk(anyString()))
                .thenCallRealMethod()
                .thenThrow(SocketTimeoutException.class);
        when(kodeverkMock.getNavn(anyString(), anyString())).thenReturn("Norge");

        FallbackCache<String, Kodeverk> klient = new FallbackCache<>(kodeverkServiceMock::hentKodeverk, new Kodeverk.KodeverkFallback());

        // Initiell last (gir alltid fallback pga manglende data)
        Kodeverk kodeverk1 = klient.get("land");
        Thread.sleep(100);

        // Første request fikk ok, så nå har vi data
        Kodeverk kodeverk2 = klient.get("land");
        // Refresh feiler
        klient.refresh("land");
        Thread.sleep(100);

        // Da refresh feiler så beholder vi data
        Kodeverk kodeverk3 = klient.get("land");
        Thread.sleep(1000);

        assertThat(kodeverk1.getClass()).isEqualTo(Kodeverk.KodeverkFallback.class);
        assertThat(kodeverk2.getClass()).isEqualTo(Kodeverk.class);
        assertThat(kodeverk3.getClass()).isEqualTo(Kodeverk.class);
        assertThat(kodeverk1.getNavn("land", "NOR")).isEqualTo("NOR");
        assertThat(kodeverk2.getNavn("land", "NOR")).isEqualTo("Norge");
        assertThat(kodeverk3.getNavn("land", "NOR")).isEqualTo("Norge");
    }

    @Test
    public void forceSingleRequest() throws InterruptedException {
        final int numberOfTries = 10;
        final CountDownLatch latch = new CountDownLatch(numberOfTries);
        final AtomicInteger currentActive = new AtomicInteger();
        final AtomicInteger startCount = new AtomicInteger(1);
        final AtomicInteger endCount = new AtomicInteger(0);
        final AtomicBoolean isOk = new AtomicBoolean(true);

        KodeverkService pt = new KodeverkService(mock(KodeverkPortType.class)) {
            @Override
            public Kodeverk hentKodeverk(String kodeverkRef) {
                int currentCount = currentActive.incrementAndGet();
                if (currentCount > 1) {
                    isOk.set(false);
                    startCount.set(currentCount);
                }

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                currentCount = currentActive.decrementAndGet();
                if (currentCount != 0) {
                    isOk.set(false);
                    endCount.set(currentCount);
                }
                latch.countDown();
                return null;
            }
        };

        FallbackCache<String, Kodeverk> klient = new FallbackCache<>(pt::hentKodeverk, new Kodeverk.KodeverkFallback());
        ForkJoinPool pool = new ForkJoinPool(10);
        for (int i = 0; i < numberOfTries; i++) {
            final String id = String.valueOf(i);
            pool.submit(() -> {
                klient.get(id);
            });
        }

        latch.await();

        assertThat(currentActive.get()).isEqualTo(0);
        assertThat(startCount.get()).isEqualTo(1);
        assertThat(endCount.get()).isEqualTo(0);
        assertThat(isOk.get()).isTrue();
    }

    @Test
    public void fallbackUntilFixIsOk() throws InterruptedException {
        KodeverkService kodeverkServiceMock = mock(KodeverkService.class);
        when(kodeverkServiceMock.hentKodeverk(anyString()))
                .thenCallRealMethod()
                .thenThrow(SocketTimeoutException.class)
                .thenCallRealMethod()
                .thenCallRealMethod();

        FallbackCache<String, Kodeverk> klient = new FallbackCache<>(kodeverkServiceMock::hentKodeverk, new Kodeverk.KodeverkFallback());

        // Warmup
        klient.get("kjonn");
        klient.get("applikasjon");
        klient.get("land");
        Thread.sleep(100);

        // First request
        Kodeverk kjonnKodeverk1 = klient.get("kjonn");
        klient.get("kjonn");
        klient.get("kjonn");
        Kodeverk applikasjonKodeverk1 = klient.get("applikasjon");
        Kodeverk applikasjonKodeverk2 = klient.get("applikasjon");
        Kodeverk applikasjonKodeverk3 = klient.get("applikasjon");
        Kodeverk landKodeverk1 = klient.get("land");
        klient.get("land");
        klient.get("land");

        assertThat(kjonnKodeverk1.getClass()).isEqualTo(Kodeverk.class);
        assertThat(applikasjonKodeverk1.getClass()).isEqualTo(Kodeverk.KodeverkFallback.class);
        assertThat(applikasjonKodeverk2.getClass()).isEqualTo(Kodeverk.KodeverkFallback.class);
        assertThat(applikasjonKodeverk3.getClass()).isEqualTo(Kodeverk.KodeverkFallback.class);
        assertThat(landKodeverk1.getClass()).isEqualTo(Kodeverk.class);

        klient.fix();
        Thread.sleep(100);

        Kodeverk applikasjonKodeverk4 = klient.get("applikasjon");
        assertThat(applikasjonKodeverk4.getClass()).isEqualTo(Kodeverk.class);
        verify(kodeverkServiceMock, times(4)).hentKodeverk(anyString());
    }

    private Answer<Kodeverk> delay(final Kodeverk kodeverk, final long delay) {
        return (invocationOnMock) -> {
            Thread.sleep(delay);
            return kodeverk;
        };
    }
}