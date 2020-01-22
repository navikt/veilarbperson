package no.nav.fo.veilarbperson.config;

import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.config.ApiAppConfigurator;
import no.nav.brukerdialog.security.domain.IdentType;
import no.nav.brukerdialog.security.oidc.provider.AzureADB2CConfig;
import no.nav.common.auth.SecurityLevel;
import no.nav.dialogarena.aktor.AktorConfig;
import no.nav.fo.veilarbperson.PersonFletter;
import no.nav.sbl.dialogarena.common.abac.pep.context.AbacContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import static no.nav.brukerdialog.security.Constants.AZUREADB2C_OIDC_COOKIE_NAME_FSS;
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

    @Override
    public void configure(ApiAppConfigurator apiAppConfigurator) {
        String discoveryUrl = getRequiredProperty("AAD_DISCOVERY_URL");
        String clientId = getRequiredProperty("VEILARBLOGIN_AAD_CLIENT_ID");

        AzureADB2CConfig config = AzureADB2CConfig.builder()
                .discoveryUrl(discoveryUrl)
                .expectedAudience(clientId)
                .identType(IdentType.InternBruker)
                .tokenName(AZUREADB2C_OIDC_COOKIE_NAME_FSS)
                .build();

        apiAppConfigurator
                .validateAzureAdExternalUserTokens(SecurityLevel.Level4)
                .validateAzureAdInternalUsersTokens(config)
                .issoLogin() // OpenAM i FSS
                .sts();
    }
}
