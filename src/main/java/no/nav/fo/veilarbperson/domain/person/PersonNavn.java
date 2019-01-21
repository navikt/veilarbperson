package no.nav.fo.veilarbperson.domain.person;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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
