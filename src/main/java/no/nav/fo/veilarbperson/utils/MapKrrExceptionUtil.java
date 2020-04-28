package no.nav.fo.veilarbperson.utils;

import no.nav.apiapp.feil.Feil;
import no.nav.apiapp.feil.FeilType;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonSikkerhetsbegrensing;

public class MapKrrExceptionUtil {

    public static Feil map(Throwable error) {

        FeilType feilType;
        if (error instanceof HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet) {
            feilType = FeilType.FINNES_IKKE;
        } else if (error instanceof HentDigitalKontaktinformasjonPersonIkkeFunnet) {
            feilType = FeilType.INGEN_TILGANG;
        } else if (error instanceof HentDigitalKontaktinformasjonSikkerhetsbegrensing) {
            feilType = FeilType.INGEN_TILGANG;
        } else {
            feilType = FeilType.UKJENT;
        }

        return new Feil(feilType, error);
    }
}
