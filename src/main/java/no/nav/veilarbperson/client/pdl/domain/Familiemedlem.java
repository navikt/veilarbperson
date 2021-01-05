package no.nav.veilarbperson.client.pdl.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Familiemedlem {
    String fornavn;
    String mellomnavn;
    String etternavn;
    String forkortetnavn;
    String fodselsnummer;
    String fodselsdato;
    String kjonn;
    String dodsdato;

    boolean harSammeBosted;

    public Familiemedlem copy() {
        return new Familiemedlem()
                .setFornavn(fornavn)
                .setMellomnavn(mellomnavn)
                .setEtternavn(etternavn)
                .setForkortetnavn(forkortetnavn)
                .setFodselsnummer(fodselsnummer)
                .setFodselsdato(fodselsdato)
                .setKjonn(kjonn)
                .setDodsdato(dodsdato)
                .setHarSammeBosted(harSammeBosted);
    }

}
