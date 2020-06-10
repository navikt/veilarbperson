package no.nav.veilarbperson.config;

import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.config.ApiAppConfigurator;
import no.nav.apiapp.security.PepClient;
import no.nav.brukerdialog.security.domain.IdentType;
import no.nav.common.oidc.auth.OidcAuthenticatorConfig;
import no.nav.dialogarena.aktor.AktorConfig;
import no.nav.veilarbperson.PersonFletter;
import no.nav.sbl.dialogarena.common.abac.pep.Pep;
import no.nav.sbl.dialogarena.common.abac.pep.context.AbacContext;
import no.nav.sbl.dialogarena.common.abac.pep.domain.ResourceType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import static no.nav.common.oidc.Constants.*;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

@Configuration
@EnableScheduling
@ComponentScan(basePackages = "no.nav.fo.veilarbperson")
@Import({
        ServiceConfig.class,
        PersonFletter.class,
        CacheConfig.class,
        AbacContext.class,
        AktorConfig.class,
})
public class ApplicationConfig implements ApiApplication {
    public static final String AKTOER_V2_URL_PROPERTY = "AKTOER_V2_ENDPOINTURL";

    public OidcAuthenticatorConfig openAmAuthConfig() {
        return new OidcAuthenticatorConfig()
                .withDiscoveryUrl(getRequiredProperty("OPENAM_DISCOVERY_URL"))
                .withClientId(getRequiredProperty("VEILARBLOGIN_OPENAM_CLIENT_ID"))
                .withIdTokenCookieName(OPEN_AM_ID_TOKEN_COOKIE_NAME)
                .withRefreshTokenCookieName(REFRESH_TOKEN_COOKIE_NAME)
                .withRefreshUrl(getRequiredProperty("VEILARBLOGIN_OPENAM_REFRESH_URL"))
                .withIdentType(IdentType.InternBruker);
    }

    public OidcAuthenticatorConfig azureAdAuthConfig() {
        return new OidcAuthenticatorConfig()
                .withDiscoveryUrl(getRequiredProperty("AAD_DISCOVERY_URL"))
                .withClientId(getRequiredProperty("LOGINSERVICE_OIDC_CLIENT_ID"))
                .withIdTokenCookieName(AZURE_AD_ID_TOKEN_COOKIE_NAME)
                .withIdentType(IdentType.InternBruker);
    }

    public OidcAuthenticatorConfig azureAdB2CAuthConfig() {
        return new OidcAuthenticatorConfig()
                .withDiscoveryUrl(getRequiredProperty("AAD_B2C_DISCOVERY_URL"))
                .withClientId(getRequiredProperty("AAD_B2C_CLIENTID_USERNAME"))
                .withIdTokenCookieName(AZURE_AD_B2C_ID_TOKEN_COOKIE_NAME)
                .withIdentType(IdentType.EksternBruker);
    }

    @Override
    public void configure(ApiAppConfigurator apiAppConfigurator) {
        apiAppConfigurator
                .addOidcAuthenticator(openAmAuthConfig())
                .addOidcAuthenticator(azureAdAuthConfig())
                .addOidcAuthenticator(azureAdB2CAuthConfig())
                .sts();
    }

    @Bean
    public PepClient pepClient(Pep pep) {
        return new PepClient(pep, "veilarb", ResourceType.Person);
    }

}
