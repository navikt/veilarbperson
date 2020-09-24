package no.nav.veilarbperson.client.veilarbportefolje;

import no.nav.common.health.HealthCheck;
import no.nav.common.types.identer.Fnr;

public interface VeilarbportefoljeClient extends HealthCheck {

    Personinfo hentPersonInfo(Fnr fodselsnummer);

}
