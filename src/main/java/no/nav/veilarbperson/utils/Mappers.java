package no.nav.veilarbperson.utils;

import no.nav.veilarbperson.client.person.domain.Diskresjonskoder;
import no.nav.veilarbperson.client.person.domain.Enhet;
import no.nav.veilarbperson.client.person.domain.Familiemedlem;

import java.util.List;
import java.util.stream.Collectors;

public class Mappers {

    private static final String BARN = "BARN";

    public static Enhet fraNorg2Enhet(no.nav.common.client.norg2.Enhet enhet) {
        return new Enhet(enhet.getEnhetNr(), enhet.getNavn());
    }

    public static List<Familiemedlem> familierelasjonerTilBarn(List<no.nav.tjeneste.virksomhet.person.v3.informasjon.Familierelasjon> familierelasjoner) {
        return familierelasjoner.stream()
                .filter(familierelasjon -> BARN.equals(familierelasjon.getTilRolle().getValue()))
                .map(FamiliemedlemMapper::familierelasjonTilFamiliemedlem)
                .collect(Collectors.toList());
    }

    public static String mapTilTallkode(String diskresjonskoder) {
        if (Diskresjonskoder.STRENGT_FORTROLIG_ADRESSE.kodeverkVerdi.equals(diskresjonskoder)){
            return Diskresjonskoder.STRENGT_FORTROLIG_ADRESSE.tallVerdi;
        } else if (Diskresjonskoder.FORTROLIG_ADRESSE.kodeverkVerdi.equals(diskresjonskoder)) {
            return Diskresjonskoder.FORTROLIG_ADRESSE.tallVerdi;
        } else {
            return null;
        }
    }
}
