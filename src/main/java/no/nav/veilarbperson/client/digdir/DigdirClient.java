package no.nav.veilarbperson.client.digdir;

import lombok.SneakyThrows;
import no.nav.common.health.HealthCheck;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.config.CacheConfig;
import org.springframework.cache.annotation.Cacheable;

public interface DigdirClient extends HealthCheck {

    DigdirKontaktinfo hentKontaktInfo(Fnr fnr);

    @Cacheable(CacheConfig.DIFI_HAR_NIVA_4_CACHE_NAME)
    @SneakyThrows
    HarLoggetInnRespons harLoggetInnSiste18mnd(Fnr fnr);
}
