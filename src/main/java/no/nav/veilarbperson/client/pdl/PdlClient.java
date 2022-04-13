package no.nav.veilarbperson.client.pdl;

import no.nav.common.health.HealthCheck;
import no.nav.common.types.identer.Fnr;

import java.util.List;

public interface PdlClient extends HealthCheck {

    HentPerson.Person hentPerson(Fnr personIdent, PdlAuth auth);

    HentPerson.VergeOgFullmakt hentVergeOgFullmakt(Fnr personIdent, PdlAuth auth);

    HentPerson.PersonNavn hentPersonNavn(Fnr personIdent, PdlAuth auth);

    List<HentPerson.PersonFraBolk> hentPersonBolk(List<Fnr> personIdenter, PdlAuth auth);

    HentPerson.GeografiskTilknytning hentGeografiskTilknytning(Fnr personIdent, PdlAuth auth);

    HentPerson.HentSpraakTolk hentTilrettelagtKommunikasjon(Fnr personIdent, PdlAuth auth);
}
