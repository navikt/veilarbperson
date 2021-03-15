package no.nav.veilarbperson.client.pdl;

import no.nav.common.health.HealthCheck;

import java.util.List;

public interface PdlClient extends HealthCheck {

    HentPdlPerson.PdlPerson hentPerson(String personIdent, String userToken);

    HentPdlPerson.Familiemedlem hentPartner(String personIdent, String userToken);

    HentPdlPerson.VergeOgFullmakt hentVergeOgFullmakt(String personIdent, String userToken);

    List<HentPdlPerson.Navn> hentPersonNavn(String personIdent, String userToken);

    List<HentPdlPerson.Barn> hentPersonBolk(String[] personIdenter);

    HentPdlPerson.GeografiskTilknytning hentGeografiskTilknytning(String personIdent, String userToken);
}
