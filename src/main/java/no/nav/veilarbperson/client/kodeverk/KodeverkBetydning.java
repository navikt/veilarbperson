package no.nav.veilarbperson.client.kodeverk;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
class KodeverkBetydning {
    String gyldigFra; // Dato: 1900-01-01
    String gyldigTil; // Dato: 9999-12-31
    String beskrivelseNb; // Beskrivelse på bokmål
}
