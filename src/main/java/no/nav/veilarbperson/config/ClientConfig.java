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
import no.nav.common.utils.Credentials;
import no.nav.common.utils.EnvironmentUtils;
import no.nav.common.utils.NaisUtils;
import no.nav.veilarbperson.client.difi.DifiAccessTokenProviderImpl;
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

import static no.nav.common.utils.NaisUtils.getCredentials;
import static no.nav.common.utils.UrlUtils.*;
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
        String url = isProduction()
                ? createNaisAdeoIngressUrl("veilarbportefolje", true)
                : createNaisPreprodIngressUrl("veilarbportefolje", "q1", true);

        return new VeilarbportefoljeClientImpl(url);
    }

    @Bean
    public DkifClient dkifClient(SystemUserTokenProvider systemUserTokenProvider) {
        return new DkifClientImpl(createServiceUrl("dkif", "default", false), systemUserTokenProvider);
    }


    @Bean
    public PamClient pamClient(AuthService authService) {
        String url = isProduction()
                ? createNaisAdeoIngressUrl("pam-cv-api", true)
                : createDevAdeoIngressUrl("pam-cv-api", true);

        return new PamClientImpl(url, authService::getInnloggetBrukerToken);
    }

    @Bean
    public EgenAnsattClient egenAnsattClient(EnvironmentProperties properties, StsConfig stsConfig) {
        return new EgenAnsattClientImpl(properties.getEgenAnsattV1Endpoint(), stsConfig);
    }

    @Bean
    public PersonClient personClient(EnvironmentProperties properties, StsConfig stsConfig) {
        return new PersonClientImpl(properties.getPersonV3Endpoint(), stsConfig);
    }

    @Bean
    public KodeverkClient kodeverkClient() {
        return new KodeverkClientImpl(createServiceUrl("kodeverk", "default", false));
    }

    @Bean
    public PdlClient pdlClient(SystemUserTokenProvider tokenProvider) {
        return new PdlClientImpl(createServiceUrl("pdl-api", "default", false), tokenProvider::getSystemUserToken);
    }

    @Bean
    public DifiAccessTokenProviderImpl accessTokenRepository(SbsServiceUser sbsServiceUser) {
        String apiGwSuffix = isProduction() ? "" : "-q1";
        String url = "https://api-gw" + apiGwSuffix + ".adeo.no/ekstern/difi/idporten-oidc-provider/token";

        return new DifiAccessTokenProviderImpl(sbsServiceUser, url);
    }

    @Bean
    public DifiCient difiCient(String xNavApikey, DifiAccessTokenProviderImpl difiAccessTokenProvider) {
        String apiGwSuffix = isProduction() ? "" : "-q1";
        String url = "https://api-gw"+ apiGwSuffix + ".adeo.no/ekstern/difi/authlevel/rest/v1/sikkerhetsnivaa";

        return new DifiClientImpl(difiAccessTokenProvider, xNavApikey, url);
    }

    @Bean
    public String xNavApikey() {
        return NaisUtils.getFileContent("/var/run/secrets/nais.io/authlevel/x-nav-apiKey");
    }

    @Bean
    public SbsServiceUser sbsServiceUser() {
        Credentials service_user_sbs = getCredentials("service_user_sbs");
        return new SbsServiceUser(service_user_sbs.username, service_user_sbs.password);
    }

    private static boolean isProduction() {
        return EnvironmentUtils.isProduction().orElseThrow();
    }

}
