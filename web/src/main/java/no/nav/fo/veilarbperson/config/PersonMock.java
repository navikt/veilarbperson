package no.nav.fo.veilarbperson.config;

import no.nav.tjeneste.virksomhet.person.v2.*;
import no.nav.tjeneste.virksomhet.person.v2.meldinger.*;
import no.nav.tjeneste.virksomhet.person.v2.meldinger.HentKjerneinformasjonResponse;
import no.nav.tjeneste.virksomhet.person.v2.meldinger.HentPersonnavnBolkResponse;
import no.nav.tjeneste.virksomhet.person.v2.meldinger.HentSikkerhetstiltakResponse;

public class PersonMock implements PersonV2 {
    @Override
    public void ping() {

    }

    @Override
    public HentPersonnavnBolkResponse hentPersonnavnBolk(HentPersonnavnBolkRequest hentPersonnavnBolkRequest) {
        return null;
    }

    @Override
    public HentSikkerhetstiltakResponse hentSikkerhetstiltak(HentSikkerhetstiltakRequest hentSikkerhetstiltakRequest) throws HentSikkerhetstiltakPersonIkkeFunnet {
        return null;
    }

    @Override
    public HentKjerneinformasjonResponse hentKjerneinformasjon(HentKjerneinformasjonRequest hentKjerneinformasjonRequest) throws HentKjerneinformasjonSikkerhetsbegrensning, HentKjerneinformasjonPersonIkkeFunnet {
        return null;
    }
}
