package no.nav.veilarbperson.client.person.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.common.types.identer.Fnr;

import java.util.List;

@Data
@Accessors(chain = true)
public class TpsPerson  {
    String fornavn;
    String mellomnavn;
    String etternavn;
    String sammensattNavn;
    Fnr fodselsnummer;
    String fodselsdato;
    String kjonn;
    String dodsdato;

    List<Familiemedlem> barn;
    String diskresjonskode;
    String kontonummer;
    String geografiskTilknytning;
    String statsborgerskap;
    TpsSivilstand sivilstand;
    Familiemedlem partner;
    Bostedsadresse bostedsadresse;
    MidlertidigAdresseNorge midlertidigAdresseNorge;
    MidlertidigAdresseUtland midlertidigAdresseUtland;
    PostAdresse postAdresse;
    String malform;
}
