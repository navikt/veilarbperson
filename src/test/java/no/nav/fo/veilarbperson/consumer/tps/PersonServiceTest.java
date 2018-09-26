package no.nav.fo.veilarbperson.consumer.tps;

import no.nav.fo.veilarbperson.domain.person.PersonData;
import no.nav.fo.veilarbperson.domain.person.Sikkerhetstiltak;
import no.nav.tjeneste.virksomhet.person.v3.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.HentSikkerhetstiltakPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.WSHentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.WSHentPersonResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.WSHentSikkerhetstiltakRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.WSHentSikkerhetstiltakResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import no.nav.fo.veilarbperson.TestUtil;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PersonServiceTest {

    private static final String SIKKHERHETSTILTAK = "FARLIG";
    private static String IDENT = TestUtil.fodselsnummerForDato("1980-01-01");

    private PersonV3 personV3 = mock(PersonV3.class);
    private PersonService personService;

    @Before
    public void before() {
        personService = new PersonService(personV3);
    }

    @Test
    public void hentPersonHenterPerson() throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        WSHentPersonResponse hentPersonResponse = new WSHentPersonResponse()
                .withPerson(new WSPerson().withAktoer(new WSPersonIdent().withIdent(new WSNorskIdent().withIdent(IDENT))));
        when(personV3.hentPerson(any(WSHentPersonRequest.class))).thenReturn(hentPersonResponse);

        PersonData personData = personService.hentPerson(IDENT);

        assertThat(personData.getFodselsnummer(), is(equalTo(IDENT)));
    }

    @Test
    public void hentPersonBerOmRelevantInformasjonFraTjenesten() throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        WSHentPersonResponse hentPersonResponse = new WSHentPersonResponse()
                .withPerson(new WSPerson().withAktoer(new WSPersonIdent().withIdent(new WSNorskIdent().withIdent(IDENT))));
        ArgumentCaptor<WSHentPersonRequest> argumentCaptor = ArgumentCaptor.forClass(WSHentPersonRequest.class);
        when(personV3.hentPerson(argumentCaptor.capture())).thenReturn(hentPersonResponse);

        personService.hentPerson(IDENT);

        List<WSInformasjonsbehov> informasjonsBehov = argumentCaptor.getValue().getInformasjonsbehov();
        assertThat(informasjonsBehov.contains(WSInformasjonsbehov.ADRESSE), is(true));
        assertThat(informasjonsBehov.contains(WSInformasjonsbehov.KOMMUNIKASJON), is(true));
        assertThat(informasjonsBehov.contains(WSInformasjonsbehov.FAMILIERELASJONER), is(true));
        assertThat(informasjonsBehov.contains(WSInformasjonsbehov.BANKKONTO), is(true));
        assertThat(informasjonsBehov.contains(WSInformasjonsbehov.ADRESSE), is(true));
    }

    @Test(expected = HentPersonPersonIkkeFunnet.class)
    public void hentPersonSomIkkeFinnesKasterFeil() throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        when(personV3.hentPerson(any(WSHentPersonRequest.class))).thenThrow(new HentPersonPersonIkkeFunnet());

        personService.hentPerson(IDENT);
    }

    @Test(expected = HentPersonSikkerhetsbegrensning.class)
    public void hentPersonIkkeTilgangKasterFeil() throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        when(personV3.hentPerson(any(WSHentPersonRequest.class))).thenThrow(new HentPersonSikkerhetsbegrensning());

        personService.hentPerson(IDENT);
    }

    @Test
    public void hentSikkerhetstiltakHenterSikkerhetstiltak() throws HentSikkerhetstiltakPersonIkkeFunnet {
        WSSikkerhetstiltak wsSikkerhetstiltak = new WSSikkerhetstiltak().withSikkerhetstiltaksbeskrivelse(SIKKHERHETSTILTAK);
        WSHentSikkerhetstiltakResponse response = new WSHentSikkerhetstiltakResponse().withSikkerhetstiltak(wsSikkerhetstiltak);
        when(personV3.hentSikkerhetstiltak(any(WSHentSikkerhetstiltakRequest.class))).thenReturn(response);

        Sikkerhetstiltak sikkerhetstiltak = personService.hentSikkerhetstiltak(IDENT);

        assertThat(sikkerhetstiltak.sikkerhetstiltaksbeskrivelse, is(equalTo(SIKKHERHETSTILTAK)));
    }

    @Test
    public void hentSikkerhetstiltakDersomIngenSikkerhetstiltak() throws HentSikkerhetstiltakPersonIkkeFunnet {
        WSHentSikkerhetstiltakResponse response = new WSHentSikkerhetstiltakResponse();
        when(personV3.hentSikkerhetstiltak(any(WSHentSikkerhetstiltakRequest.class))).thenReturn(response);

        Sikkerhetstiltak sikkerhetstiltak = personService.hentSikkerhetstiltak(IDENT);

        assertThat(sikkerhetstiltak.sikkerhetstiltaksbeskrivelse, is(nullValue()));
    }

}