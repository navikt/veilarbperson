package no.nav.veilarbperson.client.pam;

import no.nav.common.health.HealthCheck;
import no.nav.common.types.identer.Fnr;

public interface PamClient extends HealthCheck {

    String hentCvOgJobbprofilJson(Fnr fnr);

}
