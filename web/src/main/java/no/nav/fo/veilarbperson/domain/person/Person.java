package no.nav.fo.veilarbperson.domain.person;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

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
