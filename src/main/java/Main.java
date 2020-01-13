import no.nav.apiapp.ApiApp;
import no.nav.brukerdialog.tools.SecurityConstants;
import no.nav.common.utils.NaisUtils;
import no.nav.fo.veilarbperson.config.ApplicationConfig;
import no.nav.sbl.dialogarena.common.abac.pep.CredentialConstants;
import no.nav.sbl.dialogarena.common.cxf.StsSecurityConstants;

import static java.lang.System.setProperty;
import static no.nav.dialogarena.aktor.AktorConfig.AKTOER_ENDPOINT_URL;
import static no.nav.fo.veilarbperson.config.ApplicationConfig.AKTOER_V2_URL_PROPERTY;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

public class Main {

    public static void main(String... args) {

        NaisUtils.Credentials serviceUser = NaisUtils.getCredentials("service_user");

        //ABAC
        System.setProperty(CredentialConstants.SYSTEMUSER_USERNAME, serviceUser.username);
        System.setProperty(CredentialConstants.SYSTEMUSER_PASSWORD, serviceUser.password);

        //CXF
        System.setProperty(StsSecurityConstants.SYSTEMUSER_USERNAME, serviceUser.username);
        System.setProperty(StsSecurityConstants.SYSTEMUSER_PASSWORD, serviceUser.password);

        //OIDC
        System.setProperty(SecurityConstants.SYSTEMUSER_USERNAME, serviceUser.username);
        System.setProperty(SecurityConstants.SYSTEMUSER_PASSWORD, serviceUser.password);

        NaisUtils.addConfigMapToEnv("pto-config",
                "AAD_B2C_DISCOVERY_URL",
                "ISSO_HOST_URL",
                "ISSO_ISALIVE_URL",
                "ISSO_ISSUER_URL",
                "ISSO_JWKS_URL",
                AKTOER_V2_URL_PROPERTY,
                "AKTOER_V2_SECURITYTOKEN",
                "AKTOER_V2_WSDLURL",
                "VIRKSOMHET_ORGANISASJONENHET_V2_ENDPOINTURL",
                "VIRKSOMHET_ORGANISASJONENHET_V2_SECURITYTOKEN",
                "VIRKSOMHET_ORGANISASJONENHET_V2_WSDLURL_URL",
                "SECURITYTOKENSERVICE_URL",
                "UNLEASH_API_URL",
                "VEILARBAKTIVITETAPI_URL",
                "VEILARBDIALOGAPI_URL",
                "VEILARBLOGIN_REDIRECT_URL_DESCRIPTION",
                "VEILARBLOGIN_REDIRECT_URL_URL",
                "VIRKSOMHET_DIGITALKONTAKINFORMASJON_V1_ENDPOINTURL",
                "VIRKSOMHET_DIGITALKONTAKINFORMASJON_V1_SECURITYTOKEN",
                "VIRKSOMHET_DIGITALKONTAKINFORMASJON_V1_WSDLURL",
                "VIRKSOMHET_ENHET_V1_ENDPOINTURL",
                "VIRKSOMHET_ENHET_V1_SECURITYTOKEN",
                "VIRKSOMHET_ENHET_V1_WSDLURL",
                "VEILARBPORTEFOLJEDB_URL",
                "VEILARBPORTEFOLJEDB_ONSHOSTS",
                "VIRKSOMHET_KODEVERK_V2_SECURITYTOKEN",
                "VIRKSOMHET_KODEVERK_V2_ENDPOINTURL",
                "VIRKSOMHET_KODEVERK_V2_WSDLURL",
                "VIRKSOMHET_PERSON_V3_SECURITYTOKEN",
                "VIRKSOMHET_PERSON_V3_ENDPOINTURL",
                "VIRKSOMHET_PERSON_V3_WSDLURL"
                );

        setProperty(AKTOER_ENDPOINT_URL, getRequiredProperty(AKTOER_V2_URL_PROPERTY));

        ApiApp.runApp(ApplicationConfig.class, args);
    }

}
