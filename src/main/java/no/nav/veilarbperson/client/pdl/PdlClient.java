package no.nav.veilarbperson.client.pdl;

import no.nav.common.health.HealthCheck;

public interface PdlClient extends HealthCheck {

    HentPersonData.PdlPerson hentPerson(String personIdent, String userToken);

}
