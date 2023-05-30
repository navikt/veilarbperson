package no.nav.veilarbperson.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PersonDataKontoregister {
    String kontonummer;
}
