package no.nav.fo.veilarbperson;

import no.nav.brukerdialog.security.Constants;
import no.nav.brukerdialog.tools.SecurityConstants;
import no.nav.dialogarena.config.fasit.FasitUtils;
import no.nav.dialogarena.config.fasit.ServiceUser;
import no.nav.dialogarena.config.util.Util;
import no.nav.fo.veilarbperson.consumer.digitalkontaktinformasjon.DigitalKontaktinformasjonConfig;
import no.nav.fo.veilarbperson.consumer.kodeverk.KodeverkConfig;
import no.nav.fo.veilarbperson.consumer.organisasjonenhet.Norg2Config;
import no.nav.fo.veilarbperson.consumer.tps.TpsConfig;
import no.nav.sbl.dialogarena.common.abac.pep.CredentialConstants;
import no.nav.sbl.dialogarena.common.cxf.StsSecurityConstants;

import static java.lang.System.setProperty;
import static no.nav.brukerdialog.security.oidc.provider.AzureADB2CConfig.AZUREAD_B2C_DISCOVERY_URL_PROPERTY_NAME;
import static no.nav.brukerdialog.security.oidc.provider.AzureADB2CConfig.AZUREAD_B2C_EXPECTED_AUDIENCE_PROPERTY_NAME;
import static no.nav.dialogarena.config.fasit.FasitUtils.Zone.FSS;
import static no.nav.dialogarena.config.fasit.FasitUtils.getDefaultEnvironment;
import static no.nav.sbl.dialogarena.common.abac.pep.service.AbacServiceConfig.ABAC_ENDPOINT_URL_PROPERTY_NAME;


public class TestContext {

    public static final String APPLICATION_NAME = "veilarbperson";

    public static void setup() {
        String securityTokenService = FasitUtils.getBaseUrl("securityTokenService", FSS);
        ServiceUser srvveilarbperson = FasitUtils.getServiceUser("srvveilarbperson", APPLICATION_NAME);

        setProperty(StsSecurityConstants.STS_URL_KEY, securityTokenService);
        setProperty(StsSecurityConstants.SYSTEMUSER_USERNAME, srvveilarbperson.getUsername());
        setProperty(StsSecurityConstants.SYSTEMUSER_PASSWORD, srvveilarbperson.getPassword());

        setProperty(DigitalKontaktinformasjonConfig.DIGITAL_KONTAKTINFORMASJON_ENDPOINT, "https://app-" + getDefaultEnvironment()
                + ".adeo.no/digital-kontaktinformasjon/DigitalKontaktinformasjon/v1");

        setProperty(KodeverkConfig.KODEVERK_ENDPOINT, "https://modapp-" + getDefaultEnvironment() + ".adeo.no/kodeverk/ws/Kodeverk/v2");

        setProperty(Norg2Config.ENHET_NORG2_ENDPOINT_KEY, "https://app-" + getDefaultEnvironment() + ".adeo.no/norg2/ws/OrganisasjonEnhet/v2");

        setProperty(TpsConfig.EGENANSATT_ENDPOINT, "https://wasapp-" + getDefaultEnvironment() + ".adeo.no/tpsws/EgenAnsatt_v1");

        setProperty(TpsConfig.PERSON_ENDPOINT, "https://wasapp-" + getDefaultEnvironment() + ".adeo.no/tpsws/ws/Person/v3");

        setProperty(ABAC_ENDPOINT_URL_PROPERTY_NAME, FasitUtils.getRestService("abac.pdp.endpoint", getDefaultEnvironment()).getUrl());
        setProperty(CredentialConstants.SYSTEMUSER_USERNAME, srvveilarbperson.getUsername());
        setProperty(CredentialConstants.SYSTEMUSER_PASSWORD, srvveilarbperson.getPassword());

        String issoHost = FasitUtils.getBaseUrl("isso-host");
        String issoJWS = FasitUtils.getBaseUrl("isso-jwks");
        String issoISSUER = FasitUtils.getBaseUrl("isso-issuer");
        String issoIsAlive = FasitUtils.getBaseUrl("isso.isalive", FasitUtils.Zone.FSS);
        ServiceUser isso_rp_user = FasitUtils.getServiceUser("isso-rp-user", APPLICATION_NAME);
        String loginUrl = FasitUtils.getRestService("veilarblogin.redirect-url", getDefaultEnvironment()).getUrl();

        setProperty(Constants.ISSO_HOST_URL_PROPERTY_NAME, issoHost);
        setProperty(Constants.ISSO_RP_USER_USERNAME_PROPERTY_NAME, isso_rp_user.getUsername());
        setProperty(Constants.ISSO_RP_USER_PASSWORD_PROPERTY_NAME, isso_rp_user.getPassword());
        setProperty(Constants.ISSO_JWKS_URL_PROPERTY_NAME, issoJWS);
        setProperty(Constants.ISSO_ISSUER_URL_PROPERTY_NAME, issoISSUER);
        setProperty(Constants.ISSO_ISALIVE_URL_PROPERTY_NAME, issoIsAlive);
        setProperty(SecurityConstants.SYSTEMUSER_USERNAME, srvveilarbperson.getUsername());
        setProperty(SecurityConstants.SYSTEMUSER_PASSWORD, srvveilarbperson.getPassword());
        setProperty(Constants.OIDC_REDIRECT_URL_PROPERTY_NAME, loginUrl);

        ServiceUser azureADClientId = FasitUtils.getServiceUser("aad_b2c_clientid", APPLICATION_NAME);
        Util.setProperty(AZUREAD_B2C_DISCOVERY_URL_PROPERTY_NAME, FasitUtils.getBaseUrl("aad_b2c_discovery"));
        Util.setProperty(AZUREAD_B2C_EXPECTED_AUDIENCE_PROPERTY_NAME, azureADClientId.username);
    }
}
