package no.nav.veilarbperson.client.representasjon;

import lombok.*;

import java.util.List;

@Data
class Fullmakt {
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

@Data
public class RepresentasjonFullmakt{
    private List<Fullmakt> representasjonFullmakt;
}