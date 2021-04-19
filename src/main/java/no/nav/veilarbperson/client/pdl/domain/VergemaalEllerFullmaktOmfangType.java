package no.nav.veilarbperson.client.pdl.domain;

import com.fasterxml.jackson.annotation.JsonAlias;

public enum VergemaalEllerFullmaktOmfangType {

    @JsonAlias("personligeOgOekonomiskeInteresser")
    PERSONLIGE_OG_OEKONOMISKE_INTERESSER,
    @JsonAlias("utlendingssakerPersonligeOgOekonomiskeInteresser")
    UTLENDINGSSAKER,
    @JsonAlias("personligeInteresser")
    PERSONLIGE_INTERESSER,
    @JsonAlias("oekonomiskeInteresser")
    OEKONOMISKE_INTERESSER;
}
