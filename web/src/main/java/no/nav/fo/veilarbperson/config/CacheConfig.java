package no.nav.fo.veilarbperson.config;


import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;

import javax.cache.spi.CachingProvider;
import java.net.URISyntaxException;

@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() throws URISyntaxException {
        CachingProvider cachingProvider = javax.cache.Caching.getCachingProvider();
        javax.cache.CacheManager cacheManager = cachingProvider.getCacheManager(
                getClass().getResource("/cache/ehcache.xml").toURI(),
                cachingProvider.getDefaultClassLoader());

        return new JCacheCacheManager(cacheManager);
    }

}

