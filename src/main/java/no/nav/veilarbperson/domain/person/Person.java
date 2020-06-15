package no.nav.veilarbperson.domain.person;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class Person {

    private String fornavn;
    private String mellomnavn;
    private String etternavn;
    private String sammensattNavn;
    private String fodselsnummer;
    private String fodselsdato;

    private String kjonn;
    private String dodsdato;

}
