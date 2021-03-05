package no.nav.veilarbperson.client.pdl;

import java.util.Map;

public class VergemaalEllerFullmaktConstants {

    public static final Map<String, String> saksType = Map.of(
            "ensligMindreaarigAsylsoeker","Enslig mindreårig asylsøker",
            "ensligMindreaarigFlyktning","Enslig mindreårig flyktning inklusive midlertidige saker for denne gruppen",
            "forvaltningUtenforVergemaal","Forvaltning utenfor vergemål",
            "stadfestetFremtidsfullmakt","Fremtidsfullmakt",
            "mindreaarig","Mindreårig (unntatt EMF)",
            "midlertidigForMindreaarig","Mindreårig midlertidig (unntatt EMF)",
            "voksen","Voksen",
            "midlertidigForVoksen","Voksen midlertidig"
    );

    public static final Map<String, String> omfangType = Map.of(
            "personligeOgOekonomiskeInteresser", "Ivareta personens interesser innenfor det personlige og økonomiske området",
            "utlendingssakerPersonligeOgOekonomiskeInteresser", "Ivareta personens interesser innenfor det personlige og økonomiske området herunder utlendingssaken (kun for EMA)",
            "personligeInteresser", "Ivareta personens interesser innenfor det personlige området",
            "oekonomiskeInteresser", "Ivareta personens interesser innenfor det økonomiske området"
    );
}
