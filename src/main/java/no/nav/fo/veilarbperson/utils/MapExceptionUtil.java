package no.nav.fo.veilarbperson.utils;

import no.nav.apiapp.feil.Feil;
import no.nav.apiapp.feil.FeilType;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;

public class MapExceptionUtil {

    public static Feil map(Throwable error) {

        FeilType feilType;
        if (error instanceof HentPersonPersonIkkeFunnet) {
            feilType = FeilType.FINNES_IKKE;
        } else if (error instanceof HentPersonSikkerhetsbegrensning) {
            feilType = FeilType.INGEN_TILGANG;
        } else {
            feilType = FeilType.UKJENT;
        }

        return new Feil(feilType, error);
    }
}
