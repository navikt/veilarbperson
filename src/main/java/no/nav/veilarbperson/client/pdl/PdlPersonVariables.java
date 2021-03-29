package no.nav.veilarbperson.client.pdl;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.common.types.identer.Fnr;

public class PdlPersonVariables {

    @Data
    @AllArgsConstructor
    public static class HentPersonVariables {
        private Fnr ident;
        private boolean historikk;
    }

    @Data
    @AllArgsConstructor
    public static class HentPersonBolkVariables {
        private Fnr[] identer;
        private boolean historikk;
    }

    @Data
    @AllArgsConstructor
    public static class HentGeografiskTilknytningVariables {
        private Fnr ident;
    }
}
