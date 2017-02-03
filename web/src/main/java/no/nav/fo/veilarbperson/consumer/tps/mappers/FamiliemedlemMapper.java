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

        Familiemedlem familiemedlem = new Familiemedlem();
        familiemedlem.setFornavn(person.getPersonnavn().getFornavn());
        familiemedlem.setEtternavn(person.getPersonnavn().getEtternavn());
        familiemedlem.setSammensattNavn(person.getPersonnavn().getSammensattNavn());
        familiemedlem.setHarSammeBosted(familierelasjon.isHarSammeBosted());
        familiemedlem.setPersonnummer(personnummer);
        familiemedlem.setFodselsdato(Personnummer.personnummerTilFodselsdato(personnummer));
        familiemedlem.setKjoenn(Personnummer.personnummerTilKjoenn(personnummer));

        return familiemedlem;
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