package no.nav.veilarbperson.client.pdl.domain;

public enum Omfangtyper {

     PERSONLIGE_OG_OEKONOMISKE_INTERESSER("personligeOgOekonomiskeInteresser", "Ivareta personens interesser innenfor det personlige og økonomiske området"),
     UTLENDINGSSAKER("utlendingssakerPersonligeOgOekonomiskeInteresser", "Ivareta personens interesser innenfor det personlige og økonomiske området herunder utlendingssaken (kun for EMA)"),
     PERSONLIGE_INTERESSER("personligeInteresser", "Ivareta personens interesser innenfor det personlige området"),
     OEKONOMISKE_INTERESSER("oekonomiskeInteresser", "Ivareta personens interesser innenfor det økonomiske området");

     private String type;
     private String beskrivelse;

     Omfangtyper(String type, String beskrivelse) {
         this.type = type;
         this.beskrivelse = beskrivelse;
     }

     public static String getBeskrivelse(String type) {
         if(PERSONLIGE_OG_OEKONOMISKE_INTERESSER.type.equals(type)) {
             return PERSONLIGE_OG_OEKONOMISKE_INTERESSER.beskrivelse;
         } else if(UTLENDINGSSAKER.type.equals(type)) {
             return UTLENDINGSSAKER.beskrivelse;
         } else if(PERSONLIGE_INTERESSER.type.equals(type)) {
             return PERSONLIGE_INTERESSER.beskrivelse;
         } else if(OEKONOMISKE_INTERESSER.type.equals(type)) {
             return OEKONOMISKE_INTERESSER.beskrivelse;
         } else {
             return null;
         }
     }
}
