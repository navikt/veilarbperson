package no.nav.veilarbperson.client.dkif;

import no.nav.common.health.HealthCheck;

// https://dkif.nais.preprod.local/api/v1/personer/kontaktinformasjon?inkluderSikkerDigitalPost=false
public interface DkifClient extends HealthCheck {

    DkifKontaktinfo hentKontaktInfo(String fnr);

}
