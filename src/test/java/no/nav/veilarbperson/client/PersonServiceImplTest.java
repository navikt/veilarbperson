package no.nav.veilarbperson.client;

import no.nav.veilarbperson.domain.person.PersonData;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentSikkerhetstiltakPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.feil.PersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.feil.Sikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentSikkerhetstiltakRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentSikkerhetstiltakResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import no.nav.veilarbperson.utils.TestUtil;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PersonServiceImplTest {

    private static final String SIKKHERHETSTILTAK = "FARLIG";
    private static String IDENT = TestUtil.fodselsnummerForDato("1980-01-01");

    private PersonV3 personV3 = mock(PersonV3.class);
    private PersonClient personClient;

    @Before
    public void before() {
        personClient = new PersonClientImpl(personV3, null);
    }

    @Test
    public void hentPersonHenterPerson() throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        HentPersonResponse hentPersonResponse = new HentPersonResponse()
                .withPerson(new Person().withAktoer(new PersonIdent().withIdent(new NorskIdent().withIdent(IDENT))));
        when(personV3.hentPerson(any(HentPersonRequest.class))).thenReturn(hentPersonResponse);

        PersonData personData = personClient.hentPersonData(IDENT);

        assertThat(personData.getFodselsnummer(), is(equalTo(IDENT)));
    }

    @Test
    public void hentPersonBerOmRelevantInformasjonFraTjenesten() throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        HentPersonResponse hentPersonResponse = new HentPersonResponse()
                .withPerson(new Person().withAktoer(new PersonIdent().withIdent(new NorskIdent().withIdent(IDENT))));
        ArgumentCaptor<HentPersonRequest> argumentCaptor = ArgumentCaptor.forClass(HentPersonRequest.class);
        when(personV3.hentPerson(argumentCaptor.capture())).thenReturn(hentPersonResponse);

        personClient.hentPersonData(IDENT);

        List<Informasjonsbehov> informasjonsBehov = argumentCaptor.getValue().getInformasjonsbehov();
        assertThat(informasjonsBehov.contains(Informasjonsbehov.ADRESSE), is(true));
        assertThat(informasjonsBehov.contains(Informasjonsbehov.KOMMUNIKASJON), is(true));
        assertThat(informasjonsBehov.contains(Informasjonsbehov.FAMILIERELASJONER), is(true));
        assertThat(informasjonsBehov.contains(Informasjonsbehov.BANKKONTO), is(true));
        assertThat(informasjonsBehov.contains(Informasjonsbehov.ADRESSE), is(true));
    }

    @Test
    public void hentSikkerhetstiltakHenterSikkerhetstiltak() throws HentSikkerhetstiltakPersonIkkeFunnet {
        Sikkerhetstiltak wsSikkerhetstiltak = new Sikkerhetstiltak().withSikkerhetstiltaksbeskrivelse(SIKKHERHETSTILTAK);
        HentSikkerhetstiltakResponse response = new HentSikkerhetstiltakResponse().withSikkerhetstiltak(wsSikkerhetstiltak);
        when(personV3.hentSikkerhetstiltak(any(HentSikkerhetstiltakRequest.class))).thenReturn(response);

        no.nav.veilarbperson.domain.person.Sikkerhetstiltak sikkerhetstiltak = personClient.hentSikkerhetstiltak(IDENT);

        assertThat(sikkerhetstiltak.sikkerhetstiltaksbeskrivelse, is(equalTo(SIKKHERHETSTILTAK)));
    }

    @Test
    public void hentSikkerhetstiltakDersomIngenSikkerhetstiltak() throws HentSikkerhetstiltakPersonIkkeFunnet {
        HentSikkerhetstiltakResponse response = new HentSikkerhetstiltakResponse();
        when(personV3.hentSikkerhetstiltak(any(HentSikkerhetstiltakRequest.class))).thenReturn(response);

        no.nav.veilarbperson.domain.person.Sikkerhetstiltak sikkerhetstiltak = personClient.hentSikkerhetstiltak(IDENT);

        assertThat(sikkerhetstiltak.sikkerhetstiltaksbeskrivelse, is(nullValue()));
    }

}