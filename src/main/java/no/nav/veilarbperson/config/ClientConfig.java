package no.nav.veilarbperson.config;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.client.aktoroppslag.CachedAktorOppslagClient;
import no.nav.common.client.aktoroppslag.PdlAktorOppslagClient;
import no.nav.common.client.norg2.CachedNorg2Client;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.client.norg2.NorgHttp2Client;
import no.nav.common.cxf.StsConfig;
import no.nav.common.rest.client.RestClient;
import no.nav.common.sts.SystemUserTokenProvider;
import no.nav.common.token_client.builder.AzureAdTokenClientBuilder;
import no.nav.common.token_client.client.AzureAdMachineToMachineTokenClient;
import no.nav.common.token_client.client.AzureAdOnBehalfOfTokenClient;
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
import no.nav.veilarbperson.client.nom.SkjermetClient;
import no.nav.veilarbperson.client.nom.SkjermetClientImpl;
import no.nav.veilarbperson.client.pam.PamClient;
import no.nav.veilarbperson.client.pam.PamClientImpl;
import no.nav.veilarbperson.client.pdl.PdlClient;
import no.nav.veilarbperson.client.pdl.PdlClientImpl;
import no.nav.veilarbperson.client.person.PersonClient;
import no.nav.veilarbperson.client.person.PersonClientImpl;
import no.nav.veilarbperson.client.veilarboppfolging.VeilarboppfolgingClient;
import no.nav.veilarbperson.client.veilarboppfolging.VeilarboppfolgingClientImpl;
import no.nav.veilarbperson.client.veilarbportefolje.VeilarbportefoljeClient;
import no.nav.veilarbperson.client.veilarbportefolje.VeilarbportefoljeClientImpl;
import no.nav.veilarbperson.client.veilarbregistrering.VeilarbregistreringClient;
import no.nav.veilarbperson.client.veilarbregistrering.VeilarbregistreringClientImpl;
import no.nav.veilarbperson.service.AuthService;
import no.nav.veilarbperson.utils.DownstreamApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.function.Supplier;

import static java.lang.String.format;
import static no.nav.common.utils.EnvironmentUtils.requireClusterName;
import static no.nav.common.utils.NaisUtils.getCredentials;
import static no.nav.common.utils.UrlUtils.*;
import static no.nav.veilarbperson.config.ApplicationConfig.APPLICATION_NAME;

@Slf4j
@Configuration
public class ClientConfig {

    private static final String VEILARBPORTEFOLJE = "veilarbportefolje";
    private static final String VEILARBOPPFOLGING = "veilarboppfolging";
    private static final String PAM_CV_API = "pam-cv-api";


    @Bean
    public AktorOppslagClient aktorOppslagClient(SystemUserTokenProvider systemUserTokenProvider) {
        String pdlUrl = isProduction()
                ? createProdInternalIngressUrl("pdl-api")
                : createDevInternalIngressUrl("pdl-api-q1");

        no.nav.common.client.pdl.PdlClientImpl pdlClient = new no.nav.common.client.pdl.PdlClientImpl(
                pdlUrl,
                systemUserTokenProvider::getSystemUserToken,
                systemUserTokenProvider::getSystemUserToken);

        return new CachedAktorOppslagClient(new PdlAktorOppslagClient(pdlClient));
    }

    @Bean
    public Norg2Client norg2Client(EnvironmentProperties properties) {
        return new CachedNorg2Client(new NorgHttp2Client(properties.getNorg2Url()));
    }

    @Bean
    @Deprecated
    public VeilarbportefoljeClient veilarbportefoljeClient(AuthService authService) {
        String url = isProduction()
                ? createNaisAdeoIngressUrl(VEILARBPORTEFOLJE, true)
                : createNaisPreprodIngressUrl(VEILARBPORTEFOLJE, "q1", true);

        return new VeilarbportefoljeClientImpl(url, authService::getInnloggetBrukerToken);
    }

    @Bean
    public VeilarboppfolgingClient veilarboppfolgingClient(@Qualifier("veilarboppfolging") Supplier<String> veilarboppfolgingToken) {
        String url = isProduction()
                ? createNaisAdeoIngressUrl(VEILARBOPPFOLGING, true)
                : createNaisPreprodIngressUrl(VEILARBOPPFOLGING, "q1", true);
        return new VeilarboppfolgingClientImpl(url, veilarboppfolgingToken);
    }

