package no.nav.fo.veilarbperson.consumer.kodeverk;

import no.nav.tjeneste.virksomhet.kodeverk.v2.HentKodeverkHentKodeverkKodeverkIkkeFunnet;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLEnkeltKodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLKodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.XMLHentKodeverkRequest;

public class KodeverkService {

    private final KodeverkPortType kodverkPortType;

    public KodeverkService(KodeverkPortType kodverkPortType) {
        this.kodverkPortType = kodverkPortType;
    }

    Kodeverk hentKodeverk(XMLHentKodeverkRequest kodeverkRequest) throws HentKodeverkHentKodeverkKodeverkIkkeFunnet {
        XMLKodeverk kodeverkResponse = kodverkPortType.hentKodeverk(kodeverkRequest).getKodeverk();
        if (kodeverkResponse instanceof XMLEnkeltKodeverk) {
            return new Kodeverk((XMLEnkeltKodeverk) kodeverkResponse);
        } else {
            throw new HentKodeverkHentKodeverkKodeverkIkkeFunnet("Kodeverk er ikke av typen XMLEnkeltKodeverk og er ikke støttet: " + kodeverkRequest);
        }
    }
}
