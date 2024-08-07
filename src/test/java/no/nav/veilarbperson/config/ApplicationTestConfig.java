package no.nav.veilarbperson.config;

import no.nav.common.audit_log.log.AuditLogger;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.auth.context.AuthContextHolderThreadLocal;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.token_client.client.AzureAdOnBehalfOfTokenClient;
import no.nav.poao_tilgang.client.PoaoTilgangClient;
import no.nav.poao_tilgang.client.PoaoTilgangMockClient;
import no.nav.poao_tilgang.poao_tilgang_test_core.NavContext;
import no.nav.veilarbperson.client.regoppslag.RegoppslagClient;
import no.nav.veilarbperson.client.representasjon.RepresentasjonClient;
import no.nav.veilarbperson.service.AuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.mock;

@Configuration
@Import({
        ClientTestConfig.class,
        ControllerTestConfig.class,
        ServiceTestConfig.class,
        FilterTestConfig.class,
        EnvironmentProperties.class
})
public class ApplicationTestConfig {

    @Bean
    public AuthContextHolder authContextHolder() {
        return AuthContextHolderThreadLocal.instance();
    }

    @Bean
    public RegoppslagClient RegoppslagClient(){
        return mock(RegoppslagClient.class);
    }


    @Bean("authServiceWithoutAuditLog")
    public AuthService authService(AktorOppslagClient aktorOppslagClient, AuthContextHolder authContextHolder, EnvironmentProperties environmentProperties,
                                   AzureAdOnBehalfOfTokenClient aadOboTokenClient, PoaoTilgangClient poaoTilgangClient){
        return new AuthService(aktorOppslagClient, authContextHolder, environmentProperties, aadOboTokenClient, poaoTilgangClient, null);
    }

	@Bean
	public PoaoTilgangClient poaoTilgangClient(NavContext navContext) {
        return new PoaoTilgangMockClient(navContext);
    }

    @Bean
    public NavContext navContext() {
        return new NavContext();
    }


	@Bean
	public AuditLogger auditLogger() { return mock(AuditLogger.class); }

    @Bean
    public RepresentasjonClient representasjonClient(){
        return mock(RepresentasjonClient.class);
    }
}
