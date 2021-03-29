package no.nav.veilarbperson.client.pdl.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TilrettelagtKommunikasjonData {

    private String talespraak;
    private String tegnspraak;
}
