package no.nav.veilarbperson.client.digitalkontaktinformasjon;

import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonSikkerhetsbegrensing;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSEpostadresse;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSKontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSMobiltelefonnummer;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonRequest;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonResponse;

import no.nav.veilarbperson.TestUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DigitalKontaktinformasjonServiceTest {

    private final static String FODSELSNUMMER = TestUtil.fodselsnummerForDato("1980-01-01");
    private final static String MOCK_EPOST = "test@testesen.com";
    private final static String MOCK_TELEFON = "99999999";

    @Mock
    DigitalKontaktinformasjonV1 wsDkifService;

    @InjectMocks
    DigitalKontaktinformasjonService service;

    @Test
    public void serviceSkalHenteKontaktinformasjon() throws HentDigitalKontaktinformasjonSikkerhetsbegrensing, HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet, HentDigitalKontaktinformasjonPersonIkkeFunnet {
        WSKontaktinformasjon kontaktinformasjon = new WSKontaktinformasjon()
                .withEpostadresse(new WSEpostadresse().withValue(MOCK_EPOST))
                .withMobiltelefonnummer(new WSMobiltelefonnummer().withValue(MOCK_TELEFON));
        WSHentDigitalKontaktinformasjonResponse mockResponse = new WSHentDigitalKontaktinformasjonResponse()
                .withDigitalKontaktinformasjon(kontaktinformasjon);

        when(wsDkifService.hentDigitalKontaktinformasjon(any(WSHentDigitalKontaktinformasjonRequest.class)))
                .thenReturn(mockResponse);

        DigitalKontaktinformasjon digitalKontaktinformasjon = service.hentDigitalKontaktinformasjon(FODSELSNUMMER);

        assertThat(digitalKontaktinformasjon.getEpost(), is(equalTo(MOCK_EPOST)));
        assertThat(digitalKontaktinformasjon.getTelefon(), is(equalTo(MOCK_TELEFON)));
    }

    @Test
    public void epostSettesTilNullHvisIngenEpostFinnes() throws HentDigitalKontaktinformasjonSikkerhetsbegrensing, HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet, HentDigitalKontaktinformasjonPersonIkkeFunnet {
        WSKontaktinformasjon kontaktinformasjon = new WSKontaktinformasjon();
        WSHentDigitalKontaktinformasjonResponse mockResponse = new WSHentDigitalKontaktinformasjonResponse()
                .withDigitalKontaktinformasjon(kontaktinformasjon);

        when(wsDkifService.hentDigitalKontaktinformasjon(any(WSHentDigitalKontaktinformasjonRequest.class)))
                .thenReturn(mockResponse);

        DigitalKontaktinformasjon digitalKontaktinformasjon = service.hentDigitalKontaktinformasjon(FODSELSNUMMER);


        assertThat(digitalKontaktinformasjon.getEpost(), is(nullValue()));
        assertThat(digitalKontaktinformasjon.getTelefon(), is(nullValue()));
    }

}