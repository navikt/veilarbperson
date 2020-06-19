package no.nav.veilarbperson.client.pdl;

import no.nav.common.health.HealthCheck;

public interface PdlClient extends HealthCheck {

    <T> T graphqlRequest(String gqlRequestJson, String userToken, Class<T> responseDataClass);

}
