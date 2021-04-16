package no.nav.veilarbperson.client.pdl.domain;

import java.util.HashMap;
import java.util.Map;

public enum Vergetype {

        ENSLIG_MINDREAARIG_ASYLSOEKER("ensligMindreaarigAsylsoeker"),
        ENSLIG_MINDREAARIG_FLYKTNING("ensligMindreaarigFlyktning"),
        FORVALTNING_UTENFOR_VERGEMAAL("forvaltningUtenforVergemaal"),
        STADFESTET_FREMTIDSFULLMAKT("stadfestetFremtidsfullmakt"),
        MINDREAARIG("mindreaarig"),
        MIDLERTIDIG_FOR_MINDREAARIG("midlertidigForMindreaarig"),
        VOKSEN("voksen"),
        MIDLERTIDIG_FOR_VOKSEN("midlertidigForVoksen");

        private final String type;
        private static final Map<String, Vergetype> BY_NAME = new HashMap<>();

        static {
                for (Vergetype vergetype: values()) {
                        BY_NAME.put(vergetype.type, vergetype);
                }
        }

        Vergetype(String type) {
                this.type = type;
        }

        public static Vergetype getVergetype(String type) {
                return BY_NAME.get(type);
        }
}
