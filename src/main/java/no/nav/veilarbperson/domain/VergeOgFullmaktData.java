package no.nav.veilarbperson.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.veilarbperson.client.pdl.domain.VergemaalEllerFullmaktOmfangType;
import no.nav.veilarbperson.client.pdl.domain.Vergetype;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain=true)
public class VergeOgFullmaktData {

    public List<VergemaalEllerFremtidsfullmakt> vergemaalEllerFremtidsfullmakt;
    public List<Fullmakt> fullmakt;
    public List<RepresentasjonFullmakt> representasjonFullmakt;

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

    @Data
    public static class Omraade {
        public String kode;
        public String beskrivelse;
    }

    @Data
    public static class Fullmakt {
        private String motpartsPersonident;
        private Navn motpartsPersonNavn;
        private String motpartsRolle;
        private List<Omraade> omraader;
        private LocalDate gyldigFraOgMed;
        private LocalDate gyldigTilOgMed;
    }

    @Data
    public static class RepresentasjonFullmakt {
        private int fullmaktId;
        private String registrert;
        private String registrertAv;
        private String fullmaktsgiver;
        private String fullmektig;
        private List<OmraadeMedHandling> omraade;
        private LocalDate gyldigFraOgMed;
        private LocalDate gyldigTilOgMed;
        private UUID opplysningsId;
        private Long endringsId;
        private String fullmaktsgiverNavn;
        private String fullmektigsNavn;
        private boolean opphoert;
        private String kilde;
        private String status;
    }

    @Data
    class OmraadeMedHandling {
        private String tema;
        private List<OmraadeHandlingType> handling;
    }

    enum OmraadeHandlingType {
        LES,
        KOMMUNISER,
        SKRIV
    }
}
