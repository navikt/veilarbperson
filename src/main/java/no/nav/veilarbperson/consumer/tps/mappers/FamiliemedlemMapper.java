package no.nav.veilarbperson.consumer.tps.mappers;

import no.nav.veilarbperson.domain.person.Familiemedlem;
import no.nav.veilarbperson.utils.FodselsnummerHjelper;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;

import static java.util.Optional.ofNullable;

public class FamiliemedlemMapper {

    private static final String EKTEFELLE = "EKTE";

    static Familiemedlem familierelasjonTilFamiliemedlem(no.nav.tjeneste.virksomhet.person.v3.informasjon.Familierelasjon familierelasjon) {

        no.nav.tjeneste.virksomhet.person.v3.informasjon.Person person = familierelasjon.getTilPerson();
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

    Familiemedlem partner(List<no.nav.tjeneste.virksomhet.person.v3.informasjon.Familierelasjon> familierelasjoner) {
        for (no.nav.tjeneste.virksomhet.person.v3.informasjon.Familierelasjon relasjon : familierelasjoner) {
            if (EKTEFELLE.equals(relasjon.getTilRolle().getValue())) {
                return familierelasjonTilFamiliemedlem(relasjon);
            }
        }
        return null;
    }

    private static String kanskjeFornavn(no.nav.tjeneste.virksomhet.person.v3.informasjon.Person person) {
        return ofNullable(person.getPersonnavn())
                .map(no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn::getFornavn)
                .orElse(null);
    }

    private static String kanskjeEtternavn(no.nav.tjeneste.virksomhet.person.v3.informasjon.Person person) {
        return ofNullable(person.getPersonnavn())
                .map(no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn::getEtternavn)
                .orElse(null);
    }

    private static String kanskjeSammensattNavn(no.nav.tjeneste.virksomhet.person.v3.informasjon.Person person) {
        return ofNullable(person.getPersonnavn())
                .map(no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn::getSammensattNavn)
                .orElse(null);
    }

    private static String kanskjeFodselsnummer(no.nav.tjeneste.virksomhet.person.v3.informasjon.Person person) {
        no.nav.tjeneste.virksomhet.person.v3.informasjon.Aktoer aktoer = person.getAktoer();
        if (aktoer instanceof no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent) {
            return kanskjeNorskIdent((no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent) aktoer);
        }
        return null;
    }

    private static String kanskjeNorskIdent(no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent aktoer) {
        return ofNullable(aktoer.getIdent())
                .map(no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent::getIdent)
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

    private static String kanskjeDoedsdato(no.nav.tjeneste.virksomhet.person.v3.informasjon.Person person) {
        return ofNullable(person.getDoedsdato())
                .map(no.nav.tjeneste.virksomhet.person.v3.informasjon.Doedsdato::getDoedsdato)
                .map(XMLGregorianCalendar::toString)
                .orElse(null);
    }

}