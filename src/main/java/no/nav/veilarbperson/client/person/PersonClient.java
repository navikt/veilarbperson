package no.nav.veilarbperson.client.person;

import no.nav.common.health.HealthCheck;
import no.nav.veilarbperson.client.person.domain.PersonData;

public interface PersonClient extends HealthCheck {

    PersonData hentPersonData(String ident);

    String hentSikkerhetstiltak(String ident);

}
