package no.nav.veilarbperson.client.digitalkontaktinformasjon;

import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonSikkerhetsbegrensing;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSEpostadresse;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSKontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSMobiltelefonnummer;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonRequest;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonResponse;
import no.nav.veilarbperson.config.CacheConfig;
import org.springframework.cache.annotation.Cacheable;

import java.util.Optional;

public class DigitalKontaktinformasjonService {

    private final DigitalKontaktinformasjonV1 digitalKontaktinformasjonV1;

    public DigitalKontaktinformasjonService(DigitalKontaktinformasjonV1 digitalKontaktinformasjonV1) {
        this.digitalKontaktinformasjonV1 = digitalKontaktinformasjonV1;
    }


    @Cacheable(CacheConfig.DIGITAL_KONTAKTINFO)
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
