package no.nav.veilarbperson.client.person;

import no.nav.veilarbperson.domain.Enhet;

public class Mappers {
    public static Enhet fraNorg2Enhet(no.nav.common.client.norg2.Enhet enhet) {
        return new Enhet(enhet.getEnhetNr(), enhet.getNavn());
    }
}
