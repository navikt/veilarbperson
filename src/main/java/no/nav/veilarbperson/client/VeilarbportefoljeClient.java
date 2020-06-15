package no.nav.veilarbperson.client;

import no.nav.common.health.HealthCheck;
import no.nav.veilarbperson.domain.Personinfo;

public interface VeilarbportefoljeClient extends HealthCheck {

    Personinfo hentPersonInfo(String fodselsnummer);

}
