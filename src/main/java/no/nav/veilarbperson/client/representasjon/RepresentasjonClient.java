package no.nav.veilarbperson.client.representasjon;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.IOException;
import java.util.List;

public interface RepresentasjonClient {
    List<ReprFullmaktData.Fullmakt> hentFullmakt(String personIdent) throws IOException;
}

@Data
@AllArgsConstructor
class PersonIdentDTO {
    String ident;
}
