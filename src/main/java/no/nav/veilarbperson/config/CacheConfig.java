package no.nav.veilarbperson.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String VEILARBOPPFOLGING_UNDER_OPPFOLGING_CACHE_NAME = "veilarboppfolging_underoppfolging_cache";
    public static final String NOM_SKJERMEDE_PERSONER_CACHE_NAME = "nom_skjermede_personer_cache";
    public static final String TPS_PERSON_CACHE_NAME = "tps_person_cache";
    public static final String SIKKERHETSTILTAK_CACHE_NAME = "sikkerhetstiltak_cache";
    public static final String DIGDIR_KONTAKTINFO_CACHE_NAME = "digdir_kontaktinfo_cache";
    public static final String DIFI_HAR_NIVA_4_CACHE_NAME = "difi_har_niva_4_cache";
    public static final String KODEVERK_BETYDNING_CACHE_NAME = "kodeverk_betydning_cache";

    public static final String KONTOREGISTER_CACHE_NAME = "kontoregister_person_cache";

    @Bean
    public Cache veilarboppfolgingUnderOppfolgingCache() {
        return litenCache(VEILARBOPPFOLGING_UNDER_OPPFOLGING_CACHE_NAME);
    }

    @Bean
    public Cache nomskjermedepersonerCache() {
        return new CaffeineCache(NOM_SKJERMEDE_PERSONER_CACHE_NAME, Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(500_000)
                .build());
    }

    @Bean
    public Cache tpsPersonCache() {
        return litenCache(TPS_PERSON_CACHE_NAME);
    }

    @Bean
    public Cache sikkerhetstiltakCache() {
        return litenCache(SIKKERHETSTILTAK_CACHE_NAME);
    }

    @Bean
    public Cache digdirKontaktinfoCache() {
        return litenCache(DIGDIR_KONTAKTINFO_CACHE_NAME);
    }

    @Bean
    public Cache difiHarNiva4Cache() {
        return litenCache(DIFI_HAR_NIVA_4_CACHE_NAME);
    }

    @Bean
    public Cache kodeverkBetydningCache() {
        return new CaffeineCache(KODEVERK_BETYDNING_CACHE_NAME, Caffeine.newBuilder()
                .expireAfterWrite(12, TimeUnit.HOURS)
                .maximumSize(3)
                .build());
    }

    private CaffeineCache litenCache(String cacheName) {
        return new CaffeineCache(cacheName, Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .maximumSize(15_000)
                .build());
    }

    @Bean
    public Cache kontoregisterPersonCache() {
        return litenCache(KONTOREGISTER_CACHE_NAME);
    }

}
