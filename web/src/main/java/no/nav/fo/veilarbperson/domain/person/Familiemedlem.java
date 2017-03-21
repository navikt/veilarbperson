package no.nav.fo.veilarbperson.domain.person;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Builder;

@EqualsAndHashCode(callSuper = true)
@Data
public class Familiemedlem extends Person{

    private Boolean harSammeBosted;

    @Builder
    private Familiemedlem(String fornavn,
                          String mellomnavn,
                          String etternavn,
                          String sammensattnavn,
                          String fodselsnummer,
                          String fodselsdato,
                          String kjonn,
                          String dodsdato,
                          Boolean harSammeBosted){
        super(fornavn, mellomnavn, etternavn, sammensattnavn, fodselsnummer, fodselsdato, kjonn, dodsdato);

        this.harSammeBosted = harSammeBosted;
    }

}