package no.nav.veilarbperson.consumer.kodeverk;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class KodeverkSchedule {

    private final KodeverkService kodeverkService;

    public KodeverkSchedule(KodeverkService kodeverkService) {
        this.kodeverkService = kodeverkService;
        lastKodeverk();
    }

    @Scheduled(cron = "0 0 6 * * *")
    public void refreshKodeverk() {
        kodeverkService.refreshKodeverk("Landkoder");
        kodeverkService.refreshKodeverk("Sivilstander");
        kodeverkService.refreshKodeverk("Postnummer");
    }

    @Scheduled(cron = "0 */10 * * * *")
    public void hentDataVedFeil() {
        kodeverkService.aktiveJobber.entrySet()
                .stream()
                .filter((entry) -> entry.getValue().isCompletedExceptionally())
                .forEach((entry) -> kodeverkService.refreshKodeverk(entry.getKey()));
    }


    private void lastKodeverk() {
        kodeverkService.hentKodeverk("Landkoder");
        kodeverkService.hentKodeverk("Sivilstander");
        kodeverkService.hentKodeverk("Postnummer");
    }

}
