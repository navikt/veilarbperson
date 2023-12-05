package no.nav.veilarbperson.client.pdl.domain;

import no.nav.common.types.identer.Fnr;

public record PdlRequest(Fnr fnr, String behandlingsnummer) {
}


