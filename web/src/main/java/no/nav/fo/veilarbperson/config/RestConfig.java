package no.nav.fo.veilarbperson.config;

import no.nav.fo.veilarbperson.APIController;
import org.glassfish.jersey.server.ResourceConfig;

public class RestConfig extends ResourceConfig {
    public RestConfig() {
        super(APIController.class);
    }
}
