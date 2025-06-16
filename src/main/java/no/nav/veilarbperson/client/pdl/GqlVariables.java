package no.nav.veilarbperson.client.pdl;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.common.types.identer.Fnr;

import java.util.List;

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
        private List<Fnr> identer;
        private boolean historikk;
    }

    @Data
    @AllArgsConstructor
    public static class HentGeografiskTilknytning {
        private Fnr ident;
    }

    @Data
    @AllArgsConstructor
    public static class HentTilrettelagtKommunikasjon {
        private Fnr ident;
    }

    @Data
    @AllArgsConstructor
    public static class HentAdressebeskyttelse {
        private Fnr ident;
    }

    @Data
    @AllArgsConstructor
    public static class HentFoedselsdato {
        private Fnr ident;
    }

}
