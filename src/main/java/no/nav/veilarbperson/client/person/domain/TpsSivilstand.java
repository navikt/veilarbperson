package no.nav.veilarbperson.client.person.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class TpsSivilstand {
    String sivilstand;
    String fraDato;

    public TpsSivilstand copy() {
        return new TpsSivilstand(sivilstand, fraDato);
    }
}
