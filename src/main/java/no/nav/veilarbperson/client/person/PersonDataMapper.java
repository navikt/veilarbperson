package no.nav.veilarbperson.client.person;

import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import no.nav.veilarbperson.client.person.TpsPerson;
import no.nav.veilarbperson.domain.PersonDataTPS;

import static java.util.Optional.ofNullable;

public class PersonDataMapper {

    public static PersonDataTPS tilPersonDataTPS(TpsPerson person) {
        return new PersonDataTPS().setKontonummer(person.getKontonummer());
    }

    public static TpsPerson tilTpsPerson(Person person) {
        return new TpsPerson().setKontonummer(kanskjeKontonummer(person));
    }

    private static String kanskjeKontonummer(no.nav.tjeneste.virksomhet.person.v3.informasjon.Person person) {
        if (person instanceof no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker) {
            no.nav.tjeneste.virksomhet.person.v3.informasjon.Bankkonto bankkonto =
                    ((no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker) person).getBankkonto();
            return kanskjeKontonummer(bankkonto);
        }
        return null;
    }

    private static String kanskjeKontonummer(no.nav.tjeneste.virksomhet.person.v3.informasjon.Bankkonto bankkonto) {
        if (bankkonto instanceof no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoNorge) {
            no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoNorge bankkontoNorge =
                    (no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoNorge) bankkonto;
            return ofNullable(bankkontoNorge.getBankkonto())
                    .map(no.nav.tjeneste.virksomhet.person.v3.informasjon.Bankkontonummer::getBankkontonummer)
                    .orElse(null);
        } else if (bankkonto instanceof no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoUtland) {
            no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoUtland wsBankkontoUtland =
                    (no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoUtland) bankkonto;
            return ofNullable(wsBankkontoUtland.getBankkontoUtland())
                    .map(no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontonummerUtland::getBankkontonummer)
                    .orElse(null);
        }
        return null;
    }
}