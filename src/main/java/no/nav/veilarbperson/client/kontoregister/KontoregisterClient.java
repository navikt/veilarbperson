package no.nav.veilarbperson.client.kontoregister;

import no.nav.common.health.HealthCheck;

public interface KontoregisterClient extends HealthCheck {

   HentKontoResponseDTO hentKontonummer(HentKontoRequestDTO kontohaver);

}