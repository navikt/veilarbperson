package no.nav.veilarbperson.client;

import no.nav.common.health.HealthCheck;
import no.nav.veilarbperson.domain.DkifKontaktinfo;

// https://dkif.nais.preprod.local/api/v1/personer/kontaktinformasjon?inkluderSikkerDigitalPost=false
public interface DkifClient extends HealthCheck {

    DkifKontaktinfo hentKontaktInfo(String fnr);

}
