package no.nav.veilarbperson.client.pdl.domain;

import java.util.HashMap;
import java.util.Map;

public enum VergemaalEllerFullmaktOmfangType {

    PERSONLIGE_OG_OEKONOMISKE_INTERESSER("personligeOgOekonomiskeInteresser"),
    UTLENDINGSSAKER("utlendingssakerPersonligeOgOekonomiskeInteresser"),
    PERSONLIGE_INTERESSER("personligeInteresser"),
    OEKONOMISKE_INTERESSER("oekonomiskeInteresser");

    private String type;
    private static final Map<String, VergemaalEllerFullmaktOmfangType> BY_NAME = new HashMap<>();

    static {
        for (VergemaalEllerFullmaktOmfangType omfang: values()) {
            BY_NAME.put(omfang.type, omfang);
        }
    }

    VergemaalEllerFullmaktOmfangType(String type) {
         this.type = type;
    }

    public static VergemaalEllerFullmaktOmfangType getOmfang(String type) {
        return BY_NAME.get(type);
    }
}
