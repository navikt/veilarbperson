package no.nav.veilarbperson.utils.mappers;

import no.nav.common.types.identer.Fnr;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import no.nav.veilarbperson.client.person.domain.TpsPerson;
import no.nav.veilarbperson.utils.PersonDataMapper;
import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;

import javax.xml.datatype.XMLGregorianCalendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Ja, her var det en del tester! Grunnen til at mapperen testes opp og ned og i mente er at det er en del annen kode
 * som potensielt brekker i to dersom mapperen endrer seg. Derfor må vi ha kontroll på når det skjer. Mapperen bør
 * også være robust nok til å håndtere at WS-objektene kan inneholde forskjellige kombinasjoner av data uten at
 * alt går galt. Dette testes også her.
 */

public class PersonDataMapperTest {
    
    @Test
    public void fornavnMappesDersomDetEksisterer() {
        final String forventetVerdi = "fornavn";
        final Person wsPerson = new Person().withPersonnavn(new Personnavn().withFornavn(forventetVerdi));
        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getFornavn(), is(forventetVerdi));
    }

    @Test
    public void fornavnMappesTilNullDersomWSPersonnavnErNull() {
        final Person wsPerson = new Person().withPersonnavn(null);
        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getFornavn(), nullValue());
    }

    @Test
    public void fornavnMappesTilNullDersomPersonnavnErNull() {
        final Person wsPerson = new Person().withPersonnavn(new Personnavn().withFornavn(null));
        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);
        
        assertThat(tpsPerson.getFornavn(), nullValue());
    }

    @Test
    public void etternavnMappesDersomDetEksisterer() {
        final String forventetVerdi = "etternavn";
        final Person wsPerson = new Person().withPersonnavn(new Personnavn().withEtternavn(forventetVerdi));
        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);
        
        assertThat(tpsPerson.getEtternavn(), is(forventetVerdi));
    }

    @Test
    public void etternavnMappesTilNullDersomWSPersonnavnErNull() {
        final Person wsPerson = new Person().withPersonnavn(null);
        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getEtternavn(), nullValue());
    }

    @Test
    public void etternavnMappesTilNullDersomEtternavnErNull() {
        final Person wsPerson = new Person().withPersonnavn(new Personnavn().withEtternavn(null));
        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getEtternavn(), nullValue());
    }

    @Test
    public void mellomnavnMappesDersomDetEksisterer() {
        final String forventetVerdi = "mellomnavn";
        final Person wsPerson = new Person().withPersonnavn(new Personnavn().withMellomnavn(forventetVerdi));
        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getMellomnavn(), is(forventetVerdi));
    }

    @Test
    public void mellomnavnMappesTilNullDersomWSPersonnavnErNull() {
        final Person wsPerson = new Person().withPersonnavn(null);
        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getMellomnavn(), nullValue());
    }

    @Test
    public void mellomnavnMappesTilNullDersomMellomnavnErNull() {
        final Person wsPerson = new Person().withPersonnavn(new Personnavn().withMellomnavn(null));
        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getMellomnavn(), nullValue());
    }

    @Test
    public void sammensattNavnMappesDersomDetEksisterer() {
        final String forventetVerdi = "sammensattNavn";
        final Person wsPerson = new Person().withPersonnavn(new Personnavn().withSammensattNavn(forventetVerdi));

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getSammensattNavn(), is(forventetVerdi));
    }

    @Test
    public void sammensattNavnMappesTilNullDersomWSPersonnavnErNull() {
        final Person wsPerson = new Person().withPersonnavn(null);

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getSammensattNavn(), nullValue());
    }

    @Test
    public void sammensattNavnMappesTilNullDersomMellomnavnErNull() {
        final Person wsPerson = new Person().withPersonnavn(new Personnavn().withSammensattNavn(null));

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getSammensattNavn(), nullValue());
    }

    @Test
    public void fodselsnummerMappesDersomDetEksisterer() {
        final Fnr forventetVerdi = Fnr.of("fodseslnummer");
        final Person wsPerson = new Person().withAktoer(new PersonIdent().withIdent(new NorskIdent().withIdent(forventetVerdi.get())));

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getFodselsnummer(), is(forventetVerdi));
    }

    @Test
    public void fodselsnummerMappesTilNullDersomWSAktoerErNull() {
        final Person wsPerson = new Person();

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getFodselsnummer(), nullValue());
    }

    @Test
    public void fodselsnummerMappesTilNullDersomWSNorskIdentErNull() {
        final Person wsPerson = new Person().withAktoer(new PersonIdent());

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getFodselsnummer(), nullValue());
    }

    @Test
    public void fodselsnummerMappesTilNullDersomIdentErNull() {
        final Person wsPerson = new Person().withAktoer(new PersonIdent().withIdent(new NorskIdent().withIdent(null)));

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getFodselsnummer(), nullValue());
    }

    @Test
    public void fodselsdatoMappesDersomDenEksisterer() throws Exception {
        final int forventetAr = 1999;
        final int forventetManed = 12;
        final int forventetDag = 13;
        final XMLGregorianCalendar foedselsdato = MapperTestUtils.lagDato(forventetAr, forventetManed, forventetDag);
        final Person wsPerson = new Person().withFoedselsdato(new Foedselsdato().withFoedselsdato(foedselsdato));

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getFodselsdato(), erDato(forventetAr, forventetManed, forventetDag));
    }

    @Test
    public void fodselsdatoMappesTilNullDersomWSFoedselsdatoErNull() {
        final Person wsPerson = new Person().withFoedselsdato(null);

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getFodselsdato(), nullValue());
    }

    @Test
    public void fodselsdatoMappesTilNullDersomFoedselsdatoErNull() {
        final Person wsPerson = new Person().withFoedselsdato(null);

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getFodselsdato(), nullValue());
    }

    @Test
    public void kjonnMappesDersomDetEksisterer() {
        final String forventetVerdi = "K";
        final Person wsPerson = new Person().withKjoenn(new Kjoenn().withKjoenn(new Kjoennstyper().withValue(forventetVerdi)));

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getKjonn(), is(forventetVerdi));
    }

    @Test
    public void kjonnMappesTilNullDersomWSKjoennErNull() {
        final Person wsPerson = new Person().withKjoenn(null);

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getKjonn(), nullValue());
    }

    @Test
    public void kjonnMappesTilNullDersomWSKjoennstyperErNull() {
        final Person wsPerson = new Person().withKjoenn(new Kjoenn().withKjoenn(null));

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getKjonn(), nullValue());
    }

    @Test
    public void kjonnMappesTilNullDersomValueErNull() {
        final Person wsPerson = new Person().withKjoenn(new Kjoenn().withKjoenn(new Kjoennstyper().withValue(null)));

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getKjonn(), nullValue());
    }

    @Test
    public void kode6MappesDersomDetEksisterer() {
        final String forventetVerdi = "6";
        final Person wsPerson = new Person().withDiskresjonskode(new Diskresjonskoder()
                .withValue(no.nav.veilarbperson.client.person.domain.Diskresjonskoder.STRENGT_FORTROLIG_ADRESSE.kodeverkVerdi));

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getDiskresjonskode(), is(forventetVerdi));
    }

    @Test
    public void kode7MappesDersomDetEksisterer() {
        final String forventetVerdi = "7";
        final Person wsPerson = new Person().withDiskresjonskode(new Diskresjonskoder()
                .withValue(no.nav.veilarbperson.client.person.domain.Diskresjonskoder.FORTROLIG_ADRESSE.kodeverkVerdi));

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getDiskresjonskode(), is(forventetVerdi));
    }

    @Test
    public void diskresjonskodeMappesTilNullDersomWSDiskresjonskoderErNull() {
        final Person wsPerson = new Person().withDiskresjonskode(null);

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getDiskresjonskode(), nullValue());
    }

    @Test
    public void diskresjonskodeMappesTilNullDersomValueErNull() {
        final Person wsPerson = new Person().withDiskresjonskode(new Diskresjonskoder().withValue(null));

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getDiskresjonskode(), nullValue());
    }

    @Test
    public void kontonummerMappesDersomBankkontoErNorge() {
        final String forventetVerdi = "123456789";

        final Person wsPerson = new Bruker().withBankkonto(
                new BankkontoNorge().withBankkonto(new Bankkontonummer().withBankkontonummer(forventetVerdi))
        );

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getKontonummer(), is(forventetVerdi));
    }

    @Test
    public void kontonummerMappesDersomBankkontoErUtland() {
        final String forventetVerdi = "987654321";
        final Person wsPerson = new Bruker().withBankkonto(
                new BankkontoUtland().withBankkontoUtland(new BankkontonummerUtland().withBankkontonummer(forventetVerdi))
        );

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getKontonummer(), is(forventetVerdi));
    }

    @Test
    public void kontonummerMappesTilNullDersomWSBankkontoErNull() {
        final Person wsPerson = new Bruker().withBankkonto(null);

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getKontonummer(), nullValue());
    }

    @Test
    public void kontonummerMappesTilNullDersomWSBankkontonummerErNull() {
        final Person wsPerson = new Bruker().withBankkonto(
                new BankkontoNorge().withBankkonto(null)
        );

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getKontonummer(), nullValue());
    }

    @Test
    public void kontonummerMappesTilNullDersomWSBankkontonummerVerdiErNull() {
        final Person wsPerson = new Bruker().withBankkonto(
                new BankkontoNorge().withBankkonto(new Bankkontonummer().withBankkontonummer(null))
        );

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getKontonummer(), nullValue());
    }

    @Test
    public void kontonummerMappesTilNullDersomWSBankkontonummerUtlandErNull() {
        final Person wsPerson = new Bruker().withBankkonto(
                new BankkontoUtland().withBankkontoUtland(null)
        );

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getKontonummer(), nullValue());
    }

    @Test
    public void kontonummerMappesTilNullDersomWSBankkontonummerUtlandVerdiErNull() {
        final Person wsPerson = new Bruker().withBankkonto(
                new BankkontoUtland().withBankkontoUtland(new BankkontonummerUtland().withBankkontonummer(null))
        );

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getKontonummer(), nullValue());
    }

    @Test
    public void geografiskTilknytningKommuneMappesDersomDetEksisterer() {
        final String forventetVerdi = "2890";
        final Person wsPerson = new Bruker().withGeografiskTilknytning(
                new Kommune().withGeografiskTilknytning(forventetVerdi));

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getGeografiskTilknytning(), is(forventetVerdi));
    }

    @Test
    public void geografiskTilknytningLandMappesDersomDetEksisterer() {
        final String forventetVerdi = "SWE";
        final Person wsPerson = new Bruker().withGeografiskTilknytning(
                new Land().withGeografiskTilknytning(forventetVerdi));

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getGeografiskTilknytning(), is(forventetVerdi));
    }

    @Test
    public void geografiskTilknytningBydelMappesDersomDetEksisterer() {
        final String forventetVerdi = "289033";
        final Person wsPerson = new Bruker().withGeografiskTilknytning(
                new Bydel().withGeografiskTilknytning(forventetVerdi));

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getGeografiskTilknytning(), is(forventetVerdi));
    }

    @Test
    public void geografiskTilknytningMappesTilNullDersomInputIkkeErWSBruker() {
        final Person wsPerson = new Person();

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getGeografiskTilknytning(), nullValue());
    }

    @Test
    public void geografiskTilknytningMappesTilNullDersomWSAnsvarligEnhetErNull() {
        final Person wsPerson = new Bruker().withGeografiskTilknytning(null);

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getGeografiskTilknytning(), nullValue());
    }

    @Test
    public void geografiskTilknytningMappesTilNullDersomGeografiskTilknytning() {
        final Person wsPerson = new Bruker().withGeografiskTilknytning(new Bydel().withGeografiskTilknytning(null));

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getGeografiskTilknytning(), nullValue());
    }

    @Test
    public void statsborgerskapMappesDersomDetEksisterer() {
        final String forventetVerdi = "NORGE";
        final Person wsPerson = new Person().withStatsborgerskap(
                new Statsborgerskap().withLand(new Landkoder().withValue(forventetVerdi))
        );

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getStatsborgerskap(), is(forventetVerdi));
    }

    @Test
    public void statsborgerskapMappesTilNullDersomWSStatsborgerskapErNull() {
        final Person wsPerson = new Person().withStatsborgerskap(null);

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getStatsborgerskap(), nullValue());
    }

    @Test
    public void statsborgerskapMappesTilNullDersomLandErNull() {
        final Person wsPerson = new Person().withStatsborgerskap(
                new Statsborgerskap().withLand(null)
        );

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getStatsborgerskap(), nullValue());
    }

    @Test
    public void statsborgerskapMappesTilNullDersomValueErNull() {
        final Person wsPerson = new Person().withStatsborgerskap(
                new Statsborgerskap().withLand(new Landkoder().withValue(null))
        );

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getStatsborgerskap(), nullValue());
    }

    @Test
    public void siviltilstandMappesDersomDetEksisterer() throws Exception {
        final String forventetSiviltilstand = "GIFT";
        final int forventetAr = 1999;
        final int forventetManed = 12;
        final int forventetDag = 13;
        final Person wsPerson = new Person().withSivilstand(
                new Sivilstand().withFomGyldighetsperiode(MapperTestUtils.lagDato(forventetAr, forventetManed, forventetDag))
                        .withSivilstand(new Sivilstander().withValue(forventetSiviltilstand))
        );

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getSivilstand(), notNullValue());
        assertThat(tpsPerson.getSivilstand().getSivilstand(), is(forventetSiviltilstand));
        assertThat(tpsPerson.getSivilstand().getFraDato(), erDato(forventetAr, forventetManed, forventetDag));
    }

    @Test
    public void siviltilstandMappesTilNullDersomWSSivilstandErNull() {
        final Person wsPerson = new Person().withSivilstand(null);

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getSivilstand(), nullValue());
    }

    @Test
    public void dodsdatoMappesDersomDetEksisterer() throws Exception {
        final int forventetAr = 1999;
        final int forventetManed = 12;
        final int forventetDag = 13;
        final Person wsPerson = new Person().withDoedsdato(
                new Doedsdato().withDoedsdato(MapperTestUtils.lagDato(forventetAr, forventetManed, forventetDag))
        );

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getDodsdato(), erDato(forventetAr, forventetManed, forventetDag));
    }

    @Test
    public void dodsdatoMappesTilNullDersomWSDoedsdatoErNull() {
        final Person wsPerson = new Person().withDoedsdato(null);

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getDodsdato(), nullValue());
    }

    @Test
    public void dodsdatoMappesTilNullDersomDoedsdatoErNull() {
        final Person wsPerson = new Person().withDoedsdato(new Doedsdato().withDoedsdato(null));

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getDodsdato(), nullValue());
    }

    @Test
    public void verdierIWSGateadresseMappesDersomDeEksisterer() {
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

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getBostedsadresse(), notNullValue());
        assertThat(tpsPerson.getBostedsadresse().getStrukturertAdresse(), notNullValue());
        assertThat(tpsPerson.getBostedsadresse().getStrukturertAdresse(), instanceOf(no.nav.veilarbperson.client.person.domain.Gateadresse.class));
        final no.nav.veilarbperson.client.person.domain.Gateadresse gateadresse = (no.nav.veilarbperson.client.person.domain.Gateadresse) tpsPerson.getBostedsadresse().getStrukturertAdresse();

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
    public void verdierIWSGateadresseMappesTilNullDersomDeErNull() {
        final Person wsPerson = new Person().withBostedsadresse(
                new Bostedsadresse().withStrukturertAdresse(
                        lagGateadresse(null, 0, 0, null, null, new Postnummer())
                )
        );

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getBostedsadresse(), notNullValue());
        assertThat(tpsPerson.getBostedsadresse().getStrukturertAdresse(), notNullValue());
        assertThat(tpsPerson.getBostedsadresse().getStrukturertAdresse(), instanceOf(no.nav.veilarbperson.client.person.domain.Gateadresse.class));
        final no.nav.veilarbperson.client.person.domain.Gateadresse gateadresse = (no.nav.veilarbperson.client.person.domain.Gateadresse) tpsPerson.getBostedsadresse().getStrukturertAdresse();

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
    public void verdierIWSMatrikkeladresseMappesDersomDeEksisterer() {
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

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getBostedsadresse(), notNullValue());
        assertThat(tpsPerson.getBostedsadresse().getStrukturertAdresse(), notNullValue());
        assertThat(tpsPerson.getBostedsadresse().getStrukturertAdresse(), instanceOf(no.nav.veilarbperson.client.person.domain.Matrikkeladresse.class));
        final no.nav.veilarbperson.client.person.domain.Matrikkeladresse matrikkeladresse = (no.nav.veilarbperson.client.person.domain.Matrikkeladresse) tpsPerson.getBostedsadresse().getStrukturertAdresse();

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
    public void verdierIWSMatrikkeladresseMappesTilNullDersomDeErNull() {
        final Person wsPerson = new Person().withBostedsadresse(
                new Bostedsadresse().withStrukturertAdresse(
                        lagMatrikkeladresse(null, null, null, null, null, null, null)
                )
        );

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getBostedsadresse(), notNullValue());
        assertThat(tpsPerson.getBostedsadresse().getStrukturertAdresse(), notNullValue());
        assertThat(tpsPerson.getBostedsadresse().getStrukturertAdresse(), instanceOf(no.nav.veilarbperson.client.person.domain.Matrikkeladresse.class));
        final no.nav.veilarbperson.client.person.domain.Matrikkeladresse matrikkeladresse = (no.nav.veilarbperson.client.person.domain.Matrikkeladresse) tpsPerson.getBostedsadresse().getStrukturertAdresse();
        sjekkAtMatrikkeladresseHarForventaVerdier(matrikkeladresse, null, null, null, null, null, null, null);
    }

    @Test
    public void verdierIWSPostadresseNorskMappesDersomDeEksisterer() {
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

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getBostedsadresse(), notNullValue());
        assertThat(tpsPerson.getBostedsadresse().getStrukturertAdresse(), notNullValue());
        assertThat(tpsPerson.getBostedsadresse().getStrukturertAdresse(), instanceOf(no.nav.veilarbperson.client.person.domain.PostboksadresseNorsk.class));
        final no.nav.veilarbperson.client.person.domain.PostboksadresseNorsk postboksadresseNorsk = (no.nav.veilarbperson.client.person.domain.PostboksadresseNorsk) tpsPerson.getBostedsadresse().getStrukturertAdresse();
        assertThat(postboksadresseNorsk.getPostboksanlegg(), is(forventetPostboksanlegg));
        assertThat(postboksadresseNorsk.getPostboksnummer(), is(forventetPostboksnummer));
        assertThat(postboksadresseNorsk.getPostnummer(), is(forventetPostnummer));
    }

    @Test
    public void verdierIWSPostadresseNorskTilNullDersomDeErNull() {
        final Person wsPerson = new Person().withBostedsadresse(new Bostedsadresse().withStrukturertAdresse(
                new no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk()
                        .withPostboksanlegg(null)
                        .withPostboksnummer(null)
                        .withPoststed(new Postnummer().withValue(null))
                )
        );

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getBostedsadresse(), notNullValue());
        assertThat(tpsPerson.getBostedsadresse().getStrukturertAdresse(), notNullValue());
        assertThat(tpsPerson.getBostedsadresse().getStrukturertAdresse(), instanceOf(no.nav.veilarbperson.client.person.domain.PostboksadresseNorsk.class));
        final no.nav.veilarbperson.client.person.domain.PostboksadresseNorsk postboksadresseNorsk = (no.nav.veilarbperson.client.person.domain.PostboksadresseNorsk) tpsPerson.getBostedsadresse().getStrukturertAdresse();
        assertThat(postboksadresseNorsk.getPostboksanlegg(), nullValue());
        assertThat(postboksadresseNorsk.getPostboksnummer(), nullValue());
        assertThat(postboksadresseNorsk.getPostnummer(), nullValue());
    }

    @Test
    public void wsStrukturertAdresseLandkodeMappesDersomDenEksisterer() {
        final String forventetLandkode = "NO";
        final Person wsPerson = new Person().withBostedsadresse(new Bostedsadresse().withStrukturertAdresse(
                new no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk()
                        .withPoststed(new Postnummer())
                        .withLandkode(new Landkoder().withValue(forventetLandkode)))
        );

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getBostedsadresse(), notNullValue());
        assertThat(tpsPerson.getBostedsadresse().getStrukturertAdresse(), notNullValue());
        assertThat(tpsPerson.getBostedsadresse().getStrukturertAdresse().getLandkode(), is(forventetLandkode));
    }

    @Test
    public void wsStrukturertAdresseLandkodeMappesTilNullDersomDenErNull() {
        final Person wsPerson = new Person().withBostedsadresse(new Bostedsadresse().withStrukturertAdresse(
                new no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk()
                        .withPoststed(new Postnummer())
                        .withLandkode(null))
        );

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getBostedsadresse(), notNullValue());
        assertThat(tpsPerson.getBostedsadresse().getStrukturertAdresse(), notNullValue());
        assertThat(tpsPerson.getBostedsadresse().getStrukturertAdresse().getLandkode(), nullValue());
    }

    @Test
    public void wsStrukturertAdresseTilleggsadresseMappesDersomDenEksisterer() {
        final String forventetTilleggsadresse = "C/O tilleggsadresse";
        final Person wsPerson = new Person().withBostedsadresse(new Bostedsadresse().withStrukturertAdresse(
                new no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk()
                        .withPoststed(new Postnummer())
                        .withTilleggsadresse(forventetTilleggsadresse)));

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);
        assertThat(tpsPerson.getBostedsadresse(), notNullValue());
        assertThat(tpsPerson.getBostedsadresse().getStrukturertAdresse(), notNullValue());
        assertThat(tpsPerson.getBostedsadresse().getStrukturertAdresse().getTilleggsadresse(), is(forventetTilleggsadresse));
    }

    @Test
    public void wsStrukturertAdresseTilleggsadresseMappesTilNullDersomDenErNull() {
        final Person wsPerson = new Person().withBostedsadresse(new Bostedsadresse().withStrukturertAdresse(
                new no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk()
                        .withPoststed(new Postnummer())
                        .withTilleggsadresse(null)));

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);
        assertThat(tpsPerson.getBostedsadresse(), notNullValue());
        assertThat(tpsPerson.getBostedsadresse().getStrukturertAdresse(), notNullValue());
        assertThat(tpsPerson.getBostedsadresse().getStrukturertAdresse().getTilleggsadresse(), nullValue());
    }

    @Test
    public void wsStrukturertAdresseLandkodeMappesTilNullDersomValueDenErNull() {
        final Person wsPerson = new Person().withBostedsadresse(new Bostedsadresse().withStrukturertAdresse(
                new no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk()
                        .withPoststed(new Postnummer())
                        .withLandkode(new Landkoder().withValue(null)))
        );

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getBostedsadresse(), notNullValue());
        assertThat(tpsPerson.getBostedsadresse().getStrukturertAdresse(), notNullValue());
        assertThat(tpsPerson.getBostedsadresse().getStrukturertAdresse().getLandkode(), nullValue());
    }

    @Test
    public void wsMidlertidigPostadresseNorgeMappesDersomDenEksisterer() {
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

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getMidlertidigAdresseNorge(), notNullValue());
        assertThat(tpsPerson.getMidlertidigAdresseNorge().getStrukturertAdresse(), notNullValue());
        assertThat(tpsPerson.getMidlertidigAdresseNorge().getStrukturertAdresse(), instanceOf(no.nav.veilarbperson.client.person.domain.Gateadresse.class));
        final no.nav.veilarbperson.client.person.domain.Gateadresse gateadresse = (no.nav.veilarbperson.client.person.domain.Gateadresse) tpsPerson.getMidlertidigAdresseNorge().getStrukturertAdresse();
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
    public void wsMidlertidigPostadresseNorgeMappesTilNullDersomDenErNull() {
        final Person wsPerson = new Bruker().withMidlertidigPostadresse(
                new MidlertidigPostadresseNorge().withStrukturertAdresse(
                        lagGateadresse(null, 0, 0, null, null, new Postnummer())
                )
        );

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getMidlertidigAdresseNorge(), notNullValue());
        assertThat(tpsPerson.getMidlertidigAdresseNorge().getStrukturertAdresse(), notNullValue());
        assertThat(tpsPerson.getMidlertidigAdresseNorge().getStrukturertAdresse(), instanceOf(no.nav.veilarbperson.client.person.domain.Gateadresse.class));
        final no.nav.veilarbperson.client.person.domain.Gateadresse gateadresse = (no.nav.veilarbperson.client.person.domain.Gateadresse) tpsPerson.getMidlertidigAdresseNorge().getStrukturertAdresse();
        sjekkAtGateadresseHarForventaVerdier(gateadresse, null, 0, 0, null, null, null);
    }

    @Test
    public void wsMidlertidigPostadresseNorgeMappesTilNullDersomPersonIkkeErBruker() {
        final Person wsPerson = new Person().withBostedsadresse(
                new Bostedsadresse().withStrukturertAdresse(
                        lagGateadresse("gatenavn", 0, 0, "Husbokstav", "Kommunenummer", new Postnummer().withValue("Poststed"))
                )
        );

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);
        assertThat(tpsPerson.getMidlertidigAdresseNorge(), nullValue());
    }

    @Test
    public void wsMidlertidigPostadresseUtlandMappesDersomDenEksisterer() {
        String forventetAdresselinje1 = "Adresselinje1";
        String forventetAdresselinje2 = "Adresselinje2";
        String forventetAdresselinje3 = "Adresselinje3";
        String forventetAdresselinje4 = "Adresselinje4";
        String forventetLandkode = "LAND";
        final Person wsPerson = new Bruker().withMidlertidigPostadresse(
                new MidlertidigPostadresseUtland().withUstrukturertAdresse(
                        lagUstrukturertAdresse(forventetAdresselinje1, forventetAdresselinje2, forventetAdresselinje3, forventetAdresselinje4, forventetLandkode)
                ));

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);
        assertThat(tpsPerson.getMidlertidigAdresseUtland(), notNullValue());
        no.nav.veilarbperson.client.person.domain.UstrukturertAdresse ustrukturertAdresse = tpsPerson.getMidlertidigAdresseUtland().getUstrukturertAdresse();
        sjekkAtUstrukturertAdresseHarForventaVerdier(ustrukturertAdresse, forventetAdresselinje1, forventetAdresselinje2, forventetAdresselinje3, forventetAdresselinje4, forventetLandkode);
    }

    @Test
    public void wsMidlertidigPostadresseUtlandMappesTilNullDersomDenErNull(){
        final Person wsPerson = new Bruker().withMidlertidigPostadresse(
                new MidlertidigPostadresseUtland().withUstrukturertAdresse(
                        lagUstrukturertAdresse(null, null, null, null, null)
                ));

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);
        assertThat(tpsPerson.getMidlertidigAdresseUtland(), notNullValue());
        no.nav.veilarbperson.client.person.domain.UstrukturertAdresse ustrukturertAdresse = tpsPerson.getMidlertidigAdresseUtland().getUstrukturertAdresse();
        sjekkAtUstrukturertAdresseHarForventaVerdier(ustrukturertAdresse, null, null, null, null, null);
    }
    @Test
    public void wsMidlertidigPostadresseUtlandetMappesTilNullDersomPersonIkkeErBruker() {
        final Person wsPerson = new Person().withBostedsadresse(
                new Bostedsadresse().withStrukturertAdresse(
                        lagGateadresse(null, 0, 0, null, null, new Postnummer().withValue(null))
                ));

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);
        assertThat(tpsPerson.getMidlertidigAdresseUtland(), nullValue());
    }

    @Test
    public void wsPostadressSkalMappesDersomDenEksisterer() {
        String forventetAdresselinje1 = "Adresselinje1";
        String forventetAdresselinje2 = "Adresselinje2";
        String forventetAdresselinje3 = "Adresselinje3";
        String forventetAdresselinje4 = "Adresselinje4";
        String forventetLandkode = "LAND";
        final Person wsPerson = new Person().withPostadresse(
                new no.nav.tjeneste.virksomhet.person.v3.informasjon.Postadresse().withUstrukturertAdresse(
                        lagUstrukturertAdresse(forventetAdresselinje1, forventetAdresselinje2, forventetAdresselinje3, forventetAdresselinje4, forventetLandkode)
                ));


        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);
        assertThat(tpsPerson.getPostAdresse(), notNullValue());
        no.nav.veilarbperson.client.person.domain.UstrukturertAdresse ustrukturertAdresse = tpsPerson.getPostAdresse().getUstrukturertAdresse();
        sjekkAtUstrukturertAdresseHarForventaVerdier(ustrukturertAdresse, forventetAdresselinje1, forventetAdresselinje2, forventetAdresselinje3, forventetAdresselinje4, forventetLandkode);
    }

    @Test
    public void wsPostadresseSkalMappesTilNullDersomDenErNull() {
        final Person wsPerson = new Person().withPostadresse(
                new Postadresse().withUstrukturertAdresse(lagUstrukturertAdresse(null, null, null, null, null)));

        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);
        assertThat(tpsPerson.getPostAdresse(), notNullValue());
        no.nav.veilarbperson.client.person.domain.UstrukturertAdresse ustrukturertAdresse = tpsPerson.getPostAdresse().getUstrukturertAdresse();
        sjekkAtUstrukturertAdresseHarForventaVerdier(ustrukturertAdresse, null, null, null, null, null);
    }

    @Test
    public void malformMappesDersomDetEksisterer() {
        final Person wsPerson = new Bruker().withMaalform(new Spraak().withValue("NB"));
        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getMalform(), is("NB"));
    }

    @Test
    public void malformMappesTilNullDersommalformErNull() {
        final Person wsPerson = new Bruker().withMaalform(null);
        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getMalform(), nullValue());
    }

    @Test
    public void malformMappesTilNullDersommalformVerdiErNull() {
        final Person wsPerson = new Bruker().withMaalform(new Spraak().withValue(null));
        final TpsPerson tpsPerson = PersonDataMapper.tilTpsPerson(wsPerson);

        assertThat(tpsPerson.getMalform(), nullValue());
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
            no.nav.veilarbperson.client.person.domain.UstrukturertAdresse ustrukturertAdresse,
            String forventaAdresselinje1,
            String forventaAdresselinje2,
            String forventaAdresselinje3,
            String forventaAdresselinje4,
            String forventaLandkode) {

        assertThat(ustrukturertAdresse, notNullValue());
        assertThat(ustrukturertAdresse, instanceOf(no.nav.veilarbperson.client.person.domain.UstrukturertAdresse.class));
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
            no.nav.veilarbperson.client.person.domain.Gateadresse gateadresse,
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
            no.nav.veilarbperson.client.person.domain.Matrikkeladresse matrikkeladresse,
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
