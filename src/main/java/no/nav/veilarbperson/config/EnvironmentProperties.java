package no.nav.veilarbperson.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.env")
public class EnvironmentProperties {

    private String openAmDiscoveryUrl;

    private String openAmClientId;

    private String openAmRefreshUrl;

    private String aadDiscoveryUrl;

    private String aadClientId;

    private String aadB2cDiscoveryUrl;

    private String aadB2cClientId;

    private String stsDiscoveryUrl;

    private String abacUrl;

    private String norg2Url;

    private String aktorregisterUrl;

    private String egenAnsattV1Endepoint;

    private String personV3Endpoint;
    
}
