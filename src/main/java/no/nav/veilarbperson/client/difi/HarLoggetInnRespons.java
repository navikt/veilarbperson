package no.nav.veilarbperson.client.difi;

import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.common.types.identer.Fnr;


// TODO: 21/08/2023 denne skal slettes etter vi har ryddet opp i kode i de andre appene da dkif slutter å tilby tjenesten
@Data
@Accessors(chain = true)
public class HarLoggetInnRespons {
    boolean harbruktnivaa4;
    boolean erRegistrertIdPorten;
    Fnr personidentifikator;
}
