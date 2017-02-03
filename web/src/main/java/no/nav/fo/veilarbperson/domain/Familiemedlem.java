package no.nav.fo.veilarbperson.domain;

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
                          String personnummer,
                          String fodselsdato,
                          String kjonn,
                          String dodsdato,
                          Boolean harSammeBosted){
        super(fornavn, mellomnavn, etternavn, sammensattnavn, personnummer, fodselsdato, kjonn, dodsdato);

        this.harSammeBosted = harSammeBosted;




    }


}