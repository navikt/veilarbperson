package no.nav.veilarbperson.domain;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.veilarbperson.client.representasjon.ReprFullmaktData;

import java.time.LocalDate;
import java.util.List;

@Data
@Accessors(chain=true)
public class FullmaktData {
    public List<Fullmakt> fullmakt;

    @Data
    public static class Fullmakt {
        public List<VergeOgFullmaktData.Fullmakt> fullmakt;
        private String fullmaktsgiver;
        private String fullmektig;
        private List<OmraadeMedHandling> omraade;
        private LocalDate gyldigFraOgMed;
        private LocalDate gyldigTilOgMed;
        private String fullmaktsgiverNavn;
        private String fullmektigsNavn;
        private String status;
    }

    @Data
    public static class OmraadeMedHandling {
        private String tema;
        private List<OmraadeHandlingType> handling;
    }

    public enum OmraadeHandlingType {
        LES("LES"),
        KOMMUNISER("KOMMUNISER"),
        SKRIV("SKRIV");

        public final String name;

        OmraadeHandlingType(String handlingType) {
            this.name = handlingType;
        }

        public OmraadeHandlingType getOmraadeHandlingType(String handlingType) {
            return OmraadeHandlingType.valueOf(handlingType);
        }
    }

}

