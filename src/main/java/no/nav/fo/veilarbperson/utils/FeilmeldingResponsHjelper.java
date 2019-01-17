package no.nav.fo.veilarbperson.utils;

import no.nav.fo.veilarbperson.domain.Feilmelding;
import no.nav.tjeneste.virksomhet.person.v3.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.HentPersonSikkerhetsbegrensning;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class FeilmeldingResponsHjelper {

    public static Response lagResponsForSluttbrukerIkkeTilgangFeilmelding(String fodselsnummer) {

        Feilmelding feilmelding = new Feilmelding("Bruker har ikke tilgang til fnr: " + fodselsnummer, "");

        return Response
                .status(Status.UNAUTHORIZED)
                .entity(feilmelding)
                .build();

    }

    public static Response lagResponsForIkkeTilgangFeilmelding(HentPersonSikkerhetsbegrensning hentPersonSikkerhetsbegrensning,
                                                               String fodselsnummer) {

        Feilmelding feilmelding = new Feilmelding("Saksbehandler har ikke tilgang til fnr: " + fodselsnummer,
                hentPersonSikkerhetsbegrensning.toString());

        return Response
                .status(Status.UNAUTHORIZED)
                .entity(feilmelding)
                .build();

    }

    public static Response lagResponsForIkkeFunnetFeilmelding(HentPersonPersonIkkeFunnet hentPersonPersonIkkeFunnet,
                                                              String fodselsnummer) {

        Feilmelding feilmelding = new Feilmelding("Fant ikke person med fnr: " + fodselsnummer,
                hentPersonPersonIkkeFunnet.toString());

        return Response
                .status(Status.NOT_FOUND)
                .entity(feilmelding)
                .build();

    }

}
