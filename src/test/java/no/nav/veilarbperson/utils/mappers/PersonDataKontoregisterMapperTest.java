package no.nav.veilarbperson.utils.mappers;

import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import no.nav.veilarbperson.client.kontoregister.KontoregisterClientImpl;
import no.nav.veilarbperson.domain.PersonDataKontoregister;
import org.junit.Test;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PersonDataKontoregisterMapperTest {

    @Test
    public void kontonummerMappesDersomBankkontoErNorge() {
        final String forventetVerdi = "123456789";

        final Person wsPerson = new Bruker().withBankkonto(
                new BankkontoNorge().withBankkonto(new Bankkontonummer().withBankkontonummer(forventetVerdi))
        );

        final PersonDataKontoregister kontoregisterPerson = KontoregisterClientImpl.PersonDataMapper.tilKontoregisterPerson(wsPerson);

        assertThat(kontoregisterPerson.getKontonummer(), is(forventetVerdi));
    }

    @Test
    public void kontonummerMappesDersomBankkontoErUtland() {
        final String forventetVerdi = "987654321";
        final Person wsPerson = new Bruker().withBankkonto(
                new BankkontoUtland().withBankkontoUtland(new BankkontonummerUtland().withBankkontonummer(forventetVerdi))
        );

        final PersonDataKontoregister kontoregisterPerson = KontoregisterClientImpl.PersonDataMapper.tilKontoregisterPerson(wsPerson);

        assertThat(kontoregisterPerson.getKontonummer(), is(forventetVerdi));
    }

    @Test
    public void kontonummerMappesTilNullDersomWSBankkontoErNull() {
        final Person wsPerson = new Bruker().withBankkonto(null);

        final PersonDataKontoregister kontoregisterPerson = KontoregisterClientImpl.PersonDataMapper.tilKontoregisterPerson(wsPerson);

        assertThat(kontoregisterPerson.getKontonummer(), nullValue());
    }

    @Test
    public void kontonummerMappesTilNullDersomWSBankkontonummerErNull() {
        final Person wsPerson = new Bruker().withBankkonto(
                new BankkontoNorge().withBankkonto(null)
        );

        final PersonDataKontoregister kontoregisterPerson = KontoregisterClientImpl.PersonDataMapper.tilKontoregisterPerson(wsPerson);

        assertThat(kontoregisterPerson.getKontonummer(), nullValue());
    }

    @Test
    public void kontonummerMappesTilNullDersomWSBankkontonummerVerdiErNull() {
        final Person wsPerson = new Bruker().withBankkonto(
                new BankkontoNorge().withBankkonto(new Bankkontonummer().withBankkontonummer(null))
        );

        final PersonDataKontoregister kontoregisterPerson = KontoregisterClientImpl.PersonDataMapper.tilKontoregisterPerson(wsPerson);

        assertThat(kontoregisterPerson.getKontonummer(), nullValue());
    }

    @Test
    public void kontonummerMappesTilNullDersomWSBankkontonummerUtlandErNull() {
        final Person wsPerson = new Bruker().withBankkonto(
                new BankkontoUtland().withBankkontoUtland(null)
        );

        final PersonDataKontoregister kontoregisterPerson = KontoregisterClientImpl.PersonDataMapper.tilKontoregisterPerson(wsPerson);

        assertThat(kontoregisterPerson.getKontonummer(), nullValue());
    }

    @Test
    public void kontonummerMappesTilNullDersomWSBankkontonummerUtlandVerdiErNull() {
        final Person wsPerson = new Bruker().withBankkonto(
                new BankkontoUtland().withBankkontoUtland(new BankkontonummerUtland().withBankkontonummer(null))
        );

        final PersonDataKontoregister kontoregisterPerson = KontoregisterClientImpl.PersonDataMapper.tilKontoregisterPerson(wsPerson);

        assertThat(kontoregisterPerson.getKontonummer(), nullValue());
    }

}
