package no.nav.veilarbperson.client.person;

import no.nav.common.health.HealthCheck;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.domain.KontoregisterResponseDTO;

import java.util.Optional;

public interface KontoregisterClient extends HealthCheck {

   Optional<KontoregisterResponseDTO> hentKontonummer(Fnr kontohaver);
}