package no.nav.fo.veilarbperson.consumer.tps.mappers;

import no.nav.fo.veilarbperson.domain.person.Familiemedlem;
import no.nav.fo.veilarbperson.utils.FodselsnummerHjelper;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;

import static java.util.Optional.ofNullable;

public class FamiliemedlemMapper {

    private static final String EKTEFELLE = "EKTE";

    static Familiemedlem familierelasjonTilFamiliemedlem(WSFamilierelasjon familierelasjon) {

        WSPerson person = familierelasjon.getTilPerson();
        final String fodselsnummer = kanskjeFodselsnummer(person);

        return Familiemedlem.builder()
                .fornavn(kanskjeFornavn(person))
                .etternavn(kanskjeEtternavn(person))
                .sammensattnavn(kanskjeSammensattNavn(person))
                .harSammeBosted(familierelasjon.isHarSammeBosted())
                .fodselsnummer(fodselsnummer)
                .fodselsdato(kanskjeFodselsdato(fodselsnummer))
                .kjonn(kanskjekjonn(fodselsnummer))
                .dodsdato(kanskjeDoedsdato(person))
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

    private static String kanskjeFornavn(WSPerson person) {
        return ofNullable(person.getPersonnavn())
                .map(WSPersonnavn::getFornavn)
                .orElse(null);
    }

    private static String kanskjeEtternavn(WSPerson person) {
        return ofNullable(person.getPersonnavn())
                .map(WSPersonnavn::getEtternavn)
                .orElse(null);
    }

    private static String kanskjeSammensattNavn(WSPerson person) {
        return ofNullable(person.getPersonnavn())
                .map(WSPersonnavn::getSammensattNavn)
                .orElse(null);
    }

    private static String kanskjeFodselsnummer(WSPerson person) {
        WSAktoer aktoer = person.getAktoer();
        if (aktoer instanceof WSPersonIdent) {
            return kanskjeNorskIdent((WSPersonIdent) aktoer);
        }
        return null;
    }

    private static String kanskjeNorskIdent(WSPersonIdent aktoer) {
        return ofNullable(aktoer.getIdent())
                .map(WSNorskIdent::getIdent)
                .orElse(null);
    }

    private static String kanskjeFodselsdato(String fodselsnummer) {
        return ofNullable(fodselsnummer)
                .map(FodselsnummerHjelper::fodselsnummerTilFodselsdato)
                .orElse(null);
    }

    private static String kanskjekjonn(String fodselsnummer) {
        return ofNullable(fodselsnummer)
                .map(FodselsnummerHjelper::fodselsnummerTilKjoenn)
                .orElse(null);
    }

    private static String kanskjeDoedsdato(WSPerson person) {
        return ofNullable(person.getDoedsdato())
                .map(WSDoedsdato::getDoedsdato)
                .map(XMLGregorianCalendar::toString)
                .orElse(null);
    }

}