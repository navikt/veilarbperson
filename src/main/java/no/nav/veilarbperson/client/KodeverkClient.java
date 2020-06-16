package no.nav.veilarbperson.client;

import no.nav.common.health.HealthCheck;

public interface KodeverkClient extends HealthCheck {

    String getBeskrivelseForLandkode(String kode);

    String getBeskrivelseForSivilstand(String kode);

    String getPoststed(String postnummer);

}
