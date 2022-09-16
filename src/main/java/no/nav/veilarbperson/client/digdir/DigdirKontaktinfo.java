package no.nav.veilarbperson.client.digdir;

import lombok.Data;

@Data
public class DigdirKontaktinfo {
    String personident;
    boolean kanVarsles;
    boolean reservert;
    String epostadresse;
    String epostSistOppdatert;
    String mobiltelefonnummer;
    String mobilSistOppdatert;
    String spraak;
}
