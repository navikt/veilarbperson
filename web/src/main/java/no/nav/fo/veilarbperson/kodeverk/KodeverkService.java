package no.nav.fo.veilarbperson.kodeverk;

import no.nav.tjeneste.virksomhet.kodeverk.v2.HentKodeverkHentKodeverkKodeverkIkkeFunnet;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.*;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.XMLHentKodeverkRequest;
import org.springframework.beans.factory.annotation.Autowired;

public class KodeverkService {

    @Autowired
    private KodeverkPortType kodverkPortType;

    public Kodeverk hentKodeverk(XMLHentKodeverkRequest kodeverkRequest) throws HentKodeverkHentKodeverkKodeverkIkkeFunnet {
        XMLKodeverk kodeverkResponse = kodverkPortType.hentKodeverk(kodeverkRequest).getKodeverk();
        if (kodeverkResponse instanceof XMLEnkeltKodeverk) {
            return new Kodeverk((XMLEnkeltKodeverk) kodeverkResponse);
        } else {
            throw new HentKodeverkHentKodeverkKodeverkIkkeFunnet("Kodeverk er ikke av typen XMLEnkeltKodeverk og er ikke støttet: " + kodeverkRequest);
        }
    }
}
