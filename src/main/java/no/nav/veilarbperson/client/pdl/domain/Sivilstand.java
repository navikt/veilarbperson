package no.nav.veilarbperson.client.pdl.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@Accessors(chain = true)
public class Sivilstand {
    String sivilstand;
    LocalDate fraDato;
}
