package no.nav.fo.veilarbperson.services;

import no.nav.tjeneste.virksomhet.person.v2.informasjon.Person;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

class PersonDataMapper{

    public static PersonData tilPersonData(Person person){
        return new PersonData()
                .medFornavn(person.getPersonnavn().getFornavn())
                .medMellomnavn(person.getPersonnavn().getMellomnavn())
                .medEtternavn(person.getPersonnavn().getEtternavn())
                .medSammensattNavn(person.getPersonnavn().getSammensattNavn())
                .medPersonnummer(person.getIdent().getIdent())
                .medFodselsdato(fodseldatoTilString(person.getFoedselsdato().getFoedselsdato().toGregorianCalendar()))
                .medKjoenn(person.getKjoenn().getKjoenn().getValue());
    }

    private static String fodseldatoTilString(GregorianCalendar foedselsdato) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setTimeZone(foedselsdato.getTimeZone());
        return formatter.format(foedselsdato.getTime());
    }
}