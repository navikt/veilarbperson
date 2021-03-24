package no.nav.veilarbperson.client.pdl;

import no.nav.common.health.HealthCheck;
import no.nav.common.types.identer.Fnr;

import java.util.List;

public interface PdlClient extends HealthCheck {

    HentPdlPerson.PdlPerson hentPerson(Fnr personIdent, String userToken);

    HentPdlPerson.Familiemedlem hentPartner(Fnr personIdent, String userToken);

    HentPdlPerson.VergeOgFullmakt hentVergeOgFullmakt(Fnr personIdent, String userToken);

    HentPdlPerson.PersonNavn hentPersonNavn(Fnr personIdent, String userToken);

    List<HentPdlPerson.Barn> hentPersonBolk(Fnr[] personIdenter);

    HentPdlPerson.GeografiskTilknytning hentGeografiskTilknytning(Fnr personIdent, String userToken);

    HentPdlPerson.HentSpraakTolk hentTilrettelagtKommunikasjon(Fnr personIdent, String userToken);
}
