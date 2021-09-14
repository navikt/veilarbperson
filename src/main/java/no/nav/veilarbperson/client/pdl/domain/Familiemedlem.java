package no.nav.veilarbperson.client.pdl.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.common.types.identer.Fnr;

@Data
@Accessors(chain = true)
public class Familiemedlem {
    String fornavn;
    String mellomnavn;
    String etternavn;
    String forkortetNavn;
    Fnr fodselsnummer;
    String fodselsdato;
    String kjonn;
    String dodsdato;
    boolean erEgenAnsatt;
    boolean harVeilederTilgang;
    AdressebeskyttelseGradering gradering;       //diskresjonskode
    boolean harSammeBosted;
}
