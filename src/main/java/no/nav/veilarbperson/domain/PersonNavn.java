package no.nav.veilarbperson.domain;

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

}
