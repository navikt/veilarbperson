package no.nav.fo.veilarbperson;

import no.nav.fo.veilarbperson.consumer.digitalkontaktinformasjon.DigitalKontaktinformasjon;
import no.nav.fo.veilarbperson.consumer.digitalkontaktinformasjon.DigitalKontaktinformasjonService;
import no.nav.fo.veilarbperson.consumer.kodeverk.KodeverkImpl;
import no.nav.fo.veilarbperson.consumer.kodeverk.KodeverkService;
import no.nav.fo.veilarbperson.consumer.organisasjonenhet.Enhet;
import no.nav.fo.veilarbperson.consumer.organisasjonenhet.EnhetService;
import no.nav.fo.veilarbperson.consumer.tps.EgenAnsattService;
import no.nav.fo.veilarbperson.consumer.tps.PersonService;
import no.nav.fo.veilarbperson.domain.person.PersonData;
import no.nav.fo.veilarbperson.domain.person.Sikkerhetstiltak;
import no.nav.fo.veilarbperson.domain.person.Sivilstand;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLEnkeltKodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLKode;
import no.nav.tjeneste.virksomhet.person.v3.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.HentPersonSikkerhetsbegrensning;
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
public class PersonFletterTest {

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
    private PersonFletter personFletter;

    @Before
    public void setup() throws Exception {
        when(enhetService.hentBehandlendeEnhet(anyString())).thenReturn(new Enhet());
        when(digitalKontaktinformasjonService.hentDigitalKontaktinformasjon(anyString())).thenReturn(new DigitalKontaktinformasjon());
        when(personService.hentPerson(anyString())).thenReturn(lagPersonData());
        when(personService.hentSikkerhetstiltak(anyString())).thenReturn(new Sikkerhetstiltak(null));
        when(egenAnsattService.erEgenAnsatt(anyString())).thenReturn(true);
        when(kodeverkService.hentKodeverk(any())).thenReturn(new KodeverkImpl(new XMLEnkeltKodeverk()));
    }

    @Test
    public void hentPersonSkalReturnereEnPerson() throws Exception {
        final PersonData personData = personFletter.hentPerson("", "");

        assertThat(personData, notNullValue());
    }

    @Test
    public void personSkalInneholdeGeografiskTilknytningDersomDetEksisterer() throws Exception {
        final PersonData forventetPersonData = lagPersonData();
        final String geografiskTilknytning = "1234";
        forventetPersonData.setGeografiskTilknytning(geografiskTilknytning);
        when(personService.hentPerson(anyString())).thenReturn(forventetPersonData);

        final PersonData returnertPersonData = personFletter.hentPerson("", "");

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

        final PersonData returnertPersonData = personFletter.hentPerson("", "");

        assertThat(returnertPersonData.getSivilstand(), notNullValue());
        assertThat(returnertPersonData.getSivilstand().getSivilstand(), is(kodeverksverdi));
    }

    @Test(expected = HentPersonPersonIkkeFunnet.class)
    public void hentPersonForIkkeEksisterendePersonSkalKasteFeil() throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        when(personService.hentPerson(anyString())).thenThrow(new HentPersonPersonIkkeFunnet());
        String fnr = TestUtil.fodselsnummerForDato("1988-02-17");
        personFletter.hentPerson(fnr, "");
    }

    @Test(expected = HentPersonSikkerhetsbegrensning.class)
    public void hentPersonNarSaksbehandlerIkkeHarTilgangSkalKasteFeil() throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        when(personService.hentPerson(anyString())).thenThrow(new HentPersonSikkerhetsbegrensning());
        String fnr = TestUtil.fodselsnummerForDato("1988-02-17");
        personFletter.hentPerson(fnr, "");
    }

    private PersonData lagPersonData() {
        final PersonData personData = new PersonData();
        personData.setSivilstand(new Sivilstand());
        personData.setGeografiskTilknytning("");
        return personData;
    }
}