package no.nav.veilarbperson.client.representasjon;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.util.List;

public interface RepresentasjonClient {
    List<ReprFullmaktV2Data> hentFullmakt(String personIdent) throws IOException;
}

@Getter
@AllArgsConstructor
class PersonIdentDTO {
    String ident;
}
