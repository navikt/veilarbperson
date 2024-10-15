package no.nav.veilarbperson.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.veilarbperson.client.pdl.domain.VergemaalEllerFullmaktOmfangType;
import no.nav.veilarbperson.client.pdl.domain.Vergetype;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain=true)
public class VergeData {

    public List<VergemaalEllerFremtidsfullmakt> vergemaalEllerFremtidsfullmakt;

    @Data
    public static class Navn {
        private String fornavn;
        private String mellomnavn;
        private String etternavn;
        private String forkortetNavn;
    }

    @Data
    public static class VergeNavn {
        private String fornavn;
        private String mellomnavn;
        private String etternavn;
    }

    @Data
    public static class VergeEllerFullmektig {
        private VergeNavn navn;
        private String motpartsPersonident;
        private VergemaalEllerFullmaktOmfangType omfang;
    }

    @Data
    public static class VergemaalEllerFremtidsfullmakt {
        private Vergetype type;
        private String embete;
        private VergeEllerFullmektig vergeEllerFullmektig;
        private Folkeregistermetadata folkeregistermetadata;
    }

    @Data
    public static class Folkeregistermetadata {
        public LocalDateTime ajourholdstidspunkt;
        public LocalDateTime gyldighetstidspunkt;
    }
}
