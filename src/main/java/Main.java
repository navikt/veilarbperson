import no.nav.apiapp.ApiApp;
import no.nav.fo.veilarbperson.config.ApplicationConfig;

public class Main {

    public static final String SERVICEGATEWAY_URL = "SERVICEGATEWAY_URL";

    public static void main(String[] args) {
        ApiApp.runApp(ApplicationConfig.class, args);
    }

}
