package no.nav.veilarbperson.client.kontoregister;

import no.nav.common.health.HealthCheck;
import no.nav.common.types.identer.Fnr;

public interface KontoregisterClient extends HealthCheck {

   HentKontoResponseDTO hentKontonummer(Fnr kontohaver);

}