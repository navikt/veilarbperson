package no.nav.veilarbperson.client.pdl.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.common.types.identer.Fnr;

import java.time.LocalDate;

@Data
@Accessors(chain = true)
public class Familiemedlem {
    String fornavn;
    String mellomnavn;
    String etternavn;
    String forkortetNavn;
    Fnr fodselsnummer;
    LocalDate fodselsdato;
    String kjonn;
    LocalDate dodsdato;
    AdressebeskyttelseGradering gradering;       //diskresjonskode
    boolean harSammeBosted;
}
