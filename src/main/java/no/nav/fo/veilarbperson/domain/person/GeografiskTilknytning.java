package no.nav.fo.veilarbperson.domain.person;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Data
@Accessors(chain = true)
public class GeografiskTilknytning {
    private String geografiskTilknytning;
}
