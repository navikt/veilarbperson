package no.nav.veilarbperson.client.pdl;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.common.types.identer.Fnr;

public class GqlVariables {

    @Data
    @AllArgsConstructor
    public static class HentPerson {
        private Fnr ident;
        private boolean historikk;
    }

    @Data
    @AllArgsConstructor
    public static class HentPersonBolk {
        private Fnr[] identer;
        private boolean historikk;
    }

    @Data
    @AllArgsConstructor
    public static class HenGeografiskTilknytning {
        private Fnr ident;
    }
}
