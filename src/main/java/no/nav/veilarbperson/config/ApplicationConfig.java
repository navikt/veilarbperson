package no.nav.veilarbperson.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.abac.Pep;
import no.nav.common.abac.VeilarbPepFactory;
import no.nav.common.abac.audit.SpringAuditRequestInfoSupplier;
import no.nav.common.audit_log.log.AuditLogger;
import no.nav.common.audit_log.log.AuditLoggerImpl;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.auth.context.AuthContextHolderThreadLocal;
import no.nav.common.cxf.StsConfig;
import no.nav.common.featuretoggle.UnleashClient;
import no.nav.common.featuretoggle.UnleashClientImpl;
import no.nav.common.rest.client.RestClient;
import no.nav.common.token_client.client.AzureAdMachineToMachineTokenClient;
import no.nav.common.utils.Credentials;
import no.nav.common.utils.NaisUtils;
import no.nav.poao_tilgang.client.AdGruppe;
import no.nav.poao_tilgang.client.Decision;
import no.nav.poao_tilgang.client.PoaoTilgangCachedClient;
import no.nav.poao_tilgang.client.PoaoTilgangClient;
import no.nav.poao_tilgang.client.PoaoTilgangHttpClient;
import no.nav.poao_tilgang.client.PolicyInput;
import no.nav.veilarbperson.client.regoppslag.RegoppslagClient;
import no.nav.veilarbperson.client.regoppslag.RegoppslagClientImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Configuration
@EnableScheduling
@EnableConfigurationProperties({EnvironmentProperties.class})
public class ApplicationConfig {

    public final static String APPLICATION_NAME = "veilarbperson";
	private final Cache<PolicyInput, Decision> policyInputToDecisionCache = Caffeine.newBuilder()
			.expireAfterWrite(Duration.ofMinutes(30))
			.build();
	private final Cache<UUID, List<AdGruppe>> navAnsattIdToAzureAdGrupperCache = Caffeine.newBuilder()
			.expireAfterWrite(Duration.ofMinutes(30))
			.build();
	private final Cache<String, Boolean> norskIdentToErSkjermetCache = Caffeine.newBuilder()
			.expireAfterWrite(Duration.ofMinutes(30))
			.build();

    @Bean
    public StsConfig stsConfig(EnvironmentProperties properties) {
        Credentials serviceUser = NaisUtils.getCredentials("service_user");
        return StsConfig.builder()
                .url(properties.getSoapStsUrl())
                .username(serviceUser.username)
                .password(serviceUser.password)
                .build();
    }

    @Bean
    public Pep veilarbPep(EnvironmentProperties properties) {
        Credentials serviceUserCredentials = NaisUtils.getCredentials("service_user");
        return VeilarbPepFactory.get(
                properties.getAbacUrl(), serviceUserCredentials.username,
                serviceUserCredentials.password, new SpringAuditRequestInfoSupplier());
    }
	@Bean
	public PoaoTilgangClient poaoTilgangClient(EnvironmentProperties properties, AzureAdMachineToMachineTokenClient tokenClient) {
		return new PoaoTilgangCachedClient(
				new PoaoTilgangHttpClient(
						properties.getPoaoTilgangUrl(),
						() -> tokenClient.createMachineToMachineToken(properties.getPoaoTilgangScope()),
						RestClient.baseClient()
				),
				policyInputToDecisionCache,
				navAnsattIdToAzureAdGrupperCache,
				norskIdentToErSkjermetCache
		);
	}

    @Bean
    public AuthContextHolder authContextHolder() {
        return AuthContextHolderThreadLocal.instance();
    }

    @Bean
    public UnleashClient unleashClient(EnvironmentProperties properties) {
        return new UnleashClientImpl(properties.getUnleashUrl(), APPLICATION_NAME);
    }

    @Bean
    public RegoppslagClient regoppslagClient(EnvironmentProperties properties, AzureAdMachineToMachineTokenClient tokenClient) {
        return new RegoppslagClientImpl(properties.getRegoppslagUrl(), () -> tokenClient.createMachineToMachineToken(properties.getRegoppslagScope()));
    }

	@Bean
	AuditLogger auditLogger() {
		return new AuditLoggerImpl();
	}
}
