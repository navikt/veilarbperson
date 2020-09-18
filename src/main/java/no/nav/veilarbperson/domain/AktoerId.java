package no.nav.veilarbperson.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import no.nav.common.types.identer.AktorId;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class AktoerId {
    private AktorId aktorId;
}
