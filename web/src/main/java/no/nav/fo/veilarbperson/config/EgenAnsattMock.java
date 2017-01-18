package no.nav.fo.veilarbperson.config;


import no.nav.tjeneste.pip.egenansatt.v1.EgenAnsattV1;
import no.nav.tjeneste.pip.egenansatt.v1.meldinger.HentErEgenAnsattEllerIFamilieMedEgenAnsattRequest;
import no.nav.tjeneste.pip.egenansatt.v1.meldinger.HentErEgenAnsattEllerIFamilieMedEgenAnsattResponse;

public class EgenAnsattMock implements EgenAnsattV1 {
    @Override
    public void ping() {

    }

    @Override
    public HentErEgenAnsattEllerIFamilieMedEgenAnsattResponse hentErEgenAnsattEllerIFamilieMedEgenAnsatt(HentErEgenAnsattEllerIFamilieMedEgenAnsattRequest request) {
        return null;
    }
}
