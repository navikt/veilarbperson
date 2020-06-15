package no.nav.veilarbperson;

import no.nav.veilarbperson.client.digitalkontaktinformasjon.DigitalKontaktinformasjon;
import no.nav.veilarbperson.client.digitalkontaktinformasjon.DigitalKontaktinformasjonService;
import no.nav.veilarbperson.client.kodeverk.KodeverkImpl;
import no.nav.veilarbperson.client.kodeverk.KodeverkService;
import no.nav.veilarbperson.client.organisasjonenhet.Enhet;
import no.nav.veilarbperson.client.organisasjonenhet.EnhetService;
import no.nav.veilarbperson.client.tps.EgenAnsattService;
import no.nav.veilarbperson.client.tps.PersonService;
import no.nav.veilarbperson.domain.person.PersonData;
import no.nav.veilarbperson.domain.person.Sikkerhetstiltak;
import no.nav.veilarbperson.domain.person.Sivilstand;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLEnkeltKodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLKode;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.feil.PersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.feil.Sikkerhetsbegrensning;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PersonFletterServiceTest {

    @Mock
    private EnhetService enhetService;

    @Mock
    private DigitalKontaktinformasjonService digitalKontaktinformasjonService;

    @Mock
    private PersonService personService;

    @Mock
    private EgenAnsattService egenAnsattService;

    @Mock
    private KodeverkService kodeverkService;

    @InjectMocks
    private PersonFletterService personFletterService;

    @Before
    public void setup() throws Exception {
        when(enhetService.hentGeografiskEnhet(anyString())).thenReturn(new Enhet());
        when(digitalKontaktinformasjonService.hentDigitalKontaktinformasjon(anyString())).thenReturn(new DigitalKontaktinformasjon());
        when(personService.hentPerson(anyString())).thenReturn(lagPersonData());
        when(personService.hentSikkerhetstiltak(anyString())).thenReturn(new Sikkerhetstiltak(null));
        when(egenAnsattService.erEgenAnsatt(anyString())).thenReturn(true);
        when(kodeverkService.hentKodeverk(any())).thenReturn(new KodeverkImpl(new XMLEnkeltKodeverk()));
    }

    @Test
    public void hentPersonSkalReturnereEnPerson() throws Exception {
        final PersonData personData = personFletterService.hentPerson("", "");

        assertThat(personData, notNullValue());
    }

    @Test
    public void personSkalInneholdeGeografiskTilknytningDersomDetEksisterer() throws Exception {
        final PersonData forventetPersonData = lagPersonData();
        final String geografiskTilknytning = "1234";
        forventetPersonData.setGeografiskTilknytning(geografiskTilknytning);
        when(personService.hentPerson(anyString())).thenReturn(forventetPersonData);

        final PersonData returnertPersonData = personFletterService.hentPerson("", "");

        assertThat(returnertPersonData.getGeografiskTilknytning(), is(geografiskTilknytning));
    }

    @Test
    public void personSkalInneholdeSivilstandDersomDetEksisterer() throws Exception {
        final PersonData forventetPersonData = lagPersonData();
        final String kodeverksverdi = "GIFT";
        forventetPersonData.setSivilstand(new Sivilstand().withSivilstand(kodeverksverdi));
        when(personService.hentPerson(anyString())).thenReturn(forventetPersonData);
        when(kodeverkService.getVerdi(anyString(), anyString(), anyString())).thenReturn(kodeverksverdi);
        when(kodeverkService.hentKodeverk(any(String.class))).thenReturn(
                new KodeverkImpl(new XMLEnkeltKodeverk().withKode(new XMLKode().withNavn(kodeverksverdi)))
        );

        final PersonData returnertPersonData = personFletterService.hentPerson("", "");

        assertThat(returnertPersonData.getSivilstand(), notNullValue());
        assertThat(returnertPersonData.getSivilstand().getSivilstand(), is(kodeverksverdi));
    }

    @Test(expected = HentPersonPersonIkkeFunnet.class)
    public void hentPersonForIkkeEksisterendePersonSkalKasteFeil() throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        when(personService.hentPerson(anyString())).thenThrow(new HentPersonPersonIkkeFunnet("", new PersonIkkeFunnet()));
        String fnr = TestUtil.fodselsnummerForDato("1988-02-17");
        personFletterService.hentPerson(fnr, "");
    }

    @Test(expected = HentPersonSikkerhetsbegrensning.class)
    public void hentPersonNarSaksbehandlerIkkeHarTilgangSkalKasteFeil() throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        when(personService.hentPerson(anyString())).thenThrow(new HentPersonSikkerhetsbegrensning("", new Sikkerhetsbegrensning()));
        String fnr = TestUtil.fodselsnummerForDato("1988-02-17");
        personFletterService.hentPerson(fnr, "");
    }

    private PersonData lagPersonData() {
        final PersonData personData = new PersonData();
        personData.setSivilstand(new Sivilstand());
        personData.setGeografiskTilknytning("");
        return personData;
    }
}