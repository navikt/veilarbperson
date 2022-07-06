package no.nav.veilarbperson.client.person;

import no.nav.common.health.HealthCheck;
import no.nav.common.types.identer.Fnr;

public interface PersonClient extends HealthCheck {
    TpsPerson hentPerson(Fnr fnr);
}
