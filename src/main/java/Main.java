import no.nav.apiapp.ApiApp;
import no.nav.fo.veilarbperson.config.ApplicationConfig;

import static no.nav.fo.veilarbperson.consumer.digitalkontaktinformasjon.DigitalKontaktinformasjonConfig.DIGITAL_KONTAKTINFORMASJON_ENDPOINT;
import static no.nav.fo.veilarbperson.consumer.kodeverk.KodeverkConfig.KODEVERK_ENDPOINT;
import static no.nav.fo.veilarbperson.consumer.organisasjonenhet.Norg2Config.ENHET_NORG2_ENDPOINT_KEY;
import static no.nav.fo.veilarbperson.consumer.tps.TpsConfig.TPS_ENDPOINT;
import static no.nav.sbl.util.EnvironmentUtils.Type.PUBLIC;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;
import static no.nav.sbl.util.EnvironmentUtils.setProperty;

public class Main {

    public static final String SERVICEGATEWAY_URL = "SERVICEGATEWAY_URL";

    public static void main(String... args) {

        setProperty(DIGITAL_KONTAKTINFORMASJON_ENDPOINT, getRequiredProperty(SERVICEGATEWAY_URL, DIGITAL_KONTAKTINFORMASJON_ENDPOINT), PUBLIC);
        setProperty(TPS_ENDPOINT, getRequiredProperty(SERVICEGATEWAY_URL, TPS_ENDPOINT), PUBLIC);
        setProperty(KODEVERK_ENDPOINT, getRequiredProperty(SERVICEGATEWAY_URL, KODEVERK_ENDPOINT), PUBLIC);
        setProperty(ENHET_NORG2_ENDPOINT_KEY, getRequiredProperty(SERVICEGATEWAY_URL, ENHET_NORG2_ENDPOINT_KEY), PUBLIC);

        ApiApp.runApp(ApplicationConfig.class, args);
    }

}
