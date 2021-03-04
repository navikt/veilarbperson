package no.nav.veilarbperson.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Telefon {
    private String prioritet;
    private String telefonNr;
    private String master;
}
