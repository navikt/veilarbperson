package no.nav.veilarbperson.client.representasjon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Fullmakt {
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
    private String endretAv;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class OmraadeMedHandling {
    private String tema;
    private List<OmraadeHandlingType> handling;
}

enum OmraadeHandlingType {
    LES,
    KOMMUNISER,
    SKRIV
}