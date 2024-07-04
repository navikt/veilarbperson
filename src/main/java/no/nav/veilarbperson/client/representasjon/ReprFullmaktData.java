package no.nav.veilarbperson.client.representasjon;

import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Data
@Accessors(chain=true)

public class ReprFullmaktData {
    private List<Fullmakt> fullmakter;

    @Data
    public static class Fullmakt {
        private Long fullmaktId;
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
        private String endretAv;
    }

    @Data
    public static class OmraadeMedHandling {
        private String tema;
        private List<OmraadeHandlingType> handling;
    }

    public enum OmraadeHandlingType {
        LES,
        KOMMUNISER,
        SKRIV
    }
}






