package no.nav.veilarbperson.client.pdl;

import no.nav.common.health.HealthCheck;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.pdl.domain.PdlRequest;
import no.nav.veilarbperson.domain.PersonFraPdlRequest;

import java.util.List;

public interface PdlClient extends HealthCheck {
    HentPerson.Person hentPerson(PdlRequest pdlRequest);

    HentPerson.VergeOgFullmakt hentVergeOgFullmakt(PdlRequest pdlRequest);

    HentPerson.PersonNavn hentPersonNavn(PdlRequest pdlRequest);

    List<HentPerson.PersonFraBolk> hentPersonBolk(List<Fnr> personIdenter, String behandlingsnummer);

    HentPerson.GeografiskTilknytning hentGeografiskTilknytning(PdlRequest pdlRequest);

    HentPerson.HentSpraakTolk hentTilrettelagtKommunikasjon(PdlRequest pdlRequest);
}
