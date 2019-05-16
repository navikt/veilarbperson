package no.nav.fo.veilarbperson.utils;

import no.nav.apiapp.feil.Feil;
import no.nav.apiapp.feil.FeilType;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;

public class MapExceptionUtil {

    public static Feil map(Throwable error) {

        if (error instanceof HentPersonPersonIkkeFunnet) {
            return new Feil(FeilType.FINNES_IKKE);
        } else if (error instanceof HentPersonSikkerhetsbegrensning) {
            return new Feil(FeilType.INGEN_TILGANG);
        } else {
            return new Feil(FeilType.UKJENT);
        }

    }
}
