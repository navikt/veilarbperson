package no.nav.fo.veilarbperson.consumer.tps.mappers;

import no.nav.fo.veilarbperson.domain.person.Familiemedlem;

import java.util.List;
import java.util.stream.Collectors;

public class BarnMapper {

    private static final String BARN = "BARN";

    private FamiliemedlemMapper familiemedlemMapper = new FamiliemedlemMapper();

    List<Familiemedlem> familierelasjonerTilBarn(List<no.nav.tjeneste.virksomhet.person.v3.informasjon.Familierelasjon> familierelasjoner) {
        return familierelasjoner.stream()
                .filter(familierelasjon -> BARN.equals(familierelasjon.getTilRolle().getValue()))
                .map(relasjon -> FamiliemedlemMapper.familierelasjonTilFamiliemedlem(relasjon))
                .collect(Collectors.toList());
    }
}