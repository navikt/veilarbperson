package no.nav.veilarbperson.service;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.nav.common.client.norg2.Enhet;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.veilarbperson.client.difi.DifiCient;
import no.nav.veilarbperson.client.difi.DifiClientImpl;
import no.nav.veilarbperson.client.dkif.DkifClient;
import no.nav.veilarbperson.client.dkif.DkifKontaktinfo;
import no.nav.veilarbperson.client.egenansatt.EgenAnsattClient;
import no.nav.veilarbperson.client.pam.PamClient;
import no.nav.veilarbperson.client.pdl.HentPdlPerson;
import no.nav.veilarbperson.client.pdl.PdlClient;
import no.nav.veilarbperson.client.pdl.PdlClientImpl;
import no.nav.veilarbperson.client.pdl.domain.Diskresjonskoder;
import no.nav.veilarbperson.client.pdl.domain.Familiemedlem;
import no.nav.veilarbperson.client.person.PersonClient;
import no.nav.veilarbperson.client.veilarbportefolje.VeilarbportefoljeClient;
import no.nav.veilarbperson.utils.PersonV2DataMapper;
import no.nav.veilarbperson.utils.TestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PersonV2ServiceTest {
    private Norg2Client norg2Client = mock(Norg2Client.class);
    private DkifClient dkifClient = mock(DkifClient.class);
    private PersonClient personClient = mock(PersonClient.class);
    private PdlClient pdlClient = mock(PdlClient.class);
    private EgenAnsattClient egenAnsattClient = mock(EgenAnsattClient.class);
    private KodeverkService kodeverkService = mock(KodeverkService.class);
    private VeilarbportefoljeClient veilarbportefoljeClient = mock(VeilarbportefoljeClient.class);
    private DifiCient difiCient = mock(DifiClientImpl.class);
    private AuthService authService = mock(AuthService.class);
    private PamClient pamClient = mock(PamClient.class);
    private PersonService personService;
    private PersonV2Service personV2Service;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    String[] testFnrsTilBarna = {"12345678910", "12345678911", "12345678912"};

    @Before
    public void setup() {

        when(norg2Client.hentTilhorendeEnhet(anyString())).thenReturn(new Enhet());
        when(dkifClient.hentKontaktInfo(any())).thenReturn(new DkifKontaktinfo());
        when(personClient.hentSikkerhetstiltak(any())).thenReturn(null);
        when(egenAnsattClient.erEgenAnsatt(any())).thenReturn(true);
        personService = new PersonService(norg2Client, personClient, egenAnsattClient, dkifClient, kodeverkService, veilarbportefoljeClient, difiCient, null);
        personV2Service = new PersonV2Service(pdlClient, authService, dkifClient, norg2Client, personClient, pamClient, egenAnsattClient, veilarbportefoljeClient, kodeverkService);
    }

    public String configurApiResponse(String responseFilename) {
        String hentPersonResponseJson = TestUtils.readTestResourceFile(responseFilename);
        String apiUrl = "http://localhost:" + wireMockRule.port();

        givenThat(post(anyUrl())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(hentPersonResponseJson))
        );

        return apiUrl;
    }

    public HentPdlPerson.PdlPerson hentPerson(String fnr) {
        String apiUrl = configurApiResponse("pdl-hentPerson-response.json");
        PdlClientImpl pdlClient = new PdlClientImpl(apiUrl, () -> "SYSTEM_USER_TOKEN");

        return pdlClient.hentPerson(fnr, "USER_TOKEN");
    }

    public List<HentPdlPerson.PdlPersonBolk> hentPersonBolk(String[] fnrs) {
        String apiUrl = configurApiResponse("pdl-hentPersonBolk-response.json");
        PdlClientImpl pdlClient = new PdlClientImpl(apiUrl, () -> "SYSTEM_USER_TOKEN");

        return pdlClient.hentPersonBolk(fnrs);
    }

    public HentPdlPerson.PersonsFamiliemedlem hentPartnerOpplysninger(String fnr) {
        String apiUrl = configurApiResponse("pdl-hentPersonsPartner-response.json");

        PdlClientImpl pdlClient = new PdlClientImpl(apiUrl, () -> "SYSTEM_USER_TOKEN");

        return pdlClient.hentPartnerOpplysninger(fnr, "USER_TOKEN");
    }

    @Test
    public void hentFamilieRelasjonerSkalHenteForeldreOgBarnRelasjoner() {
        HentPdlPerson.PdlPerson pdlPerson = hentPerson("0123456789");

        assertEquals("12345678910", pdlPerson.getFamilierelasjoner().get(0).getRelatertPersonsIdent());
        assertEquals("BARN",pdlPerson.getFamilierelasjoner().get(0).getRelatertPersonsRolle());

        assertEquals("12345678911", pdlPerson.getFamilierelasjoner().get(1).getRelatertPersonsIdent());
        assertEquals("BARN",pdlPerson.getFamilierelasjoner().get(1).getRelatertPersonsRolle());

        assertEquals("12345678912", pdlPerson.getFamilierelasjoner().get(2).getRelatertPersonsIdent());
        assertEquals("BARN",pdlPerson.getFamilierelasjoner().get(2).getRelatertPersonsRolle());
    }

    @Test
    public void hentFnrTilBarnaTest() {
        HentPdlPerson.PdlPerson pdlPerson = hentPerson("0123456789");
        List<HentPdlPerson.Familierelasjoner> familierelasjoner = pdlPerson.getFamilierelasjoner();
        String[] fnrListe = personV2Service.hentFnrTilBarna(familierelasjoner);

        assertEquals(3, fnrListe.length);

        for(int i =0; i<testFnrsTilBarna.length; i++) {
            assertEquals(testFnrsTilBarna[i], fnrListe[i]);
        }
    }

    @Test
    public void hentOpplysningerTilBarnaMedKodeOkFraPdlTest() {
        List<HentPdlPerson.PdlPersonBolk> pdlPersonBolk = hentPersonBolk(testFnrsTilBarna);

        assertEquals(3, pdlPersonBolk.size());

        List<HentPdlPerson.PdlPersonBolk> filterPersonBolkMedOkStatus = pdlPersonBolk.stream()
                                                                        .filter(status -> status.getCode().equals("ok"))
                                                                        .collect(Collectors.toList());

        assertEquals(1, filterPersonBolkMedOkStatus.size());
    }

    @Test
    public void hentDiskresjonsKodeTilAdressebeskyttetPersonTest() {
        HentPdlPerson.PdlPerson pdlPerson = hentPerson("0123456789");

        HentPdlPerson.Adressebeskyttelse adressebeskyttelse = PersonV2DataMapper.getFirstElement(pdlPerson.getAdressebeskyttelse());
        String gradering = adressebeskyttelse.getGradering();
        String diskresjonskode = Diskresjonskoder.mapTilTallkode(gradering);

        assertEquals(Diskresjonskoder.UGRADERT.toString(), gradering);
        assertEquals("0", diskresjonskode);

        String kode6Bruker = "STRENGT_FORTROLIG";
        assertEquals("6", Diskresjonskoder.mapTilTallkode(kode6Bruker));

        String kode7Bruker = "FORTROLIG";
        assertEquals("7", Diskresjonskoder.mapTilTallkode(kode7Bruker));
    }

    @Test
    public void hentNavnTest() {
        HentPdlPerson.PdlPerson pdlPerson = hentPerson("0123456789");
        HentPdlPerson.Navn navn = PersonV2DataMapper.getFirstElement(pdlPerson.getNavn());

        assertEquals("NATURLIG", navn.getFornavn());
        assertEquals("GLITRENDE", navn.getMellomnavn());
        assertEquals("STAFFELI", navn.getEtternavn());
        assertEquals("NATURLIG STAFFELI", navn.getForkortetnavn());
    }

    @Test
    public void unngoArrayIndexOutOfBoundExceptionNorListeErTomIPdlTest() {
        HentPdlPerson.PdlPerson pdlPerson = hentPerson("0123456789");
        String doedsfall = Optional.ofNullable(PersonV2DataMapper.getFirstElement(pdlPerson.getDoedsfall())).map(HentPdlPerson.Doedsfall::getDoedsdato).orElse(null);

        assertNull(doedsfall);
    }

    @Test
    public void hentPartnerInformasjonTest() {
        HentPdlPerson.PdlPerson pdlPerson = hentPerson("0123456789");
        String fnrTilPartner = personV2Service.hentFnrTilPartner(pdlPerson.getSivilstand());

        assertEquals("2134567890", fnrTilPartner);

        HentPdlPerson.PersonsFamiliemedlem partnerInformasjon = hentPartnerOpplysninger(fnrTilPartner);
        Familiemedlem partner = PersonV2DataMapper.familiemedlemMapper(partnerInformasjon);

        assertEquals("TYKKMAGET GASELLE", partner.getSammensattNavn());

        assertEquals("1981-12-13", partner.getFodselsdato());
    }

}
