package no.nav.fo.veilarbperson.consumer.tps.mappers;

import no.nav.fo.veilarbperson.domain.PersonData;
import no.nav.tjeneste.virksomhet.person.v2.informasjon.*;
import org.hamcrest.*;
import org.junit.Test;

import javax.xml.datatype.*;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Ja, her var det en del tester! Grunnen til at mapperen testes opp og ned og i mente er at det er en del annen kode
 * som potensielt brekker i to dersom mapperen endrer seg. Derfor må vi ha kontroll på når det skjer. Mapperen bør
 * også være robust nok til å håndtere at WS-objektene kan inneholde forskjellige kombinasjoner av data uten at
 * alt går galt. Dette testes også her.
 */

public class PersonDataMapperTest {

    private PersonDataMapper personDataMapper = new PersonDataMapper();

    @Test
    public void fornavnMappesDersomDetEksisterer() throws Exception {
        final String forventetVerdi = "fornavn";
        final WSPerson wsPerson = new WSPerson().withPersonnavn(new WSPersonnavn().withFornavn(forventetVerdi));

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getFornavn(), is(forventetVerdi));
    }

    @Test
    public void fornavnMappesTilNullDersomWSPersonnavnErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withPersonnavn(null);

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getFornavn(), nullValue());
    }

    @Test
    public void fornavnMappesTilNullDersomPersonnavnErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withPersonnavn(new WSPersonnavn().withFornavn(null));

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getFornavn(), nullValue());
    }

    @Test
    public void etternavnMappesDersomDetEksisterer() throws Exception {
        final String forventetVerdi = "etternavn";
        final WSPerson wsPerson = new WSPerson().withPersonnavn(new WSPersonnavn().withEtternavn(forventetVerdi));

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getEtternavn(), is(forventetVerdi));
    }

    @Test
    public void etternavnMappesTilNullDersomWSPersonnavnErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withPersonnavn(null);

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getEtternavn(), nullValue());
    }

    @Test
    public void etternavnMappesTilNullDersomEtternavnErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withPersonnavn(new WSPersonnavn().withEtternavn(null));

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getEtternavn(), nullValue());
    }

    @Test
    public void mellomnavnMappesDersomDetEksisterer() throws Exception {
        final String forventetVerdi = "mellomnavn";
        final WSPerson wsPerson = new WSPerson().withPersonnavn(new WSPersonnavn().withMellomnavn(forventetVerdi));

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getMellomnavn(), is(forventetVerdi));
    }

    @Test
    public void mellomnavnMappesTilNullDersomWSPersonnavnErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withPersonnavn(null);

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getMellomnavn(), nullValue());
    }

    @Test
    public void mellomnavnMappesTilNullDersomMellomnavnErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withPersonnavn(new WSPersonnavn().withMellomnavn(null));

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getMellomnavn(), nullValue());
    }

    @Test
    public void sammensattNavnMappesDersomDetEksisterer() throws Exception {
        final String forventetVerdi = "sammensattNavn";
        final WSPerson wsPerson = new WSPerson().withPersonnavn(new WSPersonnavn().withSammensattNavn(forventetVerdi));

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getSammensattNavn(), is(forventetVerdi));
    }

    @Test
    public void sammensattNavnMappesTilNullDersomWSPersonnavnErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withPersonnavn(null);

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getSammensattNavn(), nullValue());
    }

    @Test
    public void sammensattNavnMappesTilNullDersomMellomnavnErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withPersonnavn(new WSPersonnavn().withSammensattNavn(null));

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getSammensattNavn(), nullValue());
    }

    @Test
    public void personnummerMappesDersomDetEksisterer() throws Exception {
        final String forventetVerdi = "personnummer";
        final WSPerson wsPerson = new WSPerson().withIdent(new WSNorskIdent().withIdent(forventetVerdi));

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getPersonnummer(), is(forventetVerdi));
    }

    @Test
    public void personnummerMappesTilNullDersomWSNorskIdentErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withIdent(null);

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getPersonnummer(), nullValue());
    }

    @Test
    public void personnummerMappesTilNullDersomIdentErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withIdent(new WSNorskIdent().withIdent(null));

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getPersonnummer(), nullValue());
    }

    @Test
    public void fodselsdatoMappesDersomDenEksisterer() throws Exception {
        final int forventetAr = 1999;
        final int forventetManed = 12;
        final int forventetDag = 13;
        final XMLGregorianCalendar foedselsdato = lagDato(forventetAr, forventetManed, forventetDag);
        final WSPerson wsPerson = new WSPerson().withFoedselsdato(new WSFoedselsdato().withFoedselsdato(foedselsdato));

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getFodselsdato(), erDato(forventetAr, forventetManed, forventetDag));
    }

    @Test
    public void fodselsdatoMappesTilNullDersomWSFoedselsdatoErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withFoedselsdato(null);

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getFodselsdato(), nullValue());
    }

    @Test
    public void fodselsdatoMappesTilNullDersomFoedselsdatoErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withFoedselsdato(null);

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getFodselsdato(), nullValue());
    }

    @Test
    public void kjonnMappesDersomDetEksisterer() throws Exception {
        final String forventetVerdi = "K";
        final WSPerson wsPerson = new WSPerson().withKjoenn(new WSKjoenn().withKjoenn(new WSKjoennstyper().withValue(forventetVerdi)));

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getKjoenn(), is(forventetVerdi));
    }

    @Test
    public void kjonnMappesTilNullDersomWSKjoennErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withKjoenn(null);

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getKjoenn(), nullValue());
    }

    @Test
    public void kjonnMappesTilNullDersomWSKjoennstyperErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withKjoenn(new WSKjoenn().withKjoenn(null));

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getKjoenn(), nullValue());
    }

    @Test
    public void kjonnMappesTilNullDersomValueErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withKjoenn(new WSKjoenn().withKjoenn(new WSKjoennstyper().withValue(null)));

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getKjoenn(), nullValue());
    }

    @Test
    public void kode6MappesDersomDetEksisterer() throws Exception {
        final String forventetVerdi = "6";
        final WSPerson wsPerson = new WSPerson().withDiskresjonskode(new WSDiskresjonskoder().withValue(forventetVerdi));

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getDiskresjonskode(), is(forventetVerdi));
    }

    @Test
    public void kode7MappesDersomDetEksisterer() throws Exception {
        final String forventetVerdi = "7";
        final WSPerson wsPerson = new WSPerson().withDiskresjonskode(new WSDiskresjonskoder().withValue(forventetVerdi));

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getDiskresjonskode(), is(forventetVerdi));
    }

    @Test
    public void diskresjonskodeMappesTilNullDersomWSDiskresjonskoderErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withDiskresjonskode(null);

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getDiskresjonskode(), nullValue());
    }

    @Test
    public void diskresjonskodeMappesTilNullDersomValueErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withDiskresjonskode(new WSDiskresjonskoder().withValue(null));

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getDiskresjonskode(), nullValue());
    }

    @Test
    public void kontonummerMappesDersomBankkontoErNorge() throws Exception {
        final String forventetVerdi = "123456789";
        final WSPerson wsPerson = new WSPerson().withBankkonto(
                new WSBankkontoNorge().withBankkonto(new WSBankkontonummer().withBankkontonummer(forventetVerdi))
        );

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getKontonummer(), is(forventetVerdi));
    }

    @Test
    public void kontonummerMappesDersomBankkontoErUtland() throws Exception {
        final String forventetVerdi = "987654321";
        final WSPerson wsPerson = new WSPerson().withBankkonto(
                new WSBankkontoUtland().withBankkontoUtland(new WSBankkontonummerUtland().withBankkontonummer(forventetVerdi))
        );

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getKontonummer(), is(forventetVerdi));
    }

    @Test
    public void kontonummerMappesTilNullDersomWSBankkontoErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withBankkonto(null);

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getKontonummer(), nullValue());
    }

    @Test
    public void kontonummerMappesTilNullDersomWSBankkontonummerErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withBankkonto(
                new WSBankkontoNorge().withBankkonto(null)
        );

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getKontonummer(), nullValue());
    }

    @Test
    public void kontonummerMappesTilNullDersomWSBankkontonummerVerdiErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withBankkonto(
                new WSBankkontoNorge().withBankkonto(new WSBankkontonummer().withBankkontonummer(null))
        );

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getKontonummer(), nullValue());
    }

    @Test
    public void kontonummerMappesTilNullDersomWSBankkontonummerUtlandErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withBankkonto(
                new WSBankkontoUtland().withBankkontoUtland(null)
        );

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getKontonummer(), nullValue());
    }

    @Test
    public void kontonummerMappesTilNullDersomWSBankkontonummerUtlandVerdiErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withBankkonto(
                new WSBankkontoUtland().withBankkontoUtland(new WSBankkontonummerUtland().withBankkontonummer(null))
        );

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getKontonummer(), nullValue());
    }

    @Test
    public void ansvarligEnhetsnummerMappesDersomDetEksisterer() throws Exception {
        final String forventetVerdi = "2890";
        final WSPerson wsPerson = new WSBruker().withHarAnsvarligEnhet(
                new WSAnsvarligEnhet().withEnhet(new WSOrganisasjonsenhet().withOrganisasjonselementID(forventetVerdi))
        );

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getAnsvarligEnhetsnummer(), is(forventetVerdi));
    }

    @Test
    public void ansvarligEnhetsnummerMappesTilNullDersomInputIkkeErWSBruker() throws Exception {
        final WSPerson wsPerson = new WSPerson();

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getAnsvarligEnhetsnummer(), nullValue());
    }

    @Test
    public void ansvarligEnhetsnummerMappesTilNullDersomWSAnsvarligEnhetErNull() throws Exception {
        final WSPerson wsPerson = new WSBruker().withHarAnsvarligEnhet(null);

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getAnsvarligEnhetsnummer(), nullValue());
    }

    @Test
    public void ansvarligEnhetsnummerMappesTilNullDersomWSOrganisasjonsenhetErNull() throws Exception {
        final WSPerson wsPerson = new WSBruker().withHarAnsvarligEnhet(new WSAnsvarligEnhet().withEnhet(null));

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getAnsvarligEnhetsnummer(), nullValue());
    }

    @Test
    public void ansvarligEnhetsnummerMappesTilNullDersomOrganisasjonselementIDErNull() throws Exception {
        final WSPerson wsPerson = new WSBruker().withHarAnsvarligEnhet(
                new WSAnsvarligEnhet().withEnhet(new WSOrganisasjonsenhet().withOrganisasjonselementID(null))
        );

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getAnsvarligEnhetsnummer(), nullValue());
    }

    @Test
    public void statsborgerskapMappesDersomDetEksisterer() throws Exception {
        final String forventetVerdi = "NORGE";
        final WSPerson wsPerson = new WSPerson().withStatsborgerskap(
                new WSStatsborgerskap().withLand(new WSLandkoder().withValue(forventetVerdi))
        );

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getStatsborgerskap(), is(forventetVerdi));
    }

    @Test
    public void statsborgerskapMappesTilNullDersomWSStatsborgerskapErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withStatsborgerskap(null);

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getStatsborgerskap(), nullValue());
    }

    @Test
    public void statsborgerskapMappesTilNullDersomLandErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withStatsborgerskap(
                new WSStatsborgerskap().withLand(null)
        );

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getStatsborgerskap(), nullValue());
    }

    @Test
    public void statsborgerskapMappesTilNullDersomValueErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withStatsborgerskap(
                new WSStatsborgerskap().withLand(new WSLandkoder().withValue(null))
        );

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getStatsborgerskap(), nullValue());
    }

    @Test
    public void siviltilstandMappesDersomDetEksisterer() throws Exception {
        final String forventetSiviltilstand = "GIFT";
        final int forventetAr = 1999;
        final int forventetManed = 12;
        final int forventetDag = 13;
        final WSPerson wsPerson = new WSPerson().withSivilstand(
                new WSSivilstand().withFomGyldighetsperiode(lagDato(forventetAr, forventetManed, forventetDag))
                        .withSivilstand(new WSSivilstander().withValue(forventetSiviltilstand))
        );

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getSivilstand(), notNullValue());
        assertThat(personData.getSivilstand().getSivilstand(), is(forventetSiviltilstand));
        assertThat(personData.getSivilstand().getFraDato(), erDato(forventetAr, forventetManed, forventetDag));
    }

    @Test
    public void siviltilstandMappesTilNullDersomWSSivilstandErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withSivilstand(null);

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getSivilstand(), nullValue());
    }

    @Test
    public void dodsdatoMappesDersomDetEksisterer() throws Exception {
        final int forventetAr = 1999;
        final int forventetManed = 12;
        final int forventetDag = 13;
        final WSPerson wsPerson = new WSPerson().withDoedsdato(
                new WSDoedsdato().withDoedsdato(lagDato(forventetAr, forventetManed, forventetDag))
        );

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getDodsdato(), erDato(forventetAr, forventetManed, forventetDag));
    }

    @Test
    public void dodsdatoMappesTilNullDersomWSDoedsdatoErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withDoedsdato(null);

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getDodsdato(), nullValue());
    }

    @Test
    public void dodsdatoMappesTilNullDersomDoedsdatoErNull() throws Exception {
        final WSPerson wsPerson = new WSPerson().withDoedsdato(new WSDoedsdato().withDoedsdato(null));

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getDodsdato(), nullValue());
    }

    private Matcher<String> erDato(final int forventetAr, final int forventetManed, final int forventetDag) {
        return new CustomMatcher<String>("dato på formatet YYYY-MM-DD") {
            @Override
            public boolean matches(final Object o) {
                return (forventetAr + "-" + forventetManed + "-" + forventetDag).equals(o);
            }
        };
    }

    private XMLGregorianCalendar lagDato(final int ar, final int maned, final int dag) throws DatatypeConfigurationException {
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(ar, maned, dag, 1, 1, 1, 1, 1);
    }
}