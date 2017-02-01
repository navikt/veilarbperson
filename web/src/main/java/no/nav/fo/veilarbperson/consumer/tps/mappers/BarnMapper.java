package no.nav.fo.veilarbperson.consumer.tps.mappers;

import no.nav.fo.veilarbperson.domain.Familiemedlem;
import no.nav.tjeneste.virksomhet.person.v2.informasjon.WSFamilierelasjon;

import java.util.List;
import java.util.stream.Collectors;

public class BarnMapper {

    private static final String BARN = "BARN";

    private FamiliemedlemMapper familiemedlemMapper = new FamiliemedlemMapper();

    List<Familiemedlem> familierelasjonerTilBarn(List<WSFamilierelasjon> familierelasjoner) {
        return familierelasjoner.stream()
                .filter(familierelasjon -> BARN.equals(familierelasjon.getTilRolle().getValue()))
                .map(relasjon -> familiemedlemMapper.familierelasjonTilFamiliemedlem(relasjon))
                .collect(Collectors.toList());
    }
}