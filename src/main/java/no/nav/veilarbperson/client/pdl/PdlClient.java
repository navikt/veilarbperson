package no.nav.veilarbperson.client.pdl;

import no.nav.common.health.HealthCheck;
import no.nav.common.types.identer.Fnr;

import java.util.List;

public interface PdlClient extends HealthCheck {

    HentPerson.Person hentPerson(Fnr personIdent);
    HentPerson.Person hentPerson(Fnr personIdent, String behandlingsnummer);

    HentPerson.VergeOgFullmakt hentVergeOgFullmakt(Fnr personIdent);

    HentPerson.PersonNavn hentPersonNavn(Fnr personIdent);

    List<HentPerson.PersonFraBolk> hentPersonBolk(List<Fnr> personIdenter);

    HentPerson.GeografiskTilknytning hentGeografiskTilknytning(Fnr personIdent);

    HentPerson.HentSpraakTolk hentTilrettelagtKommunikasjon(Fnr personIdent);
}
