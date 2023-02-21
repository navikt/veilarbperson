package no.nav.veilarbperson.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.env")
public class EnvironmentProperties {
    private String stsDiscoveryUrl;

    private String abacUrl;

    private String difiAuthlevelUrl;

    private String difiTokenUrl;

    private String egenAnsattV1Endpoint;

    private String kodeverkUrl;

    private String krrScope;

    private String krrUrl;

    private String loginserviceIdportenAudience;

    private String loginserviceIdportenDiscoveryUrl;

    private String naisAadClientId;

    private String naisAadDiscoveryUrl;

    private String naisAadIssuer;

    private String naisStsDiscoveryUrl;

    private String norg2Url;

    private String openAmDiscoveryUrl;

    private String openAmRefreshUrl;

    private String pamCvApiScope;

    private String pamCvApiUrl;

    private String pdlApiScope;

    private String pdlApiUrl;

    private String pdlUrl;

	private String poaoTilgangScope;

    private String poaoTilgangUrl;

    private String personV3Endpoint;

    private String regoppslagScope;

    private String regoppslagUrl;

    private String skjermedePersonerPipScope;

    private String skjermedePersonerPipUrl;

    private String soapStsUrl;

    private String unleashUrl;

    private String veilarboppfolgingScope;

    private String veilarboppfolgingUrl;

    private String veilarbregistreringScope;

    private String veilarbregistreringUrl;

}
