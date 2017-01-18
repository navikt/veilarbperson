package no.nav.fo.veilarbperson.digitalkontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.*;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSKontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonRequest;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonResponse;
import org.springframework.beans.factory.annotation.Autowired;

public class DigitalKontaktinformasjonService {

    @Autowired
    private DigitalKontaktinformasjonV1 digitalKontaktinformasjonV1;

    public DigitalKontaktinformasjon hentDigitalKontaktinformasjon(String personnummer) throws HentDigitalKontaktinformasjonSikkerhetsbegrensing, HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet, HentDigitalKontaktinformasjonPersonIkkeFunnet {
        WSHentDigitalKontaktinformasjonRequest request = new WSHentDigitalKontaktinformasjonRequest()
                .withPersonident(personnummer);

        WSHentDigitalKontaktinformasjonResponse wsResponse = digitalKontaktinformasjonV1.hentDigitalKontaktinformasjon(request);
        return getDigitalKontaktinformason(wsResponse);
    }

    private DigitalKontaktinformasjon getDigitalKontaktinformason(WSHentDigitalKontaktinformasjonResponse response) {
        WSKontaktinformasjon wsKontaktinformasjon = response.getDigitalKontaktinformasjon();
        return new DigitalKontaktinformasjon()
                .medEpost(wsKontaktinformasjon.getEpostadresse().getValue())
                .medTelefon(wsKontaktinformasjon.getMobiltelefonnummer().getValue());
    }

}
