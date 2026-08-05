package no.nav.veilarbperson.utils;

import no.nav.veilarbperson.client.pdl.HentPerson;
import no.nav.veilarbperson.client.pdl.domain.Diskresjonskode;
import no.nav.veilarbperson.domain.PersonVisittkortData;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static no.nav.veilarbperson.utils.PersonV2DataMapper.*;

public class PersonVisittkortDataMapper {

    public static PersonVisittkortData tilPersonVisittkortData(HentPerson.Person person, boolean erSkjermet) {
        Optional<HentPerson.Navn> navn = hentGjeldeneNavn(person.getNavn());

        return new PersonVisittkortData()
                .setFornavn(navn.map(HentPerson.Navn::getFornavn).orElse(null))
                .setMellomnavn(navn.map(HentPerson.Navn::getMellomnavn).orElse(null))
                .setEtternavn(navn.map(HentPerson.Navn::getEtternavn).orElse(null))
                .setFodselsdato(ofNullable(getFirstElement(person.getFoedselsdato()))
                        .map(HentPerson.Foedselsdato::getFoedselsdato)
                        .map(Object::toString)
                        .orElse(null))
                .setDodsdato(ofNullable(getFirstElement(person.getDoedsfall()))
                        .map(HentPerson.Doedsfall::getDoedsdato)
                        .map(Object::toString)
                        .orElse(null))
                .setKjonn(ofNullable(getFirstElement(person.getKjoenn()))
                        .map(HentPerson.Kjoenn::getKjoenn)
                        .orElse(null))
                .setDiskresjonskode(ofNullable(getFirstElement(person.getAdressebeskyttelse()))
                        .map(HentPerson.Adressebeskyttelse::getGradering)
                        .map(Diskresjonskode::mapKodeTilTall)
                        .orElse(null))
                .setEgenAnsatt(erSkjermet)
                .setSikkerhetstiltak(ofNullable(getFirstElement(person.getSikkerhetstiltak()))
                        .map(HentPerson.Sikkerhetstiltak::getBeskrivelse)
                        .orElse(null))
                .setTelefon(mapTelefonNrFraPdl(person.getTelefonnummer()));
    }
}
