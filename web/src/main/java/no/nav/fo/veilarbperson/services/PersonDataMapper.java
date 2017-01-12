package no.nav.fo.veilarbperson.services;

import no.nav.tjeneste.virksomhet.person.v2.informasjon.Familierelasjon;
import no.nav.tjeneste.virksomhet.person.v2.informasjon.Person;

import java.text.SimpleDateFormat;
import java.util.*;
import static java.util.stream.Collectors.toList;


class PersonDataMapper{

    public static PersonData tilPersonData(Person person){
        return new PersonData()
                .medFornavn(person.getPersonnavn().getFornavn())
                .medMellomnavn(person.getPersonnavn().getMellomnavn())
                .medEtternavn(person.getPersonnavn().getEtternavn())
                .medSammensattNavn(person.getPersonnavn().getSammensattNavn())
                .medPersonnummer(person.getIdent().getIdent())
                .medFodselsdato(fodseldatoTilString(person.getFoedselsdato().getFoedselsdato().toGregorianCalendar()))
                .medKjoenn(person.getKjoenn().getKjoenn().getValue())
                .medBarn(harFraRolleITilBarn(person.getHarFraRolleI()));
    }

    private static List<Barn> harFraRolleITilBarn(List<Familierelasjon> harFraRolleI) {
       return  harFraRolleI.stream()
                .filter(familierelasjon -> "BARN".equals(familierelasjon.getTilRolle().getValue()))
                .map(barnWS -> familierelasjonTilBarn(barnWS))
                .collect(toList());
    }

    private static Barn familierelasjonTilBarn(Familierelasjon familierelasjon) {
        return new Barn()
                .medFornavn(familierelasjon.getTilPerson().getPersonnavn().getFornavn())
                .medEtternavn(familierelasjon.getTilPerson().getPersonnavn().getEtternavn())
                .medSammensattnavn(familierelasjon.getTilPerson().getPersonnavn().getSammensattNavn());
    }

    private static String fodseldatoTilString(GregorianCalendar foedselsdato) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setTimeZone(foedselsdato.getTimeZone());
        return formatter.format(foedselsdato.getTime());
    }
}