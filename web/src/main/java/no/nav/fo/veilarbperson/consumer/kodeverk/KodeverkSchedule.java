package no.nav.fo.veilarbperson.consumer.kodeverk;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class KodeverkSchedule {

    private final KodeverkFetcher kodeverkFetcher;

    public KodeverkSchedule(KodeverkFetcher kodeverkFetcher) {
        this.kodeverkFetcher = kodeverkFetcher;
    }

    @Scheduled(cron = "0 0 6 * * *")
    public void refreshKodeverk() {
        kodeverkFetcher.refreshKodeverk("Landkoder");
        kodeverkFetcher.refreshKodeverk("Sivilstander");
        kodeverkFetcher.refreshKodeverk("Postnummer");
    }

    @Scheduled(cron = "0 */10 * * * *")
    public void hentDataVedFeil() {
        kodeverkFetcher.aktiveJobber.entrySet()
                .stream()
                .filter((entry) -> entry.getValue().isCompletedExceptionally())
                .forEach((entry) -> kodeverkFetcher.refreshKodeverk(entry.getKey()));
    }


    @PostConstruct
    public void lastKodeverk() {
        kodeverkFetcher.hentKodeverk("Landkoder");
        kodeverkFetcher.hentKodeverk("Sivilstander");
        kodeverkFetcher.hentKodeverk("Postnummer");
    }

}
