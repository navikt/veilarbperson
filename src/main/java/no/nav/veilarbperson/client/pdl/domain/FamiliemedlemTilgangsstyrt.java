package no.nav.veilarbperson.client.pdl.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@Accessors(chain = true)
public class FamiliemedlemTilgangsstyrt {
    private String fornavn;
    private LocalDate fodselsdato;
    private Boolean erDod;
    private Integer alder;
    private Boolean erEgenAnsatt;
    private boolean harVeilederTilgang = false;
    private String gradering; // diskresjonskode
    private RelasjonsBosted relasjonsBosted;
}
