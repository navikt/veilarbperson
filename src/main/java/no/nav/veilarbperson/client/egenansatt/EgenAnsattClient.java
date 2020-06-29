package no.nav.veilarbperson.client.egenansatt;

import no.nav.common.health.HealthCheck;

public interface EgenAnsattClient extends HealthCheck {

    boolean erEgenAnsatt(String ident);

}
