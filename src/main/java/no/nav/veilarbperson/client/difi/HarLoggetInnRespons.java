package no.nav.veilarbperson.client.difi;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import no.nav.common.types.identer.Fnr;

@Data
@Accessors(chain = true)
public class HarLoggetInnRespons {
    boolean harbruktnivaa4;
    boolean erRegistrertIdPorten;
    Fnr personidentifikator;
}
