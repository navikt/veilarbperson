package no.nav.veilarbperson.client.pdl.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@Accessors(chain = true)
public class Familiemedlem implements Barn{
    String fornavn;
    LocalDate fodselsdato;
    LocalDate dodsdato;
    Boolean erEgenAnsatt;
    boolean harVeilederTilgang;
    String gradering;       //diskresjonskode
    RelasjonsBosted relasjonsBosted;
}
