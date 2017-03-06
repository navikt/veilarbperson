package no.nav.fo.veilarbperson.consumer.digitalkontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.*;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSKontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonRequest;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonResponse;

public class DigitalKontaktinformasjonService {

    private final DigitalKontaktinformasjonV1 digitalKontaktinformasjonV1;

    public DigitalKontaktinformasjonService(DigitalKontaktinformasjonV1 digitalKontaktinformasjonV1) {
        this.digitalKontaktinformasjonV1 = digitalKontaktinformasjonV1;
    }

    public DigitalKontaktinformasjon hentDigitalKontaktinformasjon(String fodselsnummer) throws
            HentDigitalKontaktinformasjonSikkerhetsbegrensing,
            HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet,
            HentDigitalKontaktinformasjonPersonIkkeFunnet {

        WSHentDigitalKontaktinformasjonRequest request = new WSHentDigitalKontaktinformasjonRequest()
                .withPersonident(fodselsnummer);

        WSHentDigitalKontaktinformasjonResponse wsResponse = digitalKontaktinformasjonV1.hentDigitalKontaktinformasjon(request);
        return getDigitalKontaktinformason(wsResponse);
    }

    private DigitalKontaktinformasjon getDigitalKontaktinformason(WSHentDigitalKontaktinformasjonResponse response) {
        WSKontaktinformasjon wsKontaktinformasjon = response.getDigitalKontaktinformasjon();
        return new DigitalKontaktinformasjon()
                .withEpost(wsKontaktinformasjon.getEpostadresse().getValue())
                .withTelefon(wsKontaktinformasjon.getMobiltelefonnummer().getValue());
    }

}
