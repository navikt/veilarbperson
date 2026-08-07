package no.nav.veilarbperson.utils;

import no.nav.veilarbperson.client.pdl.HentPerson;
import no.nav.veilarbperson.client.pdl.domain.Diskresjonskode;
import no.nav.veilarbperson.client.pdl.domain.Telefon;
import no.nav.veilarbperson.domain.PersonVisittkortData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static no.nav.veilarbperson.utils.PersonV2DataMapper.*;

public class PersonVisittkortDataMapper {

    public static PersonVisittkortData tilPersonVisittkortData(
            HentPerson.Person person,
            boolean erSkjermet,
            String krrTelefon,
            String krrTelefonOppdatert) {

        Optional<HentPerson.Navn> navn = hentGjeldeneNavn(person.getNavn());

        List<Telefon> telefoner = new ArrayList<>(mapTelefonNrFraPdl(person.getTelefonnummer()));
        leggKrrTelefonIListe(krrTelefon, krrTelefonOppdatert, telefoner);

        return new PersonVisittkortData()
                .setFornavn(navn.map(HentPerson.Navn::getFornavn).orElse(null))
                .setMellomnavn(navn.map(HentPerson.Navn::getMellomnavn).orElse(null))
                .setEtternavn(navn.map(HentPerson.Navn::getEtternavn).orElse(null))
                .setFodselsdato(Optional.of(getFirstElement(person.getFoedselsdato()))
                        .map(HentPerson.Foedselsdato::getFoedselsdato)
                        .map(Object::toString)
                        .orElse(null))
                .setDodsdato(Optional.of(getFirstElement(person.getDoedsfall()))
                        .map(HentPerson.Doedsfall::getDoedsdato)
                        .map(Object::toString)
                        .orElse(null))
                .setKjonn(Optional.of(getFirstElement(person.getKjoenn()))
                        .map(HentPerson.Kjoenn::getKjoenn)
                        .orElse(null))
                .setDiskresjonskode(Optional.of(getFirstElement(person.getAdressebeskyttelse()))
                        .map(HentPerson.Adressebeskyttelse::getGradering)
                        .map(Diskresjonskode::mapKodeTilTall)
                        .orElse(null))
                .setEgenAnsatt(erSkjermet)
                .setSikkerhetstiltak(Optional.of(getFirstElement(person.getSikkerhetstiltak()))
                        .map(HentPerson.Sikkerhetstiltak::getBeskrivelse)
                        .orElse(null))
                .setTelefon(telefoner);
    }

    /** KRR-telefon får alltid prioritet 1. PDL-nummer med samme nummer fjernes. Øvrige PDL-prioriteter bumpes. */
    static void leggKrrTelefonIListe(String krrTelefon, String oppdatert, List<Telefon> liste) {
        if (krrTelefon == null) return;
        liste.removeIf(t -> krrTelefon.equals(t.getTelefonNr()));
        liste.add(new Telefon().setPrioritet("1").setTelefonNr(krrTelefon).setRegistrertDato(oppdatert).setMaster("KRR"));
        liste.stream()
                .filter(t -> !"KRR".equals(t.getMaster()))
                .forEach(t -> t.setPrioritet(String.valueOf(Integer.parseInt(t.getPrioritet()) + 1)));
    }
}
