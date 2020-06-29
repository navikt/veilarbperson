package no.nav.veilarbperson.client.person.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class Sivilstand {
    String sivilstand;
    String fraDato;

    public Sivilstand copy() {
        return new Sivilstand(sivilstand, fraDato);
    }
}
