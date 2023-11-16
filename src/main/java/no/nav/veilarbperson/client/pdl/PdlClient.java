package no.nav.veilarbperson.client.pdl;

import no.nav.common.health.HealthCheck;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.domain.PdlRequest;

import java.util.List;

public interface PdlClient extends HealthCheck {

    @Deprecated
    HentPerson.Person hentPerson(Fnr personIdent);
    HentPerson.Person hentPerson(PdlRequest pdlRequest);

    @Deprecated
    HentPerson.VergeOgFullmakt hentVergeOgFullmakt(Fnr personIdent);

    HentPerson.VergeOgFullmakt hentVergeOgFullmakt(PdlRequest pdlRequest);

    @Deprecated
    HentPerson.PersonNavn hentPersonNavn(Fnr personIdent);

    HentPerson.PersonNavn hentPersonNavn(Fnr personIdent, String behandlingsnummer);

    @Deprecated
    List<HentPerson.PersonFraBolk> hentPersonBolk(List<Fnr> personIdenter);

    List<HentPerson.PersonFraBolk> hentPersonBolk(List<Fnr> personIdenter, String behandlingsnummer);

    @Deprecated
    HentPerson.GeografiskTilknytning hentGeografiskTilknytning(Fnr personIdent);

    HentPerson.GeografiskTilknytning hentGeografiskTilknytning(PdlRequest pdlRequest);

    @Deprecated
    HentPerson.HentSpraakTolk hentTilrettelagtKommunikasjon(Fnr personIdent);

    HentPerson.HentSpraakTolk hentTilrettelagtKommunikasjon(PdlRequest pdlRequest);
}
