package no.nav.veilarbperson.config;

import no.nav.common.client.norg2.CachedNorg2Client;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.client.norg2.NorgHttp2Client;
import no.nav.veilarbperson.client.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.common.utils.EnvironmentUtils.getRequiredProperty;
import static no.nav.common.utils.UrlUtils.clusterUrlForApplication;

@Configuration
public class ClientConfig {

    @Bean
    public Norg2Client norg2Client(EnvironmentProperties properties) {
        return new CachedNorg2Client(new NorgHttp2Client(properties.getNorg2Url()));
    }

    @Bean
    public VeilarbportefoljeClient veilarbportefoljeClient() {
        return new VeilarbportefoljeClientImpl(clusterUrlForApplication("veilarbportefolje", true));
    }

    @Bean
    public DkifClient dkifClient() {
        return new DkifClientImpl("http://dkif.default.svc.nais.local");
    }

    @Bean
    public EgenAnsattClient egenAnsattClient(EnvironmentProperties properties) {
        return new EgenAnsattClientImpl(properties.getEgenAnsattV1Endepoint());
    }

    @Bean
    public PersonClient personClient(EnvironmentProperties properties) {
        return new PersonClientImpl(properties.getPersonV3Endpoint());
    }

    @Bean
    public KodeverkClient kodeverkClient() {
        return new KodeverkImpl(getRequiredProperty("VIRKSOMHET_KODEVERK_V2_ENDPOINTURL"));
    }

}
