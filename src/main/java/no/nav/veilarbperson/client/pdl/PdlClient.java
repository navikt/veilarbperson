package no.nav.veilarbperson.client.pdl;

import no.nav.common.health.HealthCheck;

import java.util.List;

public interface PdlClient extends HealthCheck {

    HentPdlPerson.PdlPerson hentPerson(String personIdent, String userToken);

    HentPdlPerson.PersonsFamiliemedlem hentPartnerOpplysninger(String personIdent, String userToken);

    List<HentPdlPerson.PdlPersonBolk> hentPersonBolk(String[] personIdent);

}
