package no.nav.veilarbperson.client.tps.mappers;

import no.nav.veilarbperson.domain.person.Familiemedlem;

import java.util.List;
import java.util.stream.Collectors;

public class BarnMapper {

    private static final String BARN = "BARN";

    public static List<Familiemedlem> familierelasjonerTilBarn(List<no.nav.tjeneste.virksomhet.person.v3.informasjon.Familierelasjon> familierelasjoner) {
        return familierelasjoner.stream()
                .filter(familierelasjon -> BARN.equals(familierelasjon.getTilRolle().getValue()))
                .map(FamiliemedlemMapper::familierelasjonTilFamiliemedlem)
                .collect(Collectors.toList());
    }
}