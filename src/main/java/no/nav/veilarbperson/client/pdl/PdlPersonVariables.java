package no.nav.veilarbperson.client.pdl;

import lombok.AllArgsConstructor;
import lombok.Data;

public class PdlPersonVariables {

    @Data
    @AllArgsConstructor
    public static class HentPersonVariables {
        private String ident;
        private boolean historikk;
    }

    @Data
    @AllArgsConstructor
    public static class HentPersonBolkVariables {
        private String[] identer;
        private boolean historikk;
    }

    @Data
    @AllArgsConstructor
    public static class HentGeografiskTilknytningVariables {
        private String ident;
    }
}
