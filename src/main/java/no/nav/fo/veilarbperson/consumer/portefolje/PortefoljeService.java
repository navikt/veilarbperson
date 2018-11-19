package no.nav.fo.veilarbperson.consumer.portefolje;

import no.nav.apiapp.selftest.Helsesjekk;
import no.nav.apiapp.selftest.HelsesjekkMetadata;
import no.nav.fo.veilarbperson.domain.Personinfo;
import no.nav.sbl.rest.RestUtils;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Component
public class PortefoljeService implements Helsesjekk {

    private static final Client restClient = RestUtils.createClient();

    public Personinfo hentPersonInfo(String fodselsnummer, String cookie) {
        return apiTarget()
                .path("personinfo")
                .path(fodselsnummer)
                .request()
                .header(ACCEPT, APPLICATION_JSON)
                .header(HttpHeaders.COOKIE, cookie)
                .get(Personinfo.class);
    }

    @Override
    public void helsesjekk() {
        Response response = pingPath().request().get();
        int status = response.getStatus();
        if (status != 200) {
            throw new IllegalStateException(response.readEntity(String.class));
        }
    }

    @Override
    public HelsesjekkMetadata getMetadata() {
        return new HelsesjekkMetadata(
                "portefolje",
                pingPath().getUri().toString(),
                "api veilarbportefolje",
                false
        );
    }

    private WebTarget apiTarget() {
        return restClient.target("http://veilarbportefolje/veilarbportefolje/api");
    }

    private WebTarget pingPath() {
        return apiTarget().path("ping");
    }

}