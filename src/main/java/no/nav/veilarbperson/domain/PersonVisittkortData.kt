package no.nav.veilarbperson.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.veilarbperson.client.pdl.domain.Telefon;

import java.util.List;

@Data
@Accessors(chain = true)
public class PersonVisittkortData {
    private String fornavn;
    private String mellomnavn;
    private String etternavn;
    private String fodselsdato;
    private String dodsdato;
    private String kjonn;
    private String diskresjonskode;
    private boolean egenAnsatt;
    private String sikkerhetstiltak;
    private List<Telefon> telefon;
}
