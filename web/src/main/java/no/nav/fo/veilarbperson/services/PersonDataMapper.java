package no.nav.fo.veilarbperson.services;

import no.nav.tjeneste.virksomhet.person.v2.informasjon.*;

import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Optional.ofNullable;
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
                .medBarn(harFraRolleITilBarn(person.getHarFraRolleI()))
                .medDiskresjonskode(kanskjeDiskresjonskode(person))
                .medKontonummer(kanskjeKontonummer(person));
    }

    private static String kanskjeKontonummer(Person person) {
        Bankkonto bankkonto = person.getBankkonto();
        String kontonummer = null;

        if(bankkonto instanceof BankkontoNorge){
            BankkontoNorge bankkontoNorge = (BankkontoNorge) bankkonto;
            kontonummer = bankkontoNorge.getBankkonto().getBankkontonummer();
        }

        if(bankkonto instanceof BankkontoUtland){
            BankkontoUtland bankkontoUtland = (BankkontoUtland) bankkonto;
            kontonummer =  bankkontoUtland.getBankkontoUtland().getBankkontonummer();
        }

        return kontonummer;
    }

    private static String kanskjeDiskresjonskode(Person person) {
        return ofNullable(person.getDiskresjonskode())
        .map(Diskresjonskoder::getValue)
        .orElse("");
    }

    private static List<Barn> harFraRolleITilBarn(List<Familierelasjon> harFraRolleI) {
       return  harFraRolleI.stream()
                .filter(familierelasjon -> "BARN".equals(familierelasjon.getTilRolle().getValue()))
                .map(barnWS -> familierelasjonTilBarn(barnWS))
                .collect(toList());
    }

    private static Barn familierelasjonTilBarn(Familierelasjon familierelasjon) {

        Person person = familierelasjon.getTilPerson();

        return new Barn()
                .medFornavn(person.getPersonnavn().getFornavn())
                .medEtternavn(person.getPersonnavn().getEtternavn())
                .medSammensattnavn(person.getPersonnavn().getSammensattNavn())
                .medHarSammeBosted(familierelasjon.isHarSammeBosted())
                .medPersonnummer(person.getIdent().getIdent());

    }

    private static String fodseldatoTilString(GregorianCalendar foedselsdato) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setTimeZone(foedselsdato.getTimeZone());
        return formatter.format(foedselsdato.getTime());
    }
}