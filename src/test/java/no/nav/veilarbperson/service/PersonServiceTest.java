package no.nav.veilarbperson.service;

import no.nav.common.client.norg2.Enhet;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.veilarbperson.client.dkif.DkifClient;
import no.nav.veilarbperson.client.dkif.DkifKontaktinfo;
import no.nav.veilarbperson.client.egenansatt.EgenAnsattClient;
import no.nav.veilarbperson.client.person.PersonClient;
import no.nav.veilarbperson.client.person.domain.Sivilstand;
import no.nav.veilarbperson.client.person.domain.TpsPerson;
import no.nav.veilarbperson.client.veilarbportefolje.VeilarbportefoljeClient;
import no.nav.veilarbperson.domain.PersonData;
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

    private KodeverkService kodeverkService = mock(KodeverkService.class);

    private VeilarbportefoljeClient veilarbportefoljeClient = mock(VeilarbportefoljeClient.class);

    private PersonService personService;

    @Before
    public void setup() {
        when(norg2Client.hentTilhorendeEnhet(anyString())).thenReturn(new Enhet());
        when(dkifClient.hentKontaktInfo(anyString())).thenReturn(new DkifKontaktinfo());
        when(personClient.hentPerson(anyString())).thenReturn(lagPerson());
        when(personClient.hentSikkerhetstiltak(anyString())).thenReturn(null);
        when(egenAnsattClient.erEgenAnsatt(anyString())).thenReturn(true);

        personService = new PersonService(norg2Client, personClient, egenAnsattClient, dkifClient, kodeverkService, veilarbportefoljeClient);
    }

    @Test
    public void hentPersonSkalReturnereEnPerson() {
        final PersonData personData = personService.hentFlettetPerson("");
        assertThat(personData, notNullValue());
    }

    @Test
    public void personSkalInneholdeGeografiskTilknytningDersomDetEksisterer() {
        final TpsPerson forventetPerson = lagPerson();
        final String geografiskTilknytning = "1234";
        forventetPerson.setGeografiskTilknytning(geografiskTilknytning);
        when(personClient.hentPerson(anyString())).thenReturn(forventetPerson);

        final PersonData returnertPersonData = personService.hentFlettetPerson("");

        assertThat(returnertPersonData.getGeografiskTilknytning(), is(geografiskTilknytning));
    }

    @Test
    public void personSkalInneholdeSivilstandDersomDetEksisterer() {
        final TpsPerson forventetPerson = lagPerson();
        final String kodeverksverdi = "GIFT";
        forventetPerson.setSivilstand(new Sivilstand().withSivilstand(kodeverksverdi));
        when(personClient.hentPerson(anyString())).thenReturn(forventetPerson);
        when(kodeverkService.getBeskrivelseForSivilstand(anyString())).thenReturn(kodeverksverdi);

        final PersonData returnertPersonData = personService.hentFlettetPerson("");

        assertThat(returnertPersonData.getSivilstand(), notNullValue());
        assertThat(returnertPersonData.getSivilstand().getSivilstand(), is(kodeverksverdi));
    }

    private TpsPerson lagPerson() {
        final TpsPerson person = new TpsPerson();
        person.setSivilstand(new Sivilstand());
        person.setGeografiskTilknytning("");
        return person;
    }
}