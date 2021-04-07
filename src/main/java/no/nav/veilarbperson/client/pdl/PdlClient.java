package no.nav.veilarbperson.client.pdl;

import no.nav.common.health.HealthCheck;
import no.nav.common.types.identer.Fnr;

import java.util.List;

public interface PdlClient extends HealthCheck {

    HentPerson.Person hentPerson(Fnr personIdent, String userToken);

    HentPerson.Familiemedlem hentPartner(Fnr personIdent, String userToken);

    HentPerson.VergeOgFullmakt hentVergeOgFullmakt(Fnr personIdent, String userToken);

    HentPerson.PersonNavn hentPersonNavn(Fnr personIdent, String userToken);

    List<HentPerson.Barn> hentPersonBolk(Fnr[] personIdenter);

    HentPerson.GeografiskTilknytning hentGeografiskTilknytning(Fnr personIdent, String userToken);

    HentPerson.HentSpraakTolk hentTilrettelagtKommunikasjon(Fnr personIdent, String userToken);
}
