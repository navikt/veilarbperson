package no.nav.veilarbperson.service;

import no.nav.common.client.norg2.Enhet;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.featuretoggle.UnleashClient;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.difi.DifiCient;
import no.nav.veilarbperson.client.difi.DifiClientImpl;
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
import org.mockito.ArgumentMatchers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PersonServiceTest {

    private Norg2Client norg2Client = mock(Norg2Client.class);

    private DkifClient dkifClient = mock(DkifClient.class);

    private PersonClient personClient = mock(PersonClient.class);

    private EgenAnsattClient egenAnsattClient = mock(EgenAnsattClient.class);

    private KodeverkService kodeverkService = mock(KodeverkService.class);

    private VeilarbportefoljeClient veilarbportefoljeClient = mock(VeilarbportefoljeClient.class);

    private DifiCient difiCient = mock(DifiClientImpl.class);

    private final UnleashClient unleashClient = mock(UnleashClient.class);

    private PersonService personService;

    @Before
    public void setup() {
        when(norg2Client.hentTilhorendeEnhet(anyString())).thenReturn(new Enhet());
        when(dkifClient.hentKontaktInfo(any())).thenReturn(new DkifKontaktinfo());
        when(personClient.hentPerson(any())).thenReturn(lagPerson());
        when(personClient.hentSikkerhetstiltak(any())).thenReturn(null);
        when(egenAnsattClient.erEgenAnsatt(any())).thenReturn(true);

        personService = new PersonService(norg2Client, personClient, egenAnsattClient, dkifClient, kodeverkService, veilarbportefoljeClient, difiCient, unleashClient);
    }

    @Test
    public void hentPersonSkalReturnereEnPerson() {
        final PersonData personData = personService.hentFlettetPerson(Fnr.of(""));
        assertThat(personData, notNullValue());
    }

    @Test
    public void personSkalInneholdeGeografiskTilknytningDersomDetEksisterer() {
        final TpsPerson forventetPerson = lagPerson();
        final String geografiskTilknytning = "1234";
        forventetPerson.setGeografiskTilknytning(geografiskTilknytning);
        when(personClient.hentPerson(any())).thenReturn(forventetPerson);

        final PersonData returnertPersonData = personService.hentFlettetPerson(Fnr.of(""));

        assertThat(returnertPersonData.getGeografiskTilknytning(), is(geografiskTilknytning));
    }

    @Test
    public void personSkalInneholdeSivilstandDersomDetEksisterer() {
        final TpsPerson forventetPerson = lagPerson();
        final String kodeverksverdi = "GIFT";
        forventetPerson.setSivilstand(new Sivilstand().setSivilstand(kodeverksverdi));
        when(personClient.hentPerson(any())).thenReturn(forventetPerson);
        when(kodeverkService.getBeskrivelseForSivilstand(anyString())).thenReturn(kodeverksverdi);

        final PersonData returnertPersonData = personService.hentFlettetPerson(Fnr.of(""));

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
