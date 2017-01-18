package no.nav.fo.veilarbperson.config;

import no.nav.modig.security.ws.attributes.SAMLAttributes;


public class MockSAMLOutInterceptor implements SAMLAttributes {

    @Override
    public String getUid() {
        return "Z990300";
    }

    @Override
    public String getAuthenticationLevel() {
        return "4";
    }

    @Override
    public String getIdentType() {
        return "InternBruker";
    }

    @Override
    public String getConsumerId() {
        return "srvveilarbperson";
    }
}