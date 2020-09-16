package no.nav.veilarbperson.client.egenansatt;

import no.nav.common.health.HealthCheck;
import no.nav.common.types.identer.Fnr;

public interface EgenAnsattClient extends HealthCheck {

    boolean erEgenAnsatt(Fnr fnr);

}
