package no.nav.veilarbperson.client.tps;


import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1;
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest;
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse;

class EgenAnsattMock implements EgenAnsattV1 {
    @Override
    public void ping() {

    }

    @Override
    public WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse hentErEgenAnsattEllerIFamilieMedEgenAnsatt(WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest request) {
        return null;
    }
}
