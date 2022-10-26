package no.nav.veilarbperson.config;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.client.aktoroppslag.CachedAktorOppslagClient;
import no.nav.common.client.aktoroppslag.PdlAktorOppslagClient;
import no.nav.common.client.norg2.CachedNorg2Client;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.client.norg2.NorgHttp2Client;
import no.nav.common.cxf.StsConfig;
import no.nav.common.metrics.InfluxClient;
import no.nav.common.metrics.MetricsClient;
import no.nav.common.rest.client.RestClient;
import no.nav.common.token_client.builder.AzureAdTokenClientBuilder;
import no.nav.common.token_client.client.AzureAdMachineToMachineTokenClient;
import no.nav.common.token_client.client.AzureAdOnBehalfOfTokenClient;
import no.nav.common.token_client.client.MachineToMachineTokenClient;
import no.nav.common.utils.Credentials;
import no.nav.common.utils.EnvironmentUtils;
import no.nav.common.utils.NaisUtils;
import no.nav.veilarbperson.client.difi.DifiAccessTokenProviderImpl;
import no.nav.veilarbperson.client.difi.DifiCient;
import no.nav.veilarbperson.client.difi.DifiClientImpl;
import no.nav.veilarbperson.client.difi.SbsServiceUser;
import no.nav.veilarbperson.client.digdir.DigdirClient;
import no.nav.veilarbperson.client.digdir.DigdirClientImpl;
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
import no.nav.veilarbperson.client.veilarbregistrering.VeilarbregistreringClient;
import no.nav.veilarbperson.client.veilarbregistrering.VeilarbregistreringClientImpl;
import no.nav.veilarbperson.service.AuthService;
import no.nav.veilarbperson.utils.DownstreamApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Supplier;

import static java.lang.String.format;
import static no.nav.common.utils.EnvironmentUtils.requireClusterName;
import static no.nav.common.utils.NaisUtils.getCredentials;
import static no.nav.common.utils.UrlUtils.*;

@Slf4j
@Configuration
public class ClientConfig {
    private static final String VEILARBOPPFOLGING = "veilarboppfolging";
    private static final String PAM_CV_API = "pam-cv-api";


    @Bean
    public AktorOppslagClient aktorOppslagClient(AzureAdMachineToMachineTokenClient tokenClient) {
        String tokenScop = String.format("api://%s-fss.pdl.pdl-api/.default",
                isProduction() ? "prod" : "dev"
        );

        PdlAktorOppslagClient pdlClient = new PdlAktorOppslagClient(
                createServiceUrl("pdl-api", "pdl", false),
                () -> tokenClient.createMachineToMachineToken(tokenScop));

        return new CachedAktorOppslagClient(pdlClient);
    }

    @Bean
    public Norg2Client norg2Client(EnvironmentProperties properties) {
        return new CachedNorg2Client(new NorgHttp2Client(properties.getNorg2Url()));
    }

    @Bean
    public VeilarboppfolgingClient veilarboppfolgingClient(AuthService authService) {
        String url = isProduction()
                ? createNaisAdeoIngressUrl(VEILARBOPPFOLGING, true)
                : createNaisPreprodIngressUrl(VEILARBOPPFOLGING, "q1", true);
        return new VeilarboppfolgingClientImpl(url, () -> authService.getAadOboTokenForTjeneste(
                new DownstreamApi(EnvironmentUtils.requireClusterName(), "pto", VEILARBOPPFOLGING))
        );
    }

    @Bean
    public DigdirClient digdirClient(MachineToMachineTokenClient tokenClient) {
        String url = isProduction() ?
                createProdInternalIngressUrl("digdir-krr-proxy")
                : createDevInternalIngressUrl("digdir-krr-proxy");
        String tokenScope = String.format("api://%s.team-rocket.digdir-krr-proxy/.default", isProduction() ? "prod-gcp" : "dev-gcp");
        return new DigdirClientImpl(url, () -> tokenClient.createMachineToMachineToken(tokenScope));
    }

    @Bean
    public PamClient pamClient(AzureAdMachineToMachineTokenClient tokenClient) {
        String url = isProduction()
                ? createNaisAdeoIngressUrl(PAM_CV_API, true)
                : createDevAdeoIngressUrl(PAM_CV_API, true);
        String tokenScop = String.format("api://%s.teampam.pam-cv-api/.default",
                isProduction() ? "prod-fss" : "dev-fss"
        );
        return new PamClientImpl(url, () -> tokenClient.createMachineToMachineToken(tokenScop));
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
    public PdlClient pdlClient(AuthService authService, AzureAdMachineToMachineTokenClient tokenClient) {
        String cluster = isProduction() ? "prod-fss" : "dev-fss";
        String tokenScop = String.format("api://%s.pdl.pdl-api/.default", cluster);

        return new PdlClientImpl(
                createServiceUrl("pdl-api", "pdl", false),
                () -> authService.getAadOboTokenForTjeneste(new DownstreamApi(cluster, "pdl", "pdl-api")),
                () -> tokenClient.createMachineToMachineToken(tokenScop)
        );
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
        String cluster = isProduction() ? "prod-fss" : "dev-gcp";
        Supplier<String> serviceTokenSupplier = () -> aadMachineToMachineTokenClient
                .createMachineToMachineToken(
                        format("api://%s.%s.%s/.default", cluster, "paw", "veilarbregistrering"));

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

    @Bean
    public MetricsClient influxMetricsClient() {
        return new InfluxClient();
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
