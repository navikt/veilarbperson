package no.nav.veilarbperson.utils;

import no.nav.veilarbperson.domain.person.Enhet;

public class Mappers {

    public static Enhet fraNorg2Enhet(no.nav.common.client.norg2.Enhet enhet) {
        return new Enhet(enhet.getEnhetNr(), enhet.getNavn());
    }

}
