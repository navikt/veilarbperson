package no.nav.veilarbperson.domain.person;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Person {

    private String fornavn;
    private String mellomnavn;
    private String etternavn;
    private String sammensattNavn;
    private String fodselsnummer;
    private String fodselsdato;

    @JsonProperty("kjonn")
    private String kjoenn;
    private String dodsdato;


}
