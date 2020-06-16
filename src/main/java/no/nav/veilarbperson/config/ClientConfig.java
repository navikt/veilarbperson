package no.nav.veilarbperson.config;

import no.nav.common.client.norg2.CachedNorg2Client;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.client.norg2.NorgHttp2Client;
import no.nav.veilarbperson.client.dkif.DkifClient;
import no.nav.veilarbperson.client.dkif.DkifClientImpl;
import no.nav.veilarbperson.client.egenansatt.EgenAnsattClient;
import no.nav.veilarbperson.client.egenansatt.EgenAnsattClientImpl;
import no.nav.veilarbperson.client.kodeverk.KodeverkClient;
import no.nav.veilarbperson.client.kodeverk.KodeverkImpl;
import no.nav.veilarbperson.client.person.PersonClient;
import no.nav.veilarbperson.client.person.PersonClientImpl;
import no.nav.veilarbperson.client.veilarbportefolje.VeilarbportefoljeClient;
import no.nav.veilarbperson.client.veilarbportefolje.VeilarbportefoljeClientImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
        return new KodeverkImpl("http://kodeverk.default.svc.nais.local");
    }

}
