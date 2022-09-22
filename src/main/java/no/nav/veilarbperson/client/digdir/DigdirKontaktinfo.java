package no.nav.veilarbperson.client.digdir;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DigdirKontaktinfo {
    String personident;
    Boolean kanVarsles;
    Boolean reservert;
    String epostadresse;
    ZonedDateTime epostadresseOppdatert;
    String mobiltelefonnummer;
    ZonedDateTime mobiltelefonnummerOppdatert;
    String spraak;
}
