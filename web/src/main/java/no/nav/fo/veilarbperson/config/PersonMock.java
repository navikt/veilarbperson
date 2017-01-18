package no.nav.fo.veilarbperson.config;

import no.nav.tjeneste.virksomhet.person.v2.*;
import no.nav.tjeneste.virksomhet.person.v2.meldinger.*;

public class PersonMock implements PersonV2 {
    @Override
    public void ping() {

    }

    @Override
    public WSHentPersonnavnBolkResponse hentPersonnavnBolk(WSHentPersonnavnBolkRequest hentPersonnavnBolkRequest) {
        return null;
    }

    @Override
    public WSHentSikkerhetstiltakResponse hentSikkerhetstiltak(WSHentSikkerhetstiltakRequest hentSikkerhetstiltakRequest) throws HentSikkerhetstiltakPersonIkkeFunnet {
        return null;
    }

    @Override
    public WSHentKjerneinformasjonResponse hentKjerneinformasjon(WSHentKjerneinformasjonRequest hentKjerneinformasjonRequest) throws HentKjerneinformasjonSikkerhetsbegrensning, HentKjerneinformasjonPersonIkkeFunnet {
        return null;
    }
}
