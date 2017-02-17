package no.nav.fo.veilarbperson.consumer.kodeverk;

import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.XMLHentKodeverkRequest;

public class KodeverkRequestDO extends XMLHentKodeverkRequest {

    public String toString(){
        return this.getNavn() + this.getSpraak() + this.getVersjonsnummer();
    }
}
