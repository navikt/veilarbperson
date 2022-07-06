package no.nav.veilarbperson.client.pdl.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class Sivilstand {
    String sivilstand;
    LocalDate fraDato;
    Boolean skjermet;
    String gradering;       //diskresjonskode
    RelasjonsBosted relasjonsBosted;
    String master;
    LocalDateTime registrertDato;
}
