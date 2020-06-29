package no.nav.veilarbperson.client.person;

import no.nav.common.health.HealthCheck;
import no.nav.veilarbperson.client.person.domain.TpsPerson;

public interface PersonClient extends HealthCheck {

    TpsPerson hentPerson(String ident);

    String hentSikkerhetstiltak(String ident);

}
