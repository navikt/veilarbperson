package no.nav.fo.veilarbperson.domain.person;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@NoArgsConstructor
@Data
@Accessors(chain = true)

public class BoligInformasjon {
    private MidlertidigAdresseNorge midlertidigAdresseNorge;
    private Bostedsadresse bostedsadresse;
}
