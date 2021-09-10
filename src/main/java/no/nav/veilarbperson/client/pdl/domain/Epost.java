package no.nav.veilarbperson.client.pdl.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Epost {
    private String epostAdresse;
    private String epostSistOppdatert;
    private String master;
}
