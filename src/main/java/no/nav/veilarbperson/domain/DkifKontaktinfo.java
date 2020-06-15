package no.nav.veilarbperson.domain;

import lombok.Data;

@Data
public class DkifKontaktinfo {
    String personident;
    boolean kanVarsles;
    boolean reservert;
    String epostadresse;
    String mobiltelefonnummer;
}