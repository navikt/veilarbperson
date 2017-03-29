package no.nav.fo.veilarbperson.consumer.digitalkontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.*;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonRequest;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonResponse;

import java.util.Optional;

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
        return getDigitalKontaktinformasjon(wsResponse);
    }

    private DigitalKontaktinformasjon getDigitalKontaktinformasjon(WSHentDigitalKontaktinformasjonResponse response) {
        WSKontaktinformasjon wsKontaktinformasjon = response.getDigitalKontaktinformasjon();
        final Optional<WSEpostadresse> epostadresse = Optional.ofNullable(wsKontaktinformasjon.getEpostadresse());
        final Optional<WSMobiltelefonnummer> telefon = Optional.ofNullable(wsKontaktinformasjon.getMobiltelefonnummer());
        return new DigitalKontaktinformasjon()
                .withEpost(epostadresse
                        .map(WSEpostadresse::getValue)
                        .orElse(null))
                .withTelefon(telefon
                        .map(WSMobiltelefonnummer::getValue)
                        .orElse(null));


    }
}
