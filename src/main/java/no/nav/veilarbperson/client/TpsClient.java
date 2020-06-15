package no.nav.veilarbperson.client;

import no.nav.veilarbperson.domain.person.GeografiskTilknytning;
import no.nav.veilarbperson.domain.person.PersonData;
import no.nav.veilarbperson.domain.person.Sikkerhetstiltak;

public interface TpsClient {

    PersonData hentPerson(String ident);

    GeografiskTilknytning hentGeografiskTilknytning(String ident);

    Sikkerhetstiltak hentSikkerhetstiltak(String ident);

}
