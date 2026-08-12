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

    private PersonVisittkortDataMapper() {}

    public static PersonVisittkortData tilPersonVisittkortData(
            HentPerson.Person person,
            boolean erSkjermet,
            String krrTelefon,
            String krrTelefonOppdatert) {

        Optional<HentPerson.Navn> navn = hentGjeldeneNavn(person.getNavn());

        List<Telefon> telefoner = new ArrayList<>(mapTelefonNrFraPdl(person.getTelefonnummer()));
        leggKrrTelefonIListe(krrTelefon, krrTelefonOppdatert, telefoner);

        return new PersonVisittkortData(
                navn.map(HentPerson.Navn::getFornavn).orElse(null),
                navn.map(HentPerson.Navn::getMellomnavn).orElse(null),
                navn.map(HentPerson.Navn::getEtternavn).orElse(null),
                Optional.ofNullable(getFirstElement(person.getFoedselsdato()))
                        .map(HentPerson.Foedselsdato::getFoedselsdato)
                        .map(Object::toString)
                        .orElse(null),
                Optional.ofNullable(getFirstElement(person.getDoedsfall()))
                        .map(HentPerson.Doedsfall::getDoedsdato)
                        .map(Object::toString)
                        .orElse(null),
                Optional.ofNullable(getFirstElement(person.getKjoenn()))
                        .map(HentPerson.Kjoenn::getKjoenn)
                        .orElse(null),
                Optional.ofNullable(getFirstElement(person.getAdressebeskyttelse()))
                        .map(HentPerson.Adressebeskyttelse::getGradering)
                        .map(Diskresjonskode::mapKodeTilTall)
                        .orElse(null),
                erSkjermet,
                Optional.ofNullable(getFirstElement(person.getSikkerhetstiltak()))
                        .map(HentPerson.Sikkerhetstiltak::getBeskrivelse)
                        .orElse(null),
                telefoner
        );
    }

    /** KRR-telefon får alltid prioritet 1. PDL-nummer med samme nummer fjernes. Øvrige PDL-prioriteter bumpes. */
    static void leggKrrTelefonIListe(String krrTelefon, String oppdatert, List<Telefon> liste) {
        if (krrTelefon == null) return;
        liste.removeIf(t -> krrTelefon.equals(t.getTelefonNr()));
        liste.stream()
                .filter(t -> !"KRR".equals(t.getMaster()))
                .forEach(t -> t.setPrioritet(String.valueOf(Integer.parseInt(t.getPrioritet()) + 1)));
        liste.add(0, new Telefon().setPrioritet("1").setTelefonNr(krrTelefon).setRegistrertDato(oppdatert).setMaster("KRR"));
    }
}