    @Bean
    public DkifClient dkifClient(SystemUserTokenProvider systemUserTokenProvider) {
        return new DkifClientImpl(createServiceUrl("dkif", "default", false), systemUserTokenProvider);
    }


    @Bean
    public PamClient pamClient(SystemUserTokenProvider systemUserTokenProvider) {
        String url = isProduction()
                ? createNaisAdeoIngressUrl(PAM_CV_API, true)
                : createDevAdeoIngressUrl(PAM_CV_API, true);

        return new PamClientImpl(url, systemUserTokenProvider::getSystemUserToken);
    }

    @Bean
    @Deprecated
    public EgenAnsattClient egenAnsattClient(EnvironmentProperties properties, StsConfig stsConfig) {
        return new EgenAnsattClientImpl(properties.getEgenAnsattV1Endpoint(), stsConfig);
    }

    @Bean
    public PersonClient personClient(EnvironmentProperties properties, StsConfig stsConfig) {
        return new PersonClientImpl(properties.getPersonV3Endpoint(), stsConfig);
    }

    @Bean
    public SkjermetClient skjermetClient(AzureAdMachineToMachineTokenClient aadMachineToMachineTokenClient) {
        Supplier<String> serviceTokenSupplier = () -> aadMachineToMachineTokenClient
                .createMachineToMachineToken(
                        format("api://%s.%s.%s/.default",
                                isProduction() ? "prod-gcp" : "dev-gcp", "nom", "skjermede-personer-pip"));

        return new SkjermetClientImpl(createInternalIngressUrl("skjermede-personer-pip"), serviceTokenSupplier);
    }

    @Bean
    public KodeverkClient kodeverkClient() {
        return new KodeverkClientImpl(createServiceUrl("kodeverk", "default", false));
    }

    @Bean
    public PdlClient pdlClient() {
        return new PdlClientImpl(internalDevOrProdIngress("pdl-api"));
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
        String url = "https://api-gw" + apiGwSuffix + ".adeo.no/ekstern/difi/authlevel/rest/v1/sikkerhetsnivaa";

        return new DifiClientImpl(difiAccessTokenProvider, xNavApikey, url);
    }

    @Bean
    public VeilarbregistreringClient veilarbregistreringClient(
            AzureAdMachineToMachineTokenClient aadMachineToMachineTokenClient
    ) {
        Supplier<String> serviceTokenSupplier = () -> aadMachineToMachineTokenClient
                .createMachineToMachineToken(
                        format("api://%s.%s.%s/.default", requireClusterName(), "paw", "veilarbregistrering"));

        return new VeilarbregistreringClientImpl(
                RestClient.baseClient(),
                createInternalIngressUrl("veilarbregistrering"),
                serviceTokenSupplier
        );
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

    @Bean
    public AzureAdMachineToMachineTokenClient azureAdMachineToMachineTokenClient() {
        return AzureAdTokenClientBuilder.builder()
                .withNaisDefaults()
                .buildMachineToMachineTokenClient();
    }

    @Bean
    public AzureAdOnBehalfOfTokenClient azureAdOnBehalfOfTokenClient() {
        return AzureAdTokenClientBuilder.builder()
                .withNaisDefaults()
                .buildOnBehalfOfTokenClient();
    }

    @Bean("veilarboppfolging")
    public Supplier<String> userTokenProviderVeilarboppfolging(AuthService authService) {
        return authService.contextAwareUserTokenSupplier(new DownstreamApi(EnvironmentUtils.requireClusterName(), "pto", VEILARBOPPFOLGING));
    }

    @Bean("pdl")
    public Supplier<String> userTokenProviderPdl(AuthService authService) {
        return authService.contextAwareUserTokenSupplier(new DownstreamApi(EnvironmentUtils.requireClusterName(), "pdl", "pdl-api"));
    }

    public static boolean isProduction() {
        return EnvironmentUtils.isProduction().orElseThrow();
    }

    private static String internalDevOrProdIngress(String appName) {
        return isProduction()
                ? createProdInternalIngressUrl(appName)
                : createDevInternalIngressUrl(appName);
    }
}
