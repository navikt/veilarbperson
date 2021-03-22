package no.nav.veilarbperson.client.pdl.domain;

import com.fasterxml.jackson.annotation.JsonValue;

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

        Vergetype(String type) {
            this.type = type;
        }

        @JsonValue
        public String toString() {
            return type;
        }
}
