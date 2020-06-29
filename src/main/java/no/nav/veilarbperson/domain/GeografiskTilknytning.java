package no.nav.veilarbperson.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Data
@Accessors(chain = true)
public class GeografiskTilknytning {
    private String geografiskTilknytning;
}
