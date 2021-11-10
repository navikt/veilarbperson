package no.nav.veilarbperson.client.nom;

import no.nav.common.health.HealthCheck;
import no.nav.common.types.identer.Fnr;

public interface SkjermetClient extends HealthCheck {

    Boolean hentSkjermet(Fnr fodselsnummer);
}
