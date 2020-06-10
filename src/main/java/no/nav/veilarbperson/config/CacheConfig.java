package no.nav.veilarbperson.config;


import net.sf.ehcache.config.CacheConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;

import static net.sf.ehcache.store.MemoryStoreEvictionPolicy.LRU;
import static no.nav.dialogarena.aktor.AktorConfig.AKTOR_ID_FROM_FNR_CACHE;
import static no.nav.sbl.dialogarena.common.abac.pep.context.AbacContext.ABAC_CACHE;

@EnableCaching
public class CacheConfig {

    public static final String ENHET = "enhet";
    public static final String PERSON = "person";
    public static final String GEOGRAFISK_TILKNYTNING = "geografiskTilknytning";
    public static final String SIKKERHETSTILTAK = "sikkerhetstiltak";
    public static final String DIGITAL_KONTAKTINFO = "digital_kontaktinfo";
    public static final String EGEN_ANSATT = "egen_ansatt";

    private static final CacheConfiguration ENHET_CACHE = langCache(ENHET);

    private static final CacheConfiguration PERSON_CACHE = kortCache(PERSON);
    private static final CacheConfiguration SIKKERHETSTILTAK_CACHE = kortCache(SIKKERHETSTILTAK);
    private static final CacheConfiguration DIGITAL_KONTAKTINFO_CACHE = kortCache(DIGITAL_KONTAKTINFO);
    private static final CacheConfiguration EGEN_ANSATT_CACHE = kortCache(EGEN_ANSATT);
    private static final CacheConfiguration GEOGRAFISK_TILKNYTNING_CACHE = kortCache(GEOGRAFISK_TILKNYTNING);

    @Bean
    public CacheManager cacheManager() {
        net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();
        config.addCache(ABAC_CACHE);
        config.addCache(PERSON_CACHE);
        config.addCache(SIKKERHETSTILTAK_CACHE);
        config.addCache(ENHET_CACHE);
        config.addCache(DIGITAL_KONTAKTINFO_CACHE);
        config.addCache(EGEN_ANSATT_CACHE);
        config.addCache(GEOGRAFISK_TILKNYTNING_CACHE);
        config.addCache(AKTOR_ID_FROM_FNR_CACHE);
        return new EhCacheCacheManager(net.sf.ehcache.CacheManager.newInstance(config));
    }

    private static CacheConfiguration langCache(String navn) {
        return cache(navn, 86400);
    }

    private static CacheConfiguration kortCache(String navn) {
        return cache(navn, 60);
    }

    private static CacheConfiguration cache(String navn, int varighetSekunder) {
        return new CacheConfiguration(navn, 100)
                .memoryStoreEvictionPolicy(LRU)
                .timeToIdleSeconds(varighetSekunder)
                .timeToLiveSeconds(varighetSekunder);
    }

}

