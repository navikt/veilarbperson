package no.nav.fo.veilarbperson.consumer.tps.mappers;

import no.nav.fo.veilarbperson.domain.person.Familiemedlem;
import no.nav.fo.veilarbperson.utils.FodselsnummerHjelper;
import no.nav.tjeneste.virksomhet.person.v2.informasjon.WSFamilierelasjon;
import no.nav.tjeneste.virksomhet.person.v2.informasjon.WSPerson;

import java.util.List;

public class FamiliemedlemMapper {

    private static final String EKTEFELLE = "EKTE";

    static Familiemedlem familierelasjonTilFamiliemedlem(WSFamilierelasjon familierelasjon) {

        WSPerson person = familierelasjon.getTilPerson();
        final String fodselsnummer = person.getIdent().getIdent();

        return Familiemedlem.builder()
                .fornavn(person.getPersonnavn().getFornavn())
                .etternavn(person.getPersonnavn().getEtternavn())
                .sammensattnavn(person.getPersonnavn().getSammensattNavn())
                .harSammeBosted(familierelasjon.isHarSammeBosted())
                .fodselsnummer(fodselsnummer)
                .fodselsdato(FodselsnummerHjelper.fodselsnummerTilFodselsdato(fodselsnummer))
                .kjonn(FodselsnummerHjelper.fodselsnummerTilKjoenn(fodselsnummer))
                .build();
    }

    Familiemedlem partner(List<WSFamilierelasjon> familierelasjoner) {
        for (WSFamilierelasjon relasjon : familierelasjoner) {
            if (EKTEFELLE.equals(relasjon.getTilRolle().getValue())) {
                return familierelasjonTilFamiliemedlem(relasjon);
            }
        }
        return null;
    }
}