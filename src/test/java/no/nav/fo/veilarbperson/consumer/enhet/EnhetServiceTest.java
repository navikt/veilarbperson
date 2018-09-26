package no.nav.fo.veilarbperson.consumer.enhet;

import no.nav.fo.veilarbperson.consumer.organisasjonenhet.Enhet;
import no.nav.fo.veilarbperson.consumer.organisasjonenhet.EnhetService;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.FinnNAVKontorUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.OrganisasjonEnhetV2;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.informasjon.WSOrganisasjonsenhet;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.meldinger.WSFinnNAVKontorRequest;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.meldinger.WSFinnNAVKontorResponse;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EnhetServiceTest {

    private static String MOCK_ENHET_ID = "5678";
    private static String MOCK_ENHET_NAVN = "NAV Aremark";
    private static String MOCK_GEOGRAFISK_NEDSLAGSFELT = "1234";

    private OrganisasjonEnhetV2 organisasjonEnhet;
    private EnhetService service;

    @Before
    public void before() {
        organisasjonEnhet = mock(OrganisasjonEnhetV2.class);
        service = new EnhetService(organisasjonEnhet);
    }

    @Test
    public void hentBehandlendeEnhetReturnererEnhet() throws FinnNAVKontorUgyldigInput {
        WSFinnNAVKontorResponse navkontorResponse =
                new WSFinnNAVKontorResponse()
                        .withNAVKontor(mockEnhet());
        when(organisasjonEnhet.finnNAVKontor(any(WSFinnNAVKontorRequest.class)))
                .thenReturn(navkontorResponse);

        Enhet enhet = service.hentBehandlendeEnhet(MOCK_GEOGRAFISK_NEDSLAGSFELT);

        assertThat(enhet.getEnhetsnummer(), is(equalTo(MOCK_ENHET_ID)));
        assertThat(enhet.getNavn(), is(equalTo(MOCK_ENHET_NAVN)));
    }

    private WSOrganisasjonsenhet mockEnhet() {
        return new WSOrganisasjonsenhet()
                .withEnhetId(MOCK_ENHET_ID)
                .withEnhetNavn(MOCK_ENHET_NAVN);
    }

    @Test
    public void hentBehandlendeEnhetMedTomRespons() throws FinnNAVKontorUgyldigInput {
        WSFinnNAVKontorResponse nullResponse = new WSFinnNAVKontorResponse();
        when(organisasjonEnhet.finnNAVKontor(any(WSFinnNAVKontorRequest.class)))
                .thenReturn(nullResponse);

        Enhet enhet = service.hentBehandlendeEnhet(MOCK_GEOGRAFISK_NEDSLAGSFELT);

        assertThat(enhet, is(equalTo(null)));
    }

    @Test
    public void hentBehandlendeEnhetMedUgyldigInput() throws FinnNAVKontorUgyldigInput {
        when(organisasjonEnhet.finnNAVKontor(any(WSFinnNAVKontorRequest.class)))
                .thenThrow(new FinnNAVKontorUgyldigInput());

        Enhet enhet = service.hentBehandlendeEnhet(MOCK_GEOGRAFISK_NEDSLAGSFELT);

        assertThat(enhet, is(equalTo(null)));
    }

}