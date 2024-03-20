package no.nav.veilarbperson.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.env")
public class EnvironmentProperties {
    private String kodeverkUrl;
    private String kodeverkScope;
    private String krrScope;
    private String krrUrl;
    private String loginserviceIdportenAudience;
    private String loginserviceIdportenDiscoveryUrl;
    private String naisAadClientId;
    private String naisAadDiscoveryUrl;
    private String naisAadIssuer;
    private String norg2Url;
    private String pamCvApiScope;
    private String pamCvApiUrl;
    private String pdlApiScope;
    private String pdlApiUrl;
    private String poaoTilgangScope;
    private String poaoTilgangUrl;
    private String personV3Endpoint;
    private String regoppslagScope;
    private String regoppslagUrl;
    private String skjermedePersonerPipScope;
    private String skjermedePersonerPipUrl;
    private String soapStsUrl;
    private String veilarboppfolgingScope;
    private String veilarboppfolgingUrl;
    private String veilarbregistreringScope;
    private String veilarbregistreringUrl;
    private String kontoregisterScope;
    private String kontoregisterUrl;
    private String aiaBackendUrl;
    private String aiaBackendScope;
}
