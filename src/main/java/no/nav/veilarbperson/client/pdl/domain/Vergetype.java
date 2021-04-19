package no.nav.veilarbperson.client.pdl.domain;

import com.fasterxml.jackson.annotation.JsonAlias;

public enum Vergetype {

        @JsonAlias("ensligMindreaarigAsylsoeker")
        ENSLIG_MINDREAARIG_ASYLSOEKER,
        @JsonAlias("ensligMindreaarigFlyktning")
        ENSLIG_MINDREAARIG_FLYKTNING,
        @JsonAlias("forvaltningUtenforVergemaal")
        FORVALTNING_UTENFOR_VERGEMAAL,
        @JsonAlias("stadfestetFremtidsfullmakt")
        STADFESTET_FREMTIDSFULLMAKT,
        @JsonAlias("mindreaarig")
        MINDREAARIG,
        @JsonAlias("midlertidigForMindreaarig")
        MIDLERTIDIG_FOR_MINDREAARIG,
        @JsonAlias("voksen")
        VOKSEN,
        @JsonAlias("midlertidigForVoksen")
        MIDLERTIDIG_FOR_VOKSEN;
}
