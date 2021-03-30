package no.nav.veilarbperson.client.pdl.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PersonNavn {
    private String fornavn;
    private String mellomnavn;
    private String etternavn;
    private String forkortetNavn;
}
