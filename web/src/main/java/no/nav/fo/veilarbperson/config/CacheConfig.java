package no.nav.fo.veilarbperson.config;


import net.sf.ehcache.config.CacheConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;

import static net.sf.ehcache.store.MemoryStoreEvictionPolicy.LRU;
import static no.nav.sbl.dialogarena.common.abac.pep.context.AbacContext.ABAC_CACHE;

@EnableCaching
public class CacheConfig {

    public static final String KODEVERK = "kodeverk";
    private static final CacheConfiguration KODEVERK_CACHE = new CacheConfiguration(KODEVERK, 100)
            .memoryStoreEvictionPolicy(LRU)
            .timeToIdleSeconds(86400)
            .timeToLiveSeconds(86400);

    @Bean
    public CacheManager cacheManager() {
        net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();
        config.addCache(ABAC_CACHE);
        config.addCache(KODEVERK_CACHE);
        return new EhCacheCacheManager(net.sf.ehcache.CacheManager.newInstance(config));
    }
}

