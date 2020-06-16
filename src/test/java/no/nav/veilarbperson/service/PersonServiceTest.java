package no.nav.veilarbperson.service;

import no.nav.common.client.norg2.Enhet;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.veilarbperson.utils.TestUtil;
import no.nav.veilarbperson.client.*;
import no.nav.veilarbperson.domain.DkifKontaktinfo;
import no.nav.veilarbperson.domain.person.PersonData;
import no.nav.veilarbperson.domain.person.Sikkerhetstiltak;
import no.nav.veilarbperson.domain.person.Sivilstand;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.feil.PersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.feil.Sikkerhetsbegrensning;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PersonServiceTest {

    private Norg2Client norg2Client = mock(Norg2Client.class);

    private DkifClient dkifClient = mock(DkifClient.class);

    private PersonClient personClient = mock(PersonClient.class);

    private EgenAnsattClient egenAnsattClient = mock(EgenAnsattClient.class);

    private KodeverkClient kodeverkClient = mock(KodeverkClient.class);

    private VeilarbportefoljeClient veilarbportefoljeClient = mock(VeilarbportefoljeClient.class);

    private PersonService personService;

    @Before
    public void setup() {
        when(norg2Client.hentTilhorendeEnhet(anyString())).thenReturn(new Enhet());
        when(dkifClient.hentKontaktInfo(anyString())).thenReturn(new DkifKontaktinfo());
        when(personClient.hentPersonData(anyString())).thenReturn(lagPersonData());
        when(personClient.hentSikkerhetstiltak(anyString())).thenReturn(new Sikkerhetstiltak(null));
        when(egenAnsattClient.erEgenAnsatt(anyString())).thenReturn(true);

        personService = new PersonService(norg2Client, personClient, egenAnsattClient, dkifClient, kodeverkClient, veilarbportefoljeClient);
    }

    @Test
    public void hentPersonSkalReturnereEnPerson() {
        final PersonData personData = personService.hentPerson("");
        assertThat(personData, notNullValue());
    }

    @Test
    public void personSkalInneholdeGeografiskTilknytningDersomDetEksisterer() {
        final PersonData forventetPersonData = lagPersonData();
        final String geografiskTilknytning = "1234";
        forventetPersonData.setGeografiskTilknytning(geografiskTilknytning);
        when(personClient.hentPersonData(anyString())).thenReturn(forventetPersonData);

        final PersonData returnertPersonData = personService.hentPerson("");

        assertThat(returnertPersonData.getGeografiskTilknytning(), is(geografiskTilknytning));
    }

    @Test
    public void personSkalInneholdeSivilstandDersomDetEksisterer() {
        final PersonData forventetPersonData = lagPersonData();
        final String kodeverksverdi = "GIFT";
        forventetPersonData.setSivilstand(new Sivilstand().withSivilstand(kodeverksverdi));
        when(personClient.hentPersonData(anyString())).thenReturn(forventetPersonData);
        when(kodeverkClient.getBeskrivelseForSivilstand(anyString())).thenReturn(kodeverksverdi);

        final PersonData returnertPersonData = personService.hentPerson("");

        assertThat(returnertPersonData.getSivilstand(), notNullValue());
        assertThat(returnertPersonData.getSivilstand().getSivilstand(), is(kodeverksverdi));
    }

    private PersonData lagPersonData() {
        final PersonData personData = new PersonData();
        personData.setSivilstand(new Sivilstand());
        personData.setGeografiskTilknytning("");
        return personData;
    }
}