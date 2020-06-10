package no.nav.veilarbperson.consumer.tps.mappers;

import no.nav.veilarbperson.domain.person.Diskresjonskoder;

class DiskresjonskodeMapper {
    static String mapTilTallkode(String diskresjonskoder) {
        if (Diskresjonskoder.STRENGT_FORTROLIG_ADRESSE.kodeverkVerdi.equals(diskresjonskoder)){
            return Diskresjonskoder.STRENGT_FORTROLIG_ADRESSE.tallVerdi;
        } else if (Diskresjonskoder.FORTROLIG_ADRESSE.kodeverkVerdi.equals(diskresjonskoder)) {
            return Diskresjonskoder.FORTROLIG_ADRESSE.tallVerdi;
        } else {
            return null;
        }
    }
}
