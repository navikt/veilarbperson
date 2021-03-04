package no.nav.veilarbperson.client.pdl.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@Accessors(chain = true)
public class Familiemedlem {
    String fornavn;
    String mellomnavn;
    String etternavn;
    String forkortetNavn;
    String fodselsnummer;
    LocalDate fodselsdato;
    String kjonn;
    LocalDate dodsdato;
    boolean harSammeBosted;
}
