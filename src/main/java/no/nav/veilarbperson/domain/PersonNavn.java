package no.nav.veilarbperson.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import no.nav.veilarbperson.client.person.domain.Person;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class PersonNavn {

    private String fornavn;
    private String mellomnavn;
    private String etternavn;
    private String sammensattNavn;

    public static PersonNavn fraPerson(Person person){
        return new PersonNavn()
                .setFornavn(person.getFornavn())
                .setMellomnavn(person.getMellomnavn())
                .setEtternavn(person.getEtternavn())
                .setSammensattNavn(person.getSammensattNavn());
    }

}
