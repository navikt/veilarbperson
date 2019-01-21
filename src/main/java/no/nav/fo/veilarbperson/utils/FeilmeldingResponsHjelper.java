package no.nav.fo.veilarbperson.utils;

import no.nav.apiapp.feil.Feil;
import no.nav.apiapp.feil.IngenTilgang;
import no.nav.apiapp.feil.UgyldigRequest;
import no.nav.tjeneste.virksomhet.person.v3.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.HentPersonSikkerhetsbegrensning;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.instanceOf;

public class FeilmeldingResponsHjelper {

    public static Feil feilHanteringHjelper (Throwable error) {
        return Match(error).of(
                Case($(instanceOf(HentPersonPersonIkkeFunnet.class)), UgyldigRequest::new),
                Case($(instanceOf(HentPersonSikkerhetsbegrensning.class)),(e) -> new IngenTilgang("Bruker har ikke tilgang til fnr: " + e)));

    }
}
