package no.nav.veilarbperson.config;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.aktorregister.AktorregisterClient;
import no.nav.common.client.aktorregister.AktorregisterHttpClient;
import no.nav.common.client.aktorregister.CachedAktorregisterClient;
import no.nav.common.client.norg2.CachedNorg2Client;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.client.norg2.NorgHttp2Client;
import no.nav.common.cxf.StsConfig;
import no.nav.common.sts.SystemUserTokenProvider;
import no.nav.common.utils.EnvironmentUtils;
import no.nav.common.utils.NaisUtils;
import no.nav.veilarbperson.client.difi.DifiAccessTokenProvider;
import no.nav.veilarbperson.client.difi.DifiCient;
import no.nav.veilarbperson.client.difi.DifiClientImpl;
import no.nav.veilarbperson.client.difi.SbsServiceUser;
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

import static no.nav.common.utils.EnvironmentUtils.getOptionalProperty;
import static no.nav.common.utils.UrlUtils.clusterUrlForApplication;
import static no.nav.veilarbperson.config.ApplicationConfig.APPLICATION_NAME;

@Slf4j
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
    public DifiAccessTokenProvider accessTokenRepository(SbsServiceUser sbsServiceUser) {
        return new DifiAccessTokenProvider(sbsServiceUser, DifiAccessTokenProvider.getTokenUrl());
    }

    @Bean
    public DifiCient difiCient(String xNavApikey, DifiAccessTokenProvider difiAccessTokenProvider) {
        return new DifiClientImpl(difiAccessTokenProvider, xNavApikey, DifiClientImpl.getNivaa4Url());
    }

    @Bean
    public String xNavApikey() {
        // TODO: 22/10/2020 fjern feilhåndtering når klar for prod
        try {
            return NaisUtils.getFileContent("/var/run/secrets/nais.io/authlevel/x-nav-apiKey");
        } catch (IllegalStateException e) {
            log.error("fant ikke x-nav-apiKey",e);
            return null;
        }
    }

    @Bean
    public SbsServiceUser sbsServiceUser() {
        // TODO: 22/10/2020 bytt til requered prop når klart for prod
        String sbs_user = getOptionalProperty("SBS_USER").orElse("");
        String sps_password = getOptionalProperty("SPS_PASSWORD").orElse("");
        if (sps_password.equals("")) {
            log.error("fant ikke sps_passord");
        }
        if (sbs_user.equals("")) {
            log.error("fant ikke sbs_user");
        }
        return new SbsServiceUser(sbs_user, sps_password);
    }

}
