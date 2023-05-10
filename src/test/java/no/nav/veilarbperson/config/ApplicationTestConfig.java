package no.nav.veilarbperson.config;

import no.nav.common.abac.Pep;
import no.nav.common.audit_log.log.AuditLogger;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.auth.context.AuthContextHolderThreadLocal;
import no.nav.poao_tilgang.client.PoaoTilgangClient;
import no.nav.veilarbperson.client.person.KontoregisterClient;
import no.nav.veilarbperson.client.regoppslag.RegoppslagClient;
import no.nav.veilarbperson.mock.AbacClientMock;
import no.nav.veilarbperson.mock.PepMock;
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
    public Pep veilarbPep() {
        return new PepMock(new AbacClientMock());
    }

    @Bean
    public AuthContextHolder authContextHolder() {
        return AuthContextHolderThreadLocal.instance();
    }

    @Bean
    public RegoppslagClient RegoppslagClient(){
        return mock(RegoppslagClient.class);
    }

	@Bean
	public PoaoTilgangClient poaoTilgangClient() { return mock(PoaoTilgangClient.class); }

    @Bean
    public KontoregisterClient kontoregisterClient() { return mock(KontoregisterClient.class); }

	@Bean
	public AuditLogger auditLogger() { return mock(AuditLogger.class); }
}
