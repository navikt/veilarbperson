package no.nav.fo.veilarbperson.internal;


import no.nav.sbl.dialogarena.common.abac.pep.Pep;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

@Configuration
public class PingConfig {

    public final static String ISSO_ISALIVE = "ISSO_ISALIVE";
    public final static String ABAC_PDP_ENDPOINT_URL = "ABAC_PDP_ENDPOINT_URL";

    private final Pep pep;

    PingConfig(Pep pep) {
        this.pep = pep;
    }

    @Bean
    public Pingable pepPing() {
        PingMetadata metadata = new PingMetadata(
                "ABAC via " + getRequiredProperty(ABAC_PDP_ENDPOINT_URL),
                "Tilgangskontroll. Sjekker om veileder har tilgang til bruker.",
                true
        );
        return () -> {
            try {
                pep.ping();
                return Pingable.Ping.lyktes(metadata);
            } catch( Exception e) {
                return Pingable.Ping.feilet(metadata, e);
            }
        };
    }

    @Bean
    public Pingable issoPing() throws IOException {
        PingMetadata metadata = new Pingable.Ping.PingMetadata(
                "ISSO via " + getRequiredProperty(ISSO_ISALIVE),
                "Pålogging og autorisering (single-signon).",
                true
        );

        return () -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(System.getProperty("ISSO_ISALIVÉ")).openConnection();
                connection.connect();
                if (connection.getResponseCode() == 200) {
                    return Pingable.Ping.lyktes(metadata);
                }
                return Pingable.Ping.feilet(metadata, "IsAlive returnerte statuskode: " + connection.getResponseCode());
            } catch (Exception e) {
                return Pingable.Ping.feilet(metadata, e);
            }
        };
    }

}