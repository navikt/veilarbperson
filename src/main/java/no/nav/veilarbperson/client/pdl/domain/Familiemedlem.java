package no.nav.veilarbperson.client.pdl.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Familiemedlem {
    String fornavn;
    String mellomnavn;
    String etternavn;
    String sammensattNavn;
    String fodselsnummer;
    String fodselsdato;
    String kjonn;
    String dodsdato;
    boolean harSammeBosted;
}
