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

    Boolean harSammeBosted;

    public no.nav.veilarbperson.client.person.domain.Familiemedlem copy() {
        return new no.nav.veilarbperson.client.person.domain.Familiemedlem()
                .setFornavn(fornavn)
                .setMellomnavn(mellomnavn)
                .setEtternavn(etternavn)
                .setSammensattNavn(forkortetnavn)
                .setFodselsnummer(fodselsnummer)
                .setFodselsdato(fodselsdato)
                .setKjonn(kjonn)
                .setDodsdato(dodsdato)
                .setHarSammeBosted(harSammeBosted);
    }
}
