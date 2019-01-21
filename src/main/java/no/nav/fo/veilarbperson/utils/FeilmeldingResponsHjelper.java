package no.nav.fo.veilarbperson.utils;

import no.nav.apiapp.feil.IngenTilgang;
import no.nav.apiapp.feil.UgyldigRequest;
import no.nav.tjeneste.virksomhet.person.v3.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.HentPersonSikkerhetsbegrensning;


public class FeilmeldingResponsHjelper {

    public static RuntimeException feilHanteringHjelper (Throwable error) {
        if(error instanceof HentPersonPersonIkkeFunnet) {
            return new UgyldigRequest();
        }

        if(error instanceof HentPersonSikkerhetsbegrensning) {
           return new IngenTilgang("Bruker har ikke tilgang til fnr: " + error);
        }

        return new RuntimeException(error);
    }
}
