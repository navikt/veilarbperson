import no.nav.apiapp.ApiApp;
import no.nav.fo.veilarbperson.config.ApplicationConfig;

import static java.lang.System.setProperty;
import static no.nav.dialogarena.aktor.AktorConfig.AKTOER_ENDPOINT_URL;
import static no.nav.fo.veilarbperson.config.ApplicationConfig.AKTOER_V2_URL_PROPERTY;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

public class Main {

    public static void main(String... args) {
        setProperty(AKTOER_ENDPOINT_URL, getRequiredProperty(AKTOER_V2_URL_PROPERTY));
        ApiApp.runApp(ApplicationConfig.class, args);
    }

}
