package no.nav.veilarbperson.client.pdl.domain;

import com.fasterxml.jackson.annotation.JsonValue;

public enum VergemaalEllerFullmaktOmfangType {

    PERSONLIGE_OG_OEKONOMISKE_INTERESSER("personligeOgOekonomiskeInteresser"),
    UTLENDINGSSAKER("utlendingssakerPersonligeOgOekonomiskeInteresser"),
    PERSONLIGE_INTERESSER("personligeInteresser"),
    OEKONOMISKE_INTERESSER("oekonomiskeInteresser");

     private String type;

    VergemaalEllerFullmaktOmfangType(String type) {
         this.type = type;
     }

    @JsonValue
    public String toString() {
        return type;
    }
}
