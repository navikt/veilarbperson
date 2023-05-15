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
import no.nav.veilarbperson.client.difi.DifiClient;
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
import no.nav.veilarbperson.client.kontoregister.KontoregisterClient;
import no.nav.veilarbperson.client.kontoregister.KontoregisterClientImpl;
import no.nav.veilarbperson.client.person.PersonClient;
import no.nav.veilarbperson.client.person.PersonClientImpl;
import no.nav.veilarbperson.client.veilarboppfolging.VeilarboppfolgingClient;
import no.nav.veilarbperson.client.veilarboppfolging.VeilarboppfolgingClientImpl;
import no.nav.veilarbperson.client.veilarbregistrering.VeilarbregistreringClient;
import no.nav.veilarbperson.client.veilarbregistrering.VeilarbregistreringClientImpl;
import no.nav.veilarbperson.service.AuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.common.utils.NaisUtils.getCredentials;

@Slf4j
@Configuration
public class ClientConfig {
    @Bean
    public AktorOppslagClient aktorOppslagClient(EnvironmentProperties properties, AzureAdMachineToMachineTokenClient tokenClient) {
        PdlAktorOppslagClient pdlClient = new PdlAktorOppslagClient(properties.getPdlApiUrl(),
                () -> tokenClient.createMachineToMachineToken(properties.getPdlApiScope()));

        return new CachedAktorOppslagClient(pdlClient);
    }

    @Bean
    public Norg2Client norg2Client(EnvironmentProperties properties) {
        return new CachedNorg2Client(new NorgHttp2Client(properties.getNorg2Url()));
    }

    @Bean
    public VeilarboppfolgingClient veilarboppfolgingClient(EnvironmentProperties properties, AuthService authService) {
        return new VeilarboppfolgingClientImpl(properties.getVeilarboppfolgingUrl(),
                () -> authService.getAadOboTokenForTjeneste(properties.getVeilarboppfolgingScope()));
    }

    @Bean
    public DigdirClient digdirClient(EnvironmentProperties properties, MachineToMachineTokenClient tokenClient) {
        return new DigdirClientImpl(properties.getKrrUrl(),
                () -> tokenClient.createMachineToMachineToken(properties.getKrrScope()));
    }

    @Bean
    public PamClient pamClient(EnvironmentProperties properties, AzureAdMachineToMachineTokenClient tokenClient) {
        return new PamClientImpl(properties.getPamCvApiUrl(),
                () -> tokenClient.createMachineToMachineToken(properties.getPamCvApiScope()));
    }

    @Bean
    public PersonClient personClient(EnvironmentProperties properties, StsConfig stsConfig) {
        return new PersonClientImpl(properties.getPersonV3Endpoint(), stsConfig);
    }

    @Bean
    public KontoregisterClient kontoregisterClient(EnvironmentProperties properties, MachineToMachineTokenClient tokenClient) {
        return new KontoregisterClientImpl(properties.getKontoregisterUrl(), () -> tokenClient.createMachineToMachineToken(properties.getKontoregisterScope()));
    }

    @Bean
    public SkjermetClient skjermetClient(EnvironmentProperties properties, AzureAdMachineToMachineTokenClient tokenClient) {
        return new SkjermetClientImpl(properties.getSkjermedePersonerPipUrl(),
                () -> tokenClient.createMachineToMachineToken(properties.getSkjermedePersonerPipScope()));
    }

    @Bean
    public KodeverkClient kodeverkClient(EnvironmentProperties properties) {
        return new KodeverkClientImpl(properties.getKodeverkUrl());
    }

    @Bean
    public PdlClient pdlClient(EnvironmentProperties properties, AuthService authService, AzureAdMachineToMachineTokenClient tokenClient) {
        return new PdlClientImpl(properties.getPdlApiUrl(),
                () -> authService.getAadOboTokenForTjeneste(properties.getPdlApiScope()),
                () -> tokenClient.createMachineToMachineToken(properties.getPdlApiScope()));
    }

    @Bean
    public DifiAccessTokenProviderImpl accessTokenRepository(EnvironmentProperties properties, SbsServiceUser sbsServiceUser) {
        return new DifiAccessTokenProviderImpl(sbsServiceUser, properties.getDifiTokenUrl());
    }

    @Bean
    public DifiClient difiClient(EnvironmentProperties properties, String xNavApikey, DifiAccessTokenProviderImpl difiAccessTokenProvider) {
        return new DifiClientImpl(difiAccessTokenProvider, xNavApikey, properties.getDifiAuthlevelUrl());
    }

    @Bean
    public VeilarbregistreringClient veilarbregistreringClient(EnvironmentProperties properties, AzureAdMachineToMachineTokenClient aadMachineToMachineTokenClient) {
        return new VeilarbregistreringClientImpl(RestClient.baseClient(),
                properties.getVeilarbregistreringUrl(),
                () -> aadMachineToMachineTokenClient.createMachineToMachineToken(properties.getVeilarbregistreringScope()));
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
        return AzureAdTokenClientBuilder.builder().withNaisDefaults().buildMachineToMachineTokenClient();
    }

    @Bean
    public AzureAdOnBehalfOfTokenClient azureAdOnBehalfOfTokenClient() {
        return AzureAdTokenClientBuilder.builder().withNaisDefaults().buildOnBehalfOfTokenClient();
    }

    @Bean
    public MetricsClient influxMetricsClient() {
        return new InfluxClient();
    }

    public static boolean isProduction() {
        return EnvironmentUtils.isProduction().orElseThrow();
    }
}
