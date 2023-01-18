package no.nav.veilarbperson.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.env")
public class EnvironmentProperties {

    private String openAmDiscoveryUrl;

    private String openAmRefreshUrl;

    private String naisStsDiscoveryUrl;
    
    private String loginserviceIdportenAudience;

    private String loginserviceIdportenDiscoveryUrl;

    private String naisAadDiscoveryUrl;

    private String naisAadIssuer;

    private String naisAadClientId;

    private String stsDiscoveryUrl;

    private String abacUrl;

    private String norg2Url;

    private String difiAuthlevelUrl;

    private String egenAnsattV1Endpoint;

    private String kodeverkUrl;

    private String krrUrl;

    private String krrScope;

    private String pamCvApiUrl;

    private String pamCvApiScope;

    private String pdlApiUrl;

    private String pdlApiScope;

    private String personV3Endpoint;

    private String skjermedePersonerPipUrl;

    private String skjermedePersonerPipScope;

    private String veilarboppfolgingUrl;

    private String veilarbregistreringUrl;

    private String veilarbregistreringScope;

    private String soapStsUrl;

    private String pdlUrl;

    private String unleashUrl;

}
