package no.nav.veilarbperson.client;

import no.nav.common.health.HealthCheck;
import no.nav.veilarbperson.client.kodeverk.Kodeverk;

public interface KodeverkClient extends HealthCheck {

    String getVerdi(String kodeverkRef, String kode, String sprak);

    Kodeverk hentKodeverk(String kodeverkRef);

    String getBeskrivelseForLandkode(String kode);

    String getBeskrivelseForSivilstand(String kode);

    String getPoststed(String postnummer);

}
