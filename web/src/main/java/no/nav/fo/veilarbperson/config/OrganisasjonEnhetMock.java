package no.nav.fo.veilarbperson.config;

import no.nav.tjeneste.virksomhet.organisasjonenhet.v1.*;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v1.meldinger.*;

public class OrganisasjonEnhetMock implements OrganisasjonEnhetV1 {
    @Override
    public WSFinnArbeidsfordelingForEnhetBolkResponse finnArbeidsfordelingForEnhetBolk(
            WSFinnArbeidsfordelingForEnhetBolkRequest request)
            throws FinnArbeidsfordelingForEnhetBolkUgyldigInput {
        return null;
    }

    @Override
    public WSFinnNAVKontorForGeografiskNedslagsfeltBolkResponse finnNAVKontorForGeografiskNedslagsfeltBolk(
            WSFinnNAVKontorForGeografiskNedslagsfeltBolkRequest request)
            throws FinnNAVKontorForGeografiskNedslagsfeltBolkUgyldigInput {
        return null;
    }

    @Override
    public WSFinnArbeidsfordelingBolkResponse finnArbeidsfordelingBolk(
            WSFinnArbeidsfordelingBolkRequest request)
            throws FinnArbeidsfordelingBolkUgyldigInput {
        return null;
    }

    @Override
    public WSFinnEnheterForArbeidsfordelingBolkResponse finnEnheterForArbeidsfordelingBolk(
            WSFinnEnheterForArbeidsfordelingBolkRequest request)
            throws FinnEnheterForArbeidsfordelingBolkUgyldigInput {
        return null;
    }

    @Override
    public WSHentEnhetBolkResponse hentEnhetBolk(WSHentEnhetBolkRequest request) throws HentEnhetBolkUgyldigInput {
        return null;
    }

    @Override
    public WSHentFullstendigEnhetListeResponse hentFullstendigEnhetListe(WSHentFullstendigEnhetListeRequest request) {
        return null;
    }

    @Override
    public void ping() {

    }
}
