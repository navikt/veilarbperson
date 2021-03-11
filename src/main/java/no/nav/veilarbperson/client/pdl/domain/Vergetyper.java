package no.nav.veilarbperson.client.pdl.domain;

public enum Vergetyper {

        ENSLIG_MINDREAARIG_ASYLSOEKER("ensligMindreaarigAsylsoeker", "Enslig mindreårig asylsøker"),
        ENSLIG_MINDREAARIG_FLYKTNING("ensligMindreaarigFlyktning", "Enslig mindreårig flyktning inklusive midlertidige saker for denne gruppen"),
        FORVALTNING_UTENFOR_VERGEMAAL("forvaltningUtenforVergemaal", "Forvaltning utenfor vergemål"),
        STADFESTET_FREMTIDSFULLMAKT("stadfestetFremtidsfullmakt","Fremtidsfullmakt"),
        MINDREAARIG("mindreaarig", "Mindreårig (unntatt EMF)"),
        MIDLERTIDIG_FOR_MINDREAARIG("midlertidigForMindreaarig", "Mindreårig midlertidig (unntatt EMF)"),
        VOKSEN("voksen", "Voksen"),
        MIDLERTIDIG_FOR_VOKSEN("midlertidigForVoksen", "Voksen midlertidig");

        private final String type;
        private final String beskrivelse;

        Vergetyper(String type, String beskrivelse) {
            this.type = type;
            this.beskrivelse = beskrivelse;
        }

        public static String getBeskrivelse(String type) {

            if(ENSLIG_MINDREAARIG_ASYLSOEKER.type.equals(type)) {
                return ENSLIG_MINDREAARIG_ASYLSOEKER.beskrivelse;
            } else if(ENSLIG_MINDREAARIG_FLYKTNING.type.equals(type)) {
                return ENSLIG_MINDREAARIG_FLYKTNING.beskrivelse;
            } else if(FORVALTNING_UTENFOR_VERGEMAAL.type.equals(type)) {
                return FORVALTNING_UTENFOR_VERGEMAAL.beskrivelse;
            } else if(STADFESTET_FREMTIDSFULLMAKT.type.equals(type)) {
                return STADFESTET_FREMTIDSFULLMAKT.beskrivelse;
            } else if(MINDREAARIG.type.equals(type)) {
                return MINDREAARIG.beskrivelse;
            } else if(MIDLERTIDIG_FOR_MINDREAARIG.type.equals(type)) {
                return MIDLERTIDIG_FOR_MINDREAARIG.beskrivelse;
            } else if(VOKSEN.type.equals(type)) {
                return VOKSEN.beskrivelse;
            } else if(MIDLERTIDIG_FOR_VOKSEN.type.equals(type)) {
                return MIDLERTIDIG_FOR_VOKSEN.beskrivelse;
            } else {
                return null;
            }
        }

}
