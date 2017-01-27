package no.nav.fo.veilarbperson.consumer.person;


import no.nav.tjeneste.pip.egen.ansatt.v1.*;

class EgenAnsattMock implements EgenAnsattV1 {
    @Override
    public void ping() {

    }

    @Override
    public WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse hentErEgenAnsattEllerIFamilieMedEgenAnsatt(WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest request) {
        return null;
    }
}
