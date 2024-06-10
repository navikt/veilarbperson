package no.nav.veilarbperson.client.representasjon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Fullmakt {
    private int fullmaktId;
    private String registrert;
    private String registrertAv;
    private String fullmaktsgiver;
    private String fullmektig;
    private String omraade;
    private String gyldigFraOgMed;
    private String gyldigTilOgMed;
    private String opplysningsId;
    private int endringsId;
    private String fullmaktsgiverNavn;
    private String fullmektigsNavn;
    private boolean opphoert;
}
