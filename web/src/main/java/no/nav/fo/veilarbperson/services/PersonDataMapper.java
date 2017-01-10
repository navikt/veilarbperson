package no.nav.fo.veilarbperson.services;

import no.nav.tjeneste.virksomhet.person.v2.informasjon.Person;

class PersonDataMapper{

    public static PersonData tilPersonData(Person person){
        return new PersonData()
                .medFornavn(person.getPersonnavn().getFornavn())
                .medMellomnavn(person.getPersonnavn().getMellomnavn())
                .medEtternavn(person.getPersonnavn().getEtternavn())
                .medSammensattNavn(person.getPersonnavn().getSammensattNavn())
                .medPersonnummer(person.getIdent().getIdent());
    }
}