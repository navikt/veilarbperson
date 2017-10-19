package no.nav.fo.veilarbperson.consumer.organisasjonenhet;


import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.*;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.meldinger.*;

class OrganisasjonEnhetMock implements OrganisasjonEnhetV2 {

    @Override
    public WSHentFullstendigEnhetListeResponse hentFullstendigEnhetListe(WSHentFullstendigEnhetListeRequest wsHentFullstendigEnhetListeRequest) {
        return null;
    }

    @Override
    public WSHentOverordnetEnhetListeResponse hentOverordnetEnhetListe(WSHentOverordnetEnhetListeRequest wsHentOverordnetEnhetListeRequest) throws HentOverordnetEnhetListeEnhetIkkeFunnet {
        return null;
    }

    @Override
    public WSFinnNAVKontorResponse finnNAVKontor(WSFinnNAVKontorRequest wsFinnNAVKontorRequest) throws FinnNAVKontorUgyldigInput {
        return null;
    }

    @Override
    public WSHentEnhetBolkResponse hentEnhetBolk(WSHentEnhetBolkRequest wsHentEnhetBolkRequest) {
        return null;
    }

    @Override
    public void ping() {

    }
}
