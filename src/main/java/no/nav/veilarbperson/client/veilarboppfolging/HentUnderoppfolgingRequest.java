package no.nav.veilarbperson.client.veilarboppfolging;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.common.types.identer.Fnr;

public record HentUnderoppfolgingRequest(
        Fnr fnr
) {
}
