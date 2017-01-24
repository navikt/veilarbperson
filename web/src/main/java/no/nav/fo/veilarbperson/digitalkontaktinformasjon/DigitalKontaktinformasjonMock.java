package no.nav.fo.veilarbperson.digitalkontaktinformasjon;

import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.*;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.*;

class DigitalKontaktinformasjonMock implements DigitalKontaktinformasjonV1 {

    @Override
    public WSHentSikkerDigitalPostadresseBolkResponse hentSikkerDigitalPostadresseBolk(
            WSHentSikkerDigitalPostadresseBolkRequest wsHentSikkerDigitalPostadresseBolkRequest)
            throws HentSikkerDigitalPostadresseBolkForMangeForespoersler,
            HentSikkerDigitalPostadresseBolkSikkerhetsbegrensing {
        return null;
    }

    @Override
    public WSHentPrintsertifikatResponse hentPrintsertifikat(WSHentPrintsertifikatRequest wsHentPrintsertifikatRequest) {
        return null;
    }

    @Override
    public void ping() {

    }

    @Override
    public WSHentSikkerDigitalPostadresseResponse hentSikkerDigitalPostadresse(
            WSHentSikkerDigitalPostadresseRequest wsHentSikkerDigitalPostadresseRequest) throws
            HentSikkerDigitalPostadresseKontaktinformasjonIkkeFunnet,
            HentSikkerDigitalPostadresseSikkerhetsbegrensing,
            HentSikkerDigitalPostadressePersonIkkeFunnet {
        return null;
    }

    @Override
    public WSHentDigitalKontaktinformasjonBolkResponse hentDigitalKontaktinformasjonBolk(
            WSHentDigitalKontaktinformasjonBolkRequest wsHentDigitalKontaktinformasjonBolkRequest) throws
            HentDigitalKontaktinformasjonBolkForMangeForespoersler,
            HentDigitalKontaktinformasjonBolkSikkerhetsbegrensing {
        return null;
    }

    @Override
    public WSHentDigitalKontaktinformasjonResponse hentDigitalKontaktinformasjon(
            WSHentDigitalKontaktinformasjonRequest wsHentDigitalKontaktinformasjonRequest) throws
            HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet,
            HentDigitalKontaktinformasjonSikkerhetsbegrensing,
            HentDigitalKontaktinformasjonPersonIkkeFunnet {
        return null;
    }
}
