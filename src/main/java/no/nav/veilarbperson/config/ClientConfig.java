package no.nav.veilarbperson.config;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.audit_log.log.AuditLogger;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.client.aktoroppslag.CachedAktorOppslagClient;
import no.nav.common.client.aktoroppslag.PdlAktorOppslagClient;
import no.nav.common.client.norg2.CachedNorg2Client;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.client.norg2.NorgHttp2Client;
import no.nav.common.metrics.InfluxClient;
import no.nav.common.metrics.MetricsClient;
import no.nav.common.rest.client.RestClient;
import no.nav.common.token_client.builder.AzureAdTokenClientBuilder;
import no.nav.common.token_client.client.AzureAdMachineToMachineTokenClient;
import no.nav.common.token_client.client.AzureAdOnBehalfOfTokenClient;
import no.nav.common.token_client.client.MachineToMachineTokenClient;
import no.nav.poao_tilgang.client.PoaoTilgangClient;
import no.nav.veilarbperson.client.aiabackend.AiaBackendClient;
import no.nav.veilarbperson.client.aiabackend.AiaBackendClientImpl;
import no.nav.veilarbperson.client.digdir.DigdirClient;
import no.nav.veilarbperson.client.digdir.DigdirClientImpl;
import no.nav.veilarbperson.client.kodeverk.KodeverkClient;
import no.nav.veilarbperson.client.kodeverk.KodeverkClientImpl;
import no.nav.veilarbperson.client.kontoregister.KontoregisterClient;
import no.nav.veilarbperson.client.kontoregister.KontoregisterClientImpl;
import no.nav.veilarbperson.client.nom.SkjermetClient;
import no.nav.veilarbperson.client.nom.SkjermetClientImpl;
import no.nav.veilarbperson.client.pam.PamClient;
import no.nav.veilarbperson.client.pam.PamClientImpl;
import no.nav.veilarbperson.client.pdl.PdlClient;
import no.nav.veilarbperson.client.pdl.PdlClientImpl;
import no.nav.veilarbperson.client.veilarboppfolging.VeilarboppfolgingClient;
import no.nav.veilarbperson.client.veilarboppfolging.VeilarboppfolgingClientImpl;
import no.nav.veilarbperson.client.veilarbregistrering.VeilarbregistreringClient;
import no.nav.veilarbperson.client.veilarbregistrering.VeilarbregistreringClientImpl;
import no.nav.veilarbperson.service.AuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


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
    public AiaBackendClient aiaBackendClient(EnvironmentProperties environmentProperties, AuthService authService) {
        return new AiaBackendClientImpl(
                environmentProperties.getAiaBackendUrl(),
                () -> authService.getAadOboTokenForTjeneste(environmentProperties.getAiaBackendScope()));
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
    public KontoregisterClient kontoregisterClient(EnvironmentProperties properties, MachineToMachineTokenClient tokenClient) {
        return new KontoregisterClientImpl(properties.getKontoregisterUrl(), () -> tokenClient.createMachineToMachineToken(properties.getKontoregisterScope()));
    }

    @Bean
    public SkjermetClient skjermetClient(EnvironmentProperties properties, AzureAdMachineToMachineTokenClient tokenClient) {
        return new SkjermetClientImpl(properties.getSkjermedePersonerPipUrl(),
                () -> tokenClient.createMachineToMachineToken(properties.getSkjermedePersonerPipScope()));
    }

    @Bean
    public KodeverkClient kodeverkClient(EnvironmentProperties properties, MachineToMachineTokenClient tokenClient) {
        return new KodeverkClientImpl(properties.getKodeverkUrl(),
                () -> tokenClient.createMachineToMachineToken(properties.getKodeverkScope()));
    }

    @Bean
    public PdlClient pdlClient(EnvironmentProperties properties, AuthService authService, AzureAdMachineToMachineTokenClient tokenClient) {
        return new PdlClientImpl(properties.getPdlApiUrl(),
                () -> authService.getAadOboTokenForTjeneste(properties.getPdlApiScope()),
                () -> tokenClient.createMachineToMachineToken(properties.getPdlApiScope()));
    }


    @Bean
    public VeilarbregistreringClient veilarbregistreringClient(EnvironmentProperties properties, AzureAdMachineToMachineTokenClient aadMachineToMachineTokenClient) {
        return new VeilarbregistreringClientImpl(RestClient.baseClient(),
                properties.getVeilarbregistreringUrl(),
                () -> aadMachineToMachineTokenClient.createMachineToMachineToken(properties.getVeilarbregistreringScope()));
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

    @Bean
    public AuthService authService(AktorOppslagClient aktorOppslagClient, AuthContextHolder authContextHolder,
                                   EnvironmentProperties environmentProperties,
                                   AzureAdOnBehalfOfTokenClient aadOboTokenClient,
                                   PoaoTilgangClient poaoTilgangClient,
                                   AuditLogger auditLogger){
        return new AuthService(aktorOppslagClient, authContextHolder, environmentProperties, aadOboTokenClient,
                poaoTilgangClient, auditLogger);
    }

    @Bean
    public AuthService authServiceWithoutAuditLogg(AktorOppslagClient aktorOppslagClient, AuthContextHolder authContextHolder,
                                   EnvironmentProperties environmentProperties,
                                   AzureAdOnBehalfOfTokenClient aadOboTokenClient,
                                   PoaoTilgangClient poaoTilgangClient){
        return new AuthService(aktorOppslagClient, authContextHolder, environmentProperties, aadOboTokenClient,
                poaoTilgangClient, null);
    }

}
