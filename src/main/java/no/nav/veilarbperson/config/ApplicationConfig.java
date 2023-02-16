package no.nav.veilarbperson.config;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.abac.Pep;
import no.nav.common.abac.VeilarbPepFactory;
import no.nav.common.abac.audit.SpringAuditRequestInfoSupplier;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.auth.context.AuthContextHolderThreadLocal;
import no.nav.common.cxf.StsConfig;
import no.nav.common.featuretoggle.UnleashClient;
import no.nav.common.featuretoggle.UnleashClientImpl;
import no.nav.common.token_client.client.AzureAdMachineToMachineTokenClient;
import no.nav.common.utils.Credentials;
import no.nav.common.utils.NaisUtils;
import no.nav.veilarbperson.client.regoppslag.JsonUtils;
import no.nav.veilarbperson.client.regoppslag.RegoppslagClient;
import no.nav.veilarbperson.client.regoppslag.RegoppslagClientImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
@EnableScheduling
@EnableConfigurationProperties({EnvironmentProperties.class})
public class ApplicationConfig {

    public final static String APPLICATION_NAME = "veilarbperson";

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

    @PostConstruct
    public void initJsonUtils() {
        JsonUtils.init();
    }
}
