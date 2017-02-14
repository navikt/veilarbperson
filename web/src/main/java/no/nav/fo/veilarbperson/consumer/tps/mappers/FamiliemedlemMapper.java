package no.nav.fo.veilarbperson.consumer.tps.mappers;

import no.nav.fo.veilarbperson.domain.Familiemedlem;
import no.nav.fo.veilarbperson.utils.Personnummer;
import no.nav.tjeneste.virksomhet.person.v2.informasjon.WSFamilierelasjon;
import no.nav.tjeneste.virksomhet.person.v2.informasjon.WSPerson;

import java.util.List;

public class FamiliemedlemMapper {

    private static final String EKTEFELLE = "EKTE";

    static Familiemedlem familierelasjonTilFamiliemedlem(WSFamilierelasjon familierelasjon) {

        WSPerson person = familierelasjon.getTilPerson();
        final String personnummer = person.getIdent().getIdent();

        return Familiemedlem.builder()
                .fornavn(person.getPersonnavn().getFornavn())
                .etternavn(person.getPersonnavn().getEtternavn())
                .sammensattnavn(person.getPersonnavn().getSammensattNavn())
                .harSammeBosted(familierelasjon.isHarSammeBosted())
                .personnummer(personnummer)
                .fodselsdato(Personnummer.personnummerTilFodselsdato(personnummer))
                .kjonn(Personnummer.personnummerTilKjoenn(personnummer))
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