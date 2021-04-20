package no.nav.veilarbperson.client.pam;

import no.nav.common.health.HealthCheck;
import no.nav.common.types.identer.Fnr;
import okhttp3.Response;

public interface PamClient extends HealthCheck {

    Response hentCvOgJobbprofil(Fnr fnr, boolean erBrukerManuell);

}
