package no.nav.veilarbperson.config;

import no.nav.common.abac.Pep;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.auth.context.AuthContextHolderThreadLocal;
import no.nav.veilarbperson.mock.AbacClientMock;
import no.nav.veilarbperson.mock.PepMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        SwaggerConfig.class,
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

}
