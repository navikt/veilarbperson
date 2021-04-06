package no.nav.veilarbperson.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PersonNavnV2 {
    private String fornavn;
    private String mellomnavn;
    private String etternavn;
    private String forkortetNavn;
}
