package no.nav.fo.veilarbperson.consumer.tps.mappers;

import no.nav.fo.veilarbperson.domain.person.*;
import no.nav.fo.veilarbperson.domain.person.Gateadresse;
import no.nav.fo.veilarbperson.domain.person.Matrikkeladresse;
import no.nav.fo.veilarbperson.domain.person.UstrukturertAdresse;
import no.nav.fo.veilarbperson.domain.person.PostboksadresseNorsk;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bostedsadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Diskresjonskoder;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Sivilstand;
import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;

import javax.xml.datatype.XMLGregorianCalendar;

import static no.nav.fo.veilarbperson.consumer.tps.mappers.MapperTestUtils.lagDato;
import static org.hamcrest.Matchers.*;
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
        final Person wsPerson = new Person().withPersonnavn(new Personnavn().withFornavn(forventetVerdi));

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getFornavn(), is(forventetVerdi));
    }

    @Test
    public void fornavnMappesTilNullDersomWSPersonnavnErNull() throws Exception {
        final Person wsPerson = new Person().withPersonnavn(null);

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getFornavn(), nullValue());
    }

    @Test
    public void fornavnMappesTilNullDersomPersonnavnErNull() throws Exception {
        final Person wsPerson = new Person().withPersonnavn(new Personnavn().withFornavn(null));

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getFornavn(), nullValue());
    }

    @Test
    public void etternavnMappesDersomDetEksisterer() throws Exception {
        final String forventetVerdi = "etternavn";
        final Person wsPerson = new Person().withPersonnavn(new Personnavn().withEtternavn(forventetVerdi));

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getEtternavn(), is(forventetVerdi));
    }

    @Test
    public void etternavnMappesTilNullDersomWSPersonnavnErNull() throws Exception {
        final Person wsPerson = new Person().withPersonnavn(null);

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getEtternavn(), nullValue());
    }

    @Test
    public void etternavnMappesTilNullDersomEtternavnErNull() throws Exception {
        final Person wsPerson = new Person().withPersonnavn(new Personnavn().withEtternavn(null));

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getEtternavn(), nullValue());
    }

    @Test
    public void mellomnavnMappesDersomDetEksisterer() throws Exception {
        final String forventetVerdi = "mellomnavn";
        final Person wsPerson = new Person().withPersonnavn(new Personnavn().withMellomnavn(forventetVerdi));

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getMellomnavn(), is(forventetVerdi));
    }

    @Test
    public void mellomnavnMappesTilNullDersomWSPersonnavnErNull() throws Exception {
        final Person wsPerson = new Person().withPersonnavn(null);

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getMellomnavn(), nullValue());
    }

    @Test
    public void mellomnavnMappesTilNullDersomMellomnavnErNull() throws Exception {
        final Person wsPerson = new Person().withPersonnavn(new Personnavn().withMellomnavn(null));

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getMellomnavn(), nullValue());
    }

    @Test
    public void sammensattNavnMappesDersomDetEksisterer() throws Exception {
        final String forventetVerdi = "sammensattNavn";
        final Person wsPerson = new Person().withPersonnavn(new Personnavn().withSammensattNavn(forventetVerdi));

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getSammensattNavn(), is(forventetVerdi));
    }

    @Test
    public void sammensattNavnMappesTilNullDersomWSPersonnavnErNull() throws Exception {
        final Person wsPerson = new Person().withPersonnavn(null);

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getSammensattNavn(), nullValue());
    }

    @Test
    public void sammensattNavnMappesTilNullDersomMellomnavnErNull() throws Exception {
        final Person wsPerson = new Person().withPersonnavn(new Personnavn().withSammensattNavn(null));

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getSammensattNavn(), nullValue());
    }

    @Test
    public void fodselsnummerMappesDersomDetEksisterer() throws Exception {
        final String forventetVerdi = "fodseslnummer";
        final Person wsPerson = new Person().withAktoer(new PersonIdent().withIdent(new NorskIdent().withIdent(forventetVerdi)));

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getFodselsnummer(), is(forventetVerdi));
    }

    @Test
    public void fodselsnummerMappesTilNullDersomWSAktoerErNull() throws Exception {
        final Person wsPerson = new Person();

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getFodselsnummer(), nullValue());
    }

    @Test
    public void fodselsnummerMappesTilNullDersomWSNorskIdentErNull() throws Exception {
        final Person wsPerson = new Person().withAktoer(new PersonIdent());

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getFodselsnummer(), nullValue());
    }

    @Test
    public void fodselsnummerMappesTilNullDersomIdentErNull() throws Exception {
        final Person wsPerson = new Person().withAktoer(new PersonIdent().withIdent(new NorskIdent().withIdent(null)));

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getFodselsnummer(), nullValue());
    }

    @Test
    public void fodselsdatoMappesDersomDenEksisterer() throws Exception {
        final int forventetAr = 1999;
        final int forventetManed = 12;
        final int forventetDag = 13;
        final XMLGregorianCalendar foedselsdato = lagDato(forventetAr, forventetManed, forventetDag);
        final Person wsPerson = new Person().withFoedselsdato(new Foedselsdato().withFoedselsdato(foedselsdato));

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getFodselsdato(), erDato(forventetAr, forventetManed, forventetDag));
    }

    @Test
    public void fodselsdatoMappesTilNullDersomWSFoedselsdatoErNull() throws Exception {
        final Person wsPerson = new Person().withFoedselsdato(null);

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getFodselsdato(), nullValue());
    }

    @Test
    public void fodselsdatoMappesTilNullDersomFoedselsdatoErNull() throws Exception {
        final Person wsPerson = new Person().withFoedselsdato(null);

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getFodselsdato(), nullValue());
    }

    @Test
    public void kjonnMappesDersomDetEksisterer() throws Exception {
        final String forventetVerdi = "K";
        final Person wsPerson = new Person().withKjoenn(new Kjoenn().withKjoenn(new Kjoennstyper().withValue(forventetVerdi)));

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getKjoenn(), is(forventetVerdi));
    }

    @Test
    public void kjonnMappesTilNullDersomWSKjoennErNull() throws Exception {
        final Person wsPerson = new Person().withKjoenn(null);

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getKjoenn(), nullValue());
    }

    @Test
    public void kjonnMappesTilNullDersomWSKjoennstyperErNull() throws Exception {
        final Person wsPerson = new Person().withKjoenn(new Kjoenn().withKjoenn(null));

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getKjoenn(), nullValue());
    }

    @Test
    public void kjonnMappesTilNullDersomValueErNull() throws Exception {
        final Person wsPerson = new Person().withKjoenn(new Kjoenn().withKjoenn(new Kjoennstyper().withValue(null)));

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getKjoenn(), nullValue());
    }

    @Test
    public void kode6MappesDersomDetEksisterer() throws Exception {
        final String forventetVerdi = "6";
        final Person wsPerson = new Person().withDiskresjonskode(new Diskresjonskoder()
                .withValue(no.nav.fo.veilarbperson.domain.person.Diskresjonskoder.STRENGT_FORTROLIG_ADRESSE.kodeverkVerdi));

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getDiskresjonskode(), is(forventetVerdi));
    }

    @Test
    public void kode7MappesDersomDetEksisterer() throws Exception {
        final String forventetVerdi = "7";
        final Person wsPerson = new Person().withDiskresjonskode(new Diskresjonskoder()
                .withValue(no.nav.fo.veilarbperson.domain.person.Diskresjonskoder.FORTROLIG_ADRESSE.kodeverkVerdi));

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getDiskresjonskode(), is(forventetVerdi));
    }

    @Test
    public void diskresjonskodeMappesTilNullDersomWSDiskresjonskoderErNull() throws Exception {
        final Person wsPerson = new Person().withDiskresjonskode(null);

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getDiskresjonskode(), nullValue());
    }

    @Test
    public void diskresjonskodeMappesTilNullDersomValueErNull() throws Exception {
        final Person wsPerson = new Person().withDiskresjonskode(new Diskresjonskoder().withValue(null));

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getDiskresjonskode(), nullValue());
    }

    @Test
    public void kontonummerMappesDersomBankkontoErNorge() throws Exception {
        final String forventetVerdi = "123456789";

        final Person wsPerson = new Bruker().withBankkonto(
                new BankkontoNorge().withBankkonto(new Bankkontonummer().withBankkontonummer(forventetVerdi))
        );

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getKontonummer(), is(forventetVerdi));
    }

    @Test
    public void kontonummerMappesDersomBankkontoErUtland() throws Exception {
        final String forventetVerdi = "987654321";
        final Person wsPerson = new Bruker().withBankkonto(
                new BankkontoUtland().withBankkontoUtland(new BankkontonummerUtland().withBankkontonummer(forventetVerdi))
        );

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getKontonummer(), is(forventetVerdi));
    }

    @Test
    public void kontonummerMappesTilNullDersomWSBankkontoErNull() throws Exception {
        final Person wsPerson = new Bruker().withBankkonto(null);

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getKontonummer(), nullValue());
    }

    @Test
    public void kontonummerMappesTilNullDersomWSBankkontonummerErNull() throws Exception {
        final Person wsPerson = new Bruker().withBankkonto(
                new BankkontoNorge().withBankkonto(null)
        );

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getKontonummer(), nullValue());
    }

    @Test
    public void kontonummerMappesTilNullDersomWSBankkontonummerVerdiErNull() throws Exception {
        final Person wsPerson = new Bruker().withBankkonto(
                new BankkontoNorge().withBankkonto(new Bankkontonummer().withBankkontonummer(null))
        );

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getKontonummer(), nullValue());
    }

    @Test
    public void kontonummerMappesTilNullDersomWSBankkontonummerUtlandErNull() throws Exception {
        final Person wsPerson = new Bruker().withBankkonto(
                new BankkontoUtland().withBankkontoUtland(null)
        );

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getKontonummer(), nullValue());
    }

    @Test
    public void kontonummerMappesTilNullDersomWSBankkontonummerUtlandVerdiErNull() throws Exception {
        final Person wsPerson = new Bruker().withBankkonto(
                new BankkontoUtland().withBankkontoUtland(new BankkontonummerUtland().withBankkontonummer(null))
        );

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getKontonummer(), nullValue());
    }

    @Test
    public void geografiskTilknytningKommuneMappesDersomDetEksisterer() throws Exception {
        final String forventetVerdi = "2890";
        final Person wsPerson = new Bruker().withGeografiskTilknytning(
                new Kommune().withGeografiskTilknytning(forventetVerdi));

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getGeografiskTilknytning(), is(forventetVerdi));
    }

    @Test
    public void geografiskTilknytningLandMappesDersomDetEksisterer() throws Exception {
        final String forventetVerdi = "SWE";
        final Person wsPerson = new Bruker().withGeografiskTilknytning(
                new Land().withGeografiskTilknytning(forventetVerdi));

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getGeografiskTilknytning(), is(forventetVerdi));
    }

    @Test
    public void geografiskTilknytningBydelMappesDersomDetEksisterer() throws Exception {
        final String forventetVerdi = "289033";
        final Person wsPerson = new Bruker().withGeografiskTilknytning(
                new Bydel().withGeografiskTilknytning(forventetVerdi));

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getGeografiskTilknytning(), is(forventetVerdi));
    }

    @Test
    public void geografiskTilknytningMappesTilNullDersomInputIkkeErWSBruker() throws Exception {
        final Person wsPerson = new Person();

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getGeografiskTilknytning(), nullValue());
    }

    @Test
    public void geografiskTilknytningMappesTilNullDersomWSAnsvarligEnhetErNull() throws Exception {
        final Person wsPerson = new Bruker().withGeografiskTilknytning(null);

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getGeografiskTilknytning(), nullValue());
    }

    @Test
    public void geografiskTilknytningMappesTilNullDersomGeografiskTilknytning() throws Exception {
        final Person wsPerson = new Bruker().withGeografiskTilknytning(new Bydel().withGeografiskTilknytning(null));

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getGeografiskTilknytning(), nullValue());
    }

    @Test
    public void statsborgerskapMappesDersomDetEksisterer() throws Exception {
        final String forventetVerdi = "NORGE";
        final Person wsPerson = new Person().withStatsborgerskap(
                new Statsborgerskap().withLand(new Landkoder().withValue(forventetVerdi))
        );

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getStatsborgerskap(), is(forventetVerdi));
    }

    @Test
    public void statsborgerskapMappesTilNullDersomWSStatsborgerskapErNull() throws Exception {
        final Person wsPerson = new Person().withStatsborgerskap(null);

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getStatsborgerskap(), nullValue());
    }

    @Test
    public void statsborgerskapMappesTilNullDersomLandErNull() throws Exception {
        final Person wsPerson = new Person().withStatsborgerskap(
                new Statsborgerskap().withLand(null)
        );

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getStatsborgerskap(), nullValue());
    }

    @Test
    public void statsborgerskapMappesTilNullDersomValueErNull() throws Exception {
        final Person wsPerson = new Person().withStatsborgerskap(
                new Statsborgerskap().withLand(new Landkoder().withValue(null))
        );

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getStatsborgerskap(), nullValue());
    }

    @Test
    public void siviltilstandMappesDersomDetEksisterer() throws Exception {
        final String forventetSiviltilstand = "GIFT";
        final int forventetAr = 1999;
        final int forventetManed = 12;
        final int forventetDag = 13;
        final Person wsPerson = new Person().withSivilstand(
                new Sivilstand().withFomGyldighetsperiode(lagDato(forventetAr, forventetManed, forventetDag))
                        .withSivilstand(new Sivilstander().withValue(forventetSiviltilstand))
        );

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getSivilstand(), notNullValue());
        assertThat(personData.getSivilstand().getSivilstand(), is(forventetSiviltilstand));
        assertThat(personData.getSivilstand().getFraDato(), erDato(forventetAr, forventetManed, forventetDag));
    }

    @Test
    public void siviltilstandMappesTilNullDersomWSSivilstandErNull() throws Exception {
        final Person wsPerson = new Person().withSivilstand(null);

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getSivilstand(), nullValue());
    }

    @Test
    public void dodsdatoMappesDersomDetEksisterer() throws Exception {
        final int forventetAr = 1999;
        final int forventetManed = 12;
        final int forventetDag = 13;
        final Person wsPerson = new Person().withDoedsdato(
                new Doedsdato().withDoedsdato(lagDato(forventetAr, forventetManed, forventetDag))
        );

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getDodsdato(), erDato(forventetAr, forventetManed, forventetDag));
    }

    @Test
    public void dodsdatoMappesTilNullDersomWSDoedsdatoErNull() throws Exception {
        final Person wsPerson = new Person().withDoedsdato(null);

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getDodsdato(), nullValue());
    }

    @Test
    public void dodsdatoMappesTilNullDersomDoedsdatoErNull() throws Exception {
        final Person wsPerson = new Person().withDoedsdato(new Doedsdato().withDoedsdato(null));

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getDodsdato(), nullValue());
    }

    @Test
    public void verdierIWSGateadresseMappesDersomDeEksisterer() throws Exception {
        final String forventetGatenavn = "gatenavn";
        final String forventetPostnummer = "0000";
        final int forventetHusnummer = 12;
        final String forventetHusbokstav = "A";
        final int forventetGatenummer = 1;
        final String forventetKommunenummer = "1234";
        final Person wsPerson = new Person().withBostedsadresse(
                new Bostedsadresse().withStrukturertAdresse(
                        lagGateadresse(forventetGatenavn, forventetGatenummer, forventetHusnummer, forventetHusbokstav, forventetKommunenummer, new Postnummer().withValue(forventetPostnummer))
                )
        );

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getBostedsadresse(), notNullValue());
        assertThat(personData.getBostedsadresse().getStrukturertAdresse(), notNullValue());
        assertThat(personData.getBostedsadresse().getStrukturertAdresse(), instanceOf(Gateadresse.class));
        final Gateadresse gateadresse = (Gateadresse) personData.getBostedsadresse().getStrukturertAdresse();

        sjekkAtGateadresseHarForventaVerdier(
                gateadresse,
                forventetGatenavn,
                forventetGatenummer,
                forventetHusnummer,
                forventetHusbokstav,
                forventetKommunenummer,
                forventetPostnummer
        );
    }

    @Test
    public void verdierIWSGateadresseMappesTilNullDersomDeErNull() throws Exception {
        final Person wsPerson = new Person().withBostedsadresse(
                new Bostedsadresse().withStrukturertAdresse(
                        lagGateadresse(null, 0, 0, null, null, new Postnummer())
                )
        );

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getBostedsadresse(), notNullValue());
        assertThat(personData.getBostedsadresse().getStrukturertAdresse(), notNullValue());
        assertThat(personData.getBostedsadresse().getStrukturertAdresse(), instanceOf(Gateadresse.class));
        final Gateadresse gateadresse = (Gateadresse) personData.getBostedsadresse().getStrukturertAdresse();

        sjekkAtGateadresseHarForventaVerdier(
                gateadresse,
                null,
                0,
                0,
                null,
                null,
                null);
    }

    @Test
    public void verdierIWSMatrikkeladresseMappesDersomDeEksisterer() throws Exception {
        final String forventetPostnummer = "0000";
        final String forventetGardsnummer = "gaardsnummer";
        final String forventetBruksnummer = "bruksnummer";
        final String forventetFestenummer = "festenummer";
        final String forventetSeksjonsnummer = "seksjonsnummer";
        final String forventetUndernummer = "undernummer";
        final String forventetEiendomsnavn = "eiendomsnavn";
        final Person wsPerson = new Person().withBostedsadresse(
                new Bostedsadresse().withStrukturertAdresse(
                        lagMatrikkeladresse(
                                forventetEiendomsnavn,
                                forventetGardsnummer,
                                forventetBruksnummer,
                                forventetFestenummer,
                                forventetSeksjonsnummer,
                                forventetUndernummer,
                                forventetPostnummer)
                )
        );

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getBostedsadresse(), notNullValue());
        assertThat(personData.getBostedsadresse().getStrukturertAdresse(), notNullValue());
        assertThat(personData.getBostedsadresse().getStrukturertAdresse(), instanceOf(Matrikkeladresse.class));
        final Matrikkeladresse matrikkeladresse = (Matrikkeladresse) personData.getBostedsadresse().getStrukturertAdresse();

        sjekkAtMatrikkeladresseHarForventaVerdier(
                matrikkeladresse,
                forventetEiendomsnavn,
                forventetGardsnummer,
                forventetBruksnummer,
                forventetFestenummer,
                forventetSeksjonsnummer,
                forventetUndernummer,
                forventetPostnummer
        );
    }

    @Test
    public void verdierIWSMatrikkeladresseMappesTilNullDersomDeErNull() throws Exception {
        final Person wsPerson = new Person().withBostedsadresse(
                new Bostedsadresse().withStrukturertAdresse(
                        lagMatrikkeladresse(null, null, null, null, null, null, null)
                )
        );

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getBostedsadresse(), notNullValue());
        assertThat(personData.getBostedsadresse().getStrukturertAdresse(), notNullValue());
        assertThat(personData.getBostedsadresse().getStrukturertAdresse(), instanceOf(Matrikkeladresse.class));
        final Matrikkeladresse matrikkeladresse = (Matrikkeladresse) personData.getBostedsadresse().getStrukturertAdresse();
        sjekkAtMatrikkeladresseHarForventaVerdier(matrikkeladresse, null, null, null, null, null, null, null);
    }

    @Test
    public void verdierIWSPostadresseNorskMappesDersomDeEksisterer() throws Exception {
        final String forventetPostboksanlegg = "postboksanlegg";
        final String forventetPostboksnummer = "postboksnummer";
        final String forventetPostnummer = "0000";
        final Person wsPerson = new Person().withBostedsadresse(new Bostedsadresse().withStrukturertAdresse(
                new no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk()
                        .withPostboksanlegg(forventetPostboksanlegg)
                        .withPostboksnummer(forventetPostboksnummer)
                        .withPoststed(new Postnummer().withValue(forventetPostnummer))
                )
        );

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getBostedsadresse(), notNullValue());
        assertThat(personData.getBostedsadresse().getStrukturertAdresse(), notNullValue());
        assertThat(personData.getBostedsadresse().getStrukturertAdresse(), instanceOf(PostboksadresseNorsk.class));
        final PostboksadresseNorsk postboksadresseNorsk = (PostboksadresseNorsk) personData.getBostedsadresse().getStrukturertAdresse();
        assertThat(postboksadresseNorsk.getPostboksanlegg(), is(forventetPostboksanlegg));
        assertThat(postboksadresseNorsk.getPostboksnummer(), is(forventetPostboksnummer));
        assertThat(postboksadresseNorsk.getPostnummer(), is(forventetPostnummer));
    }

    @Test
    public void verdierIWSPostadresseNorskTilNullDersomDeErNull() throws Exception {
        final Person wsPerson = new Person().withBostedsadresse(new Bostedsadresse().withStrukturertAdresse(
                new no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk()
                        .withPostboksanlegg(null)
                        .withPostboksnummer(null)
                        .withPoststed(new Postnummer().withValue(null))
                )
        );

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getBostedsadresse(), notNullValue());
        assertThat(personData.getBostedsadresse().getStrukturertAdresse(), notNullValue());
        assertThat(personData.getBostedsadresse().getStrukturertAdresse(), instanceOf(PostboksadresseNorsk.class));
        final PostboksadresseNorsk postboksadresseNorsk = (PostboksadresseNorsk) personData.getBostedsadresse().getStrukturertAdresse();
        assertThat(postboksadresseNorsk.getPostboksanlegg(), nullValue());
        assertThat(postboksadresseNorsk.getPostboksnummer(), nullValue());
        assertThat(postboksadresseNorsk.getPostnummer(), nullValue());
    }

    @Test
    public void wsStrukturertAdresseLandkodeMappesDersomDenEksisterer() throws Exception {
        final String forventetLandkode = "NO";
        final Person wsPerson = new Person().withBostedsadresse(new Bostedsadresse().withStrukturertAdresse(
                new no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk()
                        .withPoststed(new Postnummer())
                        .withLandkode(new Landkoder().withValue(forventetLandkode)))
        );

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getBostedsadresse(), notNullValue());
        assertThat(personData.getBostedsadresse().getStrukturertAdresse(), notNullValue());
        assertThat(personData.getBostedsadresse().getStrukturertAdresse().getLandkode(), is(forventetLandkode));
    }

    @Test
    public void wsStrukturertAdresseLandkodeMappesTilNullDersomDenErNull() throws Exception {
        final Person wsPerson = new Person().withBostedsadresse(new Bostedsadresse().withStrukturertAdresse(
                new no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk()
                        .withPoststed(new Postnummer())
                        .withLandkode(null))
        );

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getBostedsadresse(), notNullValue());
        assertThat(personData.getBostedsadresse().getStrukturertAdresse(), notNullValue());
        assertThat(personData.getBostedsadresse().getStrukturertAdresse().getLandkode(), nullValue());
    }

    @Test
    public void wsStrukturertAdresseTilleggsadresseMappesDersomDenEksisterer() throws Exception {
        final String forventetTilleggsadresse = "C/O tilleggsadresse";
        final Person wsPerson = new Person().withBostedsadresse(new Bostedsadresse().withStrukturertAdresse(
                new no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk()
                        .withPoststed(new Postnummer())
                        .withTilleggsadresse(forventetTilleggsadresse)));

        final PersonData personData = tilPersonData(wsPerson);
        assertThat(personData.getBostedsadresse(), notNullValue());
        assertThat(personData.getBostedsadresse().getStrukturertAdresse(), notNullValue());
        assertThat(personData.getBostedsadresse().getStrukturertAdresse().getTilleggsadresse(), is(forventetTilleggsadresse));
    }

    @Test
    public void wsStrukturertAdresseTilleggsadresseMappesTilNullDersomDenErNull() {
        final Person wsPerson = new Person().withBostedsadresse(new Bostedsadresse().withStrukturertAdresse(
                new no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk()
                        .withPoststed(new Postnummer())
                        .withTilleggsadresse(null)));

        final PersonData personData = tilPersonData(wsPerson);
        assertThat(personData.getBostedsadresse(), notNullValue());
        assertThat(personData.getBostedsadresse().getStrukturertAdresse(), notNullValue());
        assertThat(personData.getBostedsadresse().getStrukturertAdresse().getTilleggsadresse(), nullValue());
    }

    @Test
    public void wsStrukturertAdresseLandkodeMappesTilNullDersomValueDenErNull() throws Exception {
        final Person wsPerson = new Person().withBostedsadresse(new Bostedsadresse().withStrukturertAdresse(
                new no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk()
                        .withPoststed(new Postnummer())
                        .withLandkode(new Landkoder().withValue(null)))
        );

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getBostedsadresse(), notNullValue());
        assertThat(personData.getBostedsadresse().getStrukturertAdresse(), notNullValue());
        assertThat(personData.getBostedsadresse().getStrukturertAdresse().getLandkode(), nullValue());
    }

    @Test
    public void wsMidlertidigPostadresseNorgeMappesDersomDenEksisterer() throws Exception {
        final String forventetGatenavn = "gatenavn";
        final String forventetPostnummer = "0000";
        final int forventetHusnummer = 12;
        final String forventetHusbokstav = "A";
        final int forventetGatenummer = 1;
        final String forventetKommunenummer = "1234";
        final Person wsPerson = new Bruker().withMidlertidigPostadresse(
                new MidlertidigPostadresseNorge().withStrukturertAdresse(
                        lagGateadresse(forventetGatenavn, forventetGatenummer, forventetHusnummer, forventetHusbokstav, forventetKommunenummer, new Postnummer().withValue(forventetPostnummer))
                )
        );

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getMidlertidigAdresseNorge(), notNullValue());
        assertThat(personData.getMidlertidigAdresseNorge().getStrukturertAdresse(), notNullValue());
        assertThat(personData.getMidlertidigAdresseNorge().getStrukturertAdresse(), instanceOf(Gateadresse.class));
        final Gateadresse gateadresse = (Gateadresse) personData.getMidlertidigAdresseNorge().getStrukturertAdresse();
        sjekkAtGateadresseHarForventaVerdier(
                gateadresse,
                forventetGatenavn,
                forventetGatenummer,
                forventetHusnummer,
                forventetHusbokstav,
                forventetKommunenummer,
                forventetPostnummer
        );
    }

    @Test
    public void wsMidlertidigPostadresseNorgeMappesTilNullDersomDenErNull() throws Exception {
        final Person wsPerson = new Bruker().withMidlertidigPostadresse(
                new MidlertidigPostadresseNorge().withStrukturertAdresse(
                        lagGateadresse(null, 0, 0, null, null, new Postnummer())
                )
        );

        final PersonData personData = tilPersonData(wsPerson);

        assertThat(personData.getMidlertidigAdresseNorge(), notNullValue());
        assertThat(personData.getMidlertidigAdresseNorge().getStrukturertAdresse(), notNullValue());
        assertThat(personData.getMidlertidigAdresseNorge().getStrukturertAdresse(), instanceOf(Gateadresse.class));
        final Gateadresse gateadresse = (Gateadresse) personData.getMidlertidigAdresseNorge().getStrukturertAdresse();
        sjekkAtGateadresseHarForventaVerdier(gateadresse, null, 0, 0, null, null, null);
    }

    @Test
    public void wsMidlertidigPostadresseNorgeMappesTilNullDersomPersonIkkeErBruker() throws Exception {
        final Person wsPerson = new Person().withBostedsadresse(
                new Bostedsadresse().withStrukturertAdresse(
                        lagGateadresse("gatenavn", 0, 0, "Husbokstav", "Kommunenummer", new Postnummer().withValue("Poststed"))
                )
        );

        final PersonData personData = tilPersonData(wsPerson);
        assertThat(personData.getMidlertidigAdresseNorge(), nullValue());
    }

    @Test
    public void wsMidlertidigPostadresseUtlandMappesDersomDenEksisterer() throws Exception {
        String forventetAdresselinje1 = "Adresselinje1";
        String forventetAdresselinje2 = "Adresselinje2";
        String forventetAdresselinje3 = "Adresselinje3";
        String forventetAdresselinje4 = "Adresselinje4";
        String forventetLandkode = "LAND";
        final Person wsPerson = new Bruker().withMidlertidigPostadresse(
                new MidlertidigPostadresseUtland().withUstrukturertAdresse(
                        lagUstrukturertAdresse(forventetAdresselinje1, forventetAdresselinje2, forventetAdresselinje3, forventetAdresselinje4, forventetLandkode)
                ));

        final PersonData personData = tilPersonData(wsPerson);
        assertThat(personData.getMidlertidigAdresseUtland(), notNullValue());
        UstrukturertAdresse ustrukturertAdresse = personData.getMidlertidigAdresseUtland().getUstrukturertAdresse();
        sjekkAtUstrukturertAdresseHarForventaVerdier(ustrukturertAdresse, forventetAdresselinje1, forventetAdresselinje2, forventetAdresselinje3, forventetAdresselinje4, forventetLandkode);
    }

    @Test
    public void wsMidlertidigPostadresseUtlandMappesTilNullDersomDenErNull()throws Exception {
        final Person wsPerson = new Bruker().withMidlertidigPostadresse(
                new MidlertidigPostadresseUtland().withUstrukturertAdresse(
                        lagUstrukturertAdresse(null, null, null, null, null)
                ));

        final PersonData personData = tilPersonData(wsPerson);
        assertThat(personData.getMidlertidigAdresseUtland(), notNullValue());
        UstrukturertAdresse ustrukturertAdresse = personData.getMidlertidigAdresseUtland().getUstrukturertAdresse();
        sjekkAtUstrukturertAdresseHarForventaVerdier(ustrukturertAdresse, null, null, null, null, null);
    }
    @Test
    public void wsMidlertidigPostadresseUtlandetMappesTilNullDersomPersonIkkeErBruker() throws Exception {
        final Person wsPerson = new Person().withBostedsadresse(
                new Bostedsadresse().withStrukturertAdresse(
                        lagGateadresse(null, 0, 0, null, null, new Postnummer().withValue(null))
                ));

        final PersonData personData = tilPersonData(wsPerson);
        assertThat(personData.getMidlertidigAdresseUtland(), nullValue());
    }

    @Test
    public void wsPostadressSkalMappesDersomDenEksisterer() throws Exception {
        String forventetAdresselinje1 = "Adresselinje1";
        String forventetAdresselinje2 = "Adresselinje2";
        String forventetAdresselinje3 = "Adresselinje3";
        String forventetAdresselinje4 = "Adresselinje4";
        String forventetLandkode = "LAND";
        final Person wsPerson = new Person().withPostadresse(
                new no.nav.tjeneste.virksomhet.person.v3.informasjon.Postadresse().withUstrukturertAdresse(
                        lagUstrukturertAdresse(forventetAdresselinje1, forventetAdresselinje2, forventetAdresselinje3, forventetAdresselinje4, forventetLandkode)
                ));


        final PersonData personData = tilPersonData(wsPerson);
        assertThat(personData.getPostAdresse(), notNullValue());
        UstrukturertAdresse ustrukturertAdresse = personData.getPostAdresse().getUstrukturertAdresse();
        sjekkAtUstrukturertAdresseHarForventaVerdier(ustrukturertAdresse, forventetAdresselinje1, forventetAdresselinje2, forventetAdresselinje3, forventetAdresselinje4, forventetLandkode);
    }

    @Test
    public void wsPostadresseSkalMappesTilNullDersomDenErNull() throws Exception {
        final Person wsPerson = new Person().withPostadresse(
                new Postadresse().withUstrukturertAdresse(lagUstrukturertAdresse(null, null, null, null, null)));

        final PersonData personData = tilPersonData(wsPerson);
        assertThat(personData.getPostAdresse(), notNullValue());
        UstrukturertAdresse ustrukturertAdresse = personData.getPostAdresse().getUstrukturertAdresse();
        sjekkAtUstrukturertAdresseHarForventaVerdier(ustrukturertAdresse, null, null, null, null, null);
    }

    @Test
    public void malformMappesDersomDetEksisterer() {
        final Person wsPerson = new Bruker().withMaalform(new Spraak().withValue("NB"));

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getMalform(), is("NB"));
    }

    @Test
    public void malformMappesTilNullDersommalformErNull() {
        final Person wsPerson = new Bruker().withMaalform(null);

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getMalform(), nullValue());
    }

    @Test
    public void malformMappesTilNullDersommalformVerdiErNull() {
        final Person wsPerson = new Bruker().withMaalform(new Spraak().withValue(null));

        final PersonData personData = personDataMapper.tilPersonData(wsPerson);

        assertThat(personData.getMalform(), nullValue());
    }

    private PersonData tilPersonData(Person person) {
        return personDataMapper.tilPersonData(person);
    }

    private no.nav.tjeneste.virksomhet.person.v3.informasjon.UstrukturertAdresse lagUstrukturertAdresse(String adresselinje1, String adresselinje2, String adresselinje3, String adresselinje4, String landkode) {

        return new no.nav.tjeneste.virksomhet.person.v3.informasjon.UstrukturertAdresse()
                .withAdresselinje1(adresselinje1)
                .withAdresselinje2(adresselinje2)
                .withAdresselinje3(adresselinje3)
                .withAdresselinje4(adresselinje4)
                .withLandkode(new Landkoder().withValue(landkode));
    }

    private void sjekkAtUstrukturertAdresseHarForventaVerdier(
            UstrukturertAdresse ustrukturertAdresse,
            String forventaAdresselinje1,
            String forventaAdresselinje2,
            String forventaAdresselinje3,
            String forventaAdresselinje4,
            String forventaLandkode) {

        assertThat(ustrukturertAdresse, notNullValue());
        assertThat(ustrukturertAdresse, instanceOf(UstrukturertAdresse.class));
        assertThat(ustrukturertAdresse.getAdresselinje1(), is(forventaAdresselinje1));
        assertThat(ustrukturertAdresse.getAdresselinje2(), is(forventaAdresselinje2));
        assertThat(ustrukturertAdresse.getAdresselinje3(), is(forventaAdresselinje3));
        assertThat(ustrukturertAdresse.getAdresselinje4(), is(forventaAdresselinje4));
        assertThat(ustrukturertAdresse.getLandkode(), is(forventaLandkode));
    }

    private no.nav.tjeneste.virksomhet.person.v3.informasjon.Gateadresse lagGateadresse(String gatenavn, int gatenummer, int husnummer, String husbokstav, String kommunenummer, Postnummer poststed) {

        return new no.nav.tjeneste.virksomhet.person.v3.informasjon.Gateadresse()
                .withGatenavn(gatenavn)
                .withGatenummer(gatenummer)
                .withHusbokstav(husbokstav)
                .withHusnummer(husnummer)
                .withKommunenummer(kommunenummer)
                .withPoststed(poststed);
    }

    private void sjekkAtGateadresseHarForventaVerdier(
            Gateadresse gateadresse,
            String forventetGatenavn,
            int forventetGatenummer,
            int forventetHusnummer,
            String forventetHusbokstav,
            String forventetKommunenummer,
            String forventetPostnummer) {

        assertThat(gateadresse.getGatenavn(), is(forventetGatenavn));
        assertThat(gateadresse.getHusnummer(), is(forventetHusnummer));
        assertThat(gateadresse.getHusbokstav(), is(forventetHusbokstav));
        assertThat(gateadresse.getGatenummer(), is(forventetGatenummer));
        assertThat(gateadresse.getKommunenummer(), is(forventetKommunenummer));
        assertThat(gateadresse.getPostnummer(), is(forventetPostnummer));
    }

    private no.nav.tjeneste.virksomhet.person.v3.informasjon.Matrikkeladresse lagMatrikkeladresse(
            String eiendomsnavn,
            String gardsnummer,
            String bruksnummer,
            String festenummer,
            String seksjonsnummer,
            String undernummer,
            String postnummer) {

        return new no.nav.tjeneste.virksomhet.person.v3.informasjon.Matrikkeladresse()
                .withEiendomsnavn(eiendomsnavn)
                .withPoststed(new Postnummer().withValue(postnummer))
                .withMatrikkelnummer(
                        new no.nav.tjeneste.virksomhet.person.v3.informasjon.Matrikkelnummer()
                                .withGaardsnummer(gardsnummer)
                                .withBruksnummer(bruksnummer)
                                .withFestenummer(festenummer)
                                .withSeksjonsnummer(seksjonsnummer)
                                .withUndernummer(undernummer));
    }

    private void sjekkAtMatrikkeladresseHarForventaVerdier(
            Matrikkeladresse matrikkeladresse,
            String forventetEiendomsnavn,
            String forventetGardsnummer,
            String forventetBruksnummer,
            String forventetFestenummer,
            String forventetSeksjonsnummer,
            String forventetUndernummer,
            String forventetPostnummer) {

        assertThat(matrikkeladresse.getEiendomsnavn(), is(forventetEiendomsnavn));
        assertThat(matrikkeladresse.getGardsnummer(), is(forventetGardsnummer));
        assertThat(matrikkeladresse.getBruksnummer(), is(forventetBruksnummer));
        assertThat(matrikkeladresse.getFestenummer(), is(forventetFestenummer));
        assertThat(matrikkeladresse.getSeksjonsnummer(), is(forventetSeksjonsnummer));
        assertThat(matrikkeladresse.getUndernummer(), is(forventetUndernummer));
        assertThat(matrikkeladresse.getPostnummer(), is(forventetPostnummer));
    }

    private Matcher<String> erDato(final int forventetAr, final int forventetManed, final int forventetDag) {
        return new CustomMatcher<String>("dato på formatet YYYY-MM-DD") {
            @Override
            public boolean matches(final Object o) {
                return (forventetAr + "-" + forventetManed + "-" + forventetDag).equals(o);
            }
        };
    }
}