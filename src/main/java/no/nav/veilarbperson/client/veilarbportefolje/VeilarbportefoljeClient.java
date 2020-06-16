package no.nav.veilarbperson.client.veilarbportefolje;

import no.nav.common.health.HealthCheck;

public interface VeilarbportefoljeClient extends HealthCheck {

    Personinfo hentPersonInfo(String fodselsnummer);

}
