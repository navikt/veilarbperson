package no.nav.veilarbperson.config;

import no.nav.common.client.aktorregister.AktorregisterClient;
import no.nav.common.client.aktorregister.AktorregisterHttpClient;
import no.nav.common.client.aktorregister.CachedAktorregisterClient;
import no.nav.common.client.norg2.CachedNorg2Client;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.client.norg2.NorgHttp2Client;
import no.nav.common.cxf.StsConfig;
import no.nav.common.sts.SystemUserTokenProvider;
import no.nav.common.utils.Credentials;
import no.nav.common.utils.EnvironmentUtils;
import no.nav.veilarbperson.client.difi.DifiCient;
import no.nav.veilarbperson.client.difi.DifiClientImpl;
import no.nav.veilarbperson.client.dkif.DkifClient;
import no.nav.veilarbperson.client.dkif.DkifClientImpl;
import no.nav.veilarbperson.client.egenansatt.EgenAnsattClient;
import no.nav.veilarbperson.client.egenansatt.EgenAnsattClientImpl;
import no.nav.veilarbperson.client.kodeverk.KodeverkClient;
import no.nav.veilarbperson.client.kodeverk.KodeverkClientImpl;
import no.nav.veilarbperson.client.pam.PamClient;
import no.nav.veilarbperson.client.pam.PamClientImpl;
import no.nav.veilarbperson.client.pdl.PdlClient;
import no.nav.veilarbperson.client.pdl.PdlClientImpl;
import no.nav.veilarbperson.client.person.PersonClient;
import no.nav.veilarbperson.client.person.PersonClientImpl;
import no.nav.veilarbperson.client.veilarbportefolje.VeilarbportefoljeClient;
import no.nav.veilarbperson.client.veilarbportefolje.VeilarbportefoljeClientImpl;
import no.nav.veilarbperson.service.AuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.common.utils.UrlUtils.clusterUrlForApplication;
import static no.nav.veilarbperson.config.ApplicationConfig.APPLICATION_NAME;

@Configuration
public class ClientConfig {

    @Bean
    public AktorregisterClient aktorregisterClient(EnvironmentProperties properties, SystemUserTokenProvider tokenProvider) {
        AktorregisterClient aktorregisterClient = new AktorregisterHttpClient(
                properties.getAktorregisterUrl(), APPLICATION_NAME, tokenProvider::getSystemUserToken
        );
        return new CachedAktorregisterClient(aktorregisterClient);
    }

    @Bean
    public Norg2Client norg2Client(EnvironmentProperties properties) {
        return new CachedNorg2Client(new NorgHttp2Client(properties.getNorg2Url()));
    }

    @Bean
    public VeilarbportefoljeClient veilarbportefoljeClient() {
        return new VeilarbportefoljeClientImpl(clusterUrlForApplication("veilarbportefolje", true));
    }

    @Bean
    public DkifClient dkifClient(SystemUserTokenProvider systemUserTokenProvider) {
        return new DkifClientImpl("http://dkif.default.svc.nais.local", systemUserTokenProvider);
    }

    @Bean
    public PamClient pamClient(AuthService authService) {
        String adeoPrefix = EnvironmentUtils.isDevelopment().orElse(false) ? "dev" : "nais";
        String pamCvUrl = String.format("https://pam-cv-api.%s.adeo.no/pam-cv-api", adeoPrefix);
        return new PamClientImpl(pamCvUrl, authService::getInnloggetBrukerToken);
    }

    @Bean
    public EgenAnsattClient egenAnsattClient(EnvironmentProperties properties, StsConfig stsConfig) {
        return new EgenAnsattClientImpl(properties.getEgenAnsattV1Endepoint(), stsConfig);
    }

    @Bean
    public PersonClient personClient(EnvironmentProperties properties, StsConfig stsConfig) {
        return new PersonClientImpl(properties.getPersonV3Endpoint(), stsConfig);
    }

    @Bean
    public KodeverkClient kodeverkClient() {
        return new KodeverkClientImpl("http://kodeverk.default.svc.nais.local");
    }

    @Bean
    public PdlClient pdlClient(SystemUserTokenProvider tokenProvider) {
        return new PdlClientImpl("http://pdl-api.default.svc.nais.local", tokenProvider::getSystemUserToken);
    }

    @Bean
    public DifiCient difiCient(Credentials serviceUserCredentials) {
        return new DifiClientImpl(serviceUserCredentials, DifiClientImpl.getDifiUrl());
    }

}
