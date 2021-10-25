package no.nav.veilarbperson.client.veilarbregistrering;

import no.nav.common.health.HealthCheck;
import no.nav.common.types.identer.Fnr;
import okhttp3.Response;

public interface VeilarbregistreringClient  extends HealthCheck {

    Response hentRegistrering(Fnr fnr);
}
