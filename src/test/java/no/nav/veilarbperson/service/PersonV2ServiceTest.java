package no.nav.veilarbperson.service;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.nav.common.client.norg2.Enhet;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.featuretoggle.UnleashService;
import no.nav.veilarbperson.client.difi.DifiCient;
import no.nav.veilarbperson.client.difi.DifiClientImpl;
import no.nav.veilarbperson.client.dkif.DkifClient;
import no.nav.veilarbperson.client.dkif.DkifKontaktinfo;
import no.nav.veilarbperson.client.egenansatt.EgenAnsattClient;
import no.nav.veilarbperson.client.pam.PamClient;
import no.nav.veilarbperson.client.pdl.HentPdlPerson;
import no.nav.veilarbperson.client.pdl.PdlClient;
import no.nav.veilarbperson.client.pdl.PdlClientImpl;
import no.nav.veilarbperson.client.pdl.PersonV2Data;
import no.nav.veilarbperson.client.pdl.domain.*;
import no.nav.veilarbperson.client.person.PersonClient;
import no.nav.veilarbperson.client.veilarbportefolje.VeilarbportefoljeClient;
import no.nav.veilarbperson.utils.PersonV2DataMapper;
import no.nav.veilarbperson.utils.TestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Optional.ofNullable;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
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
    private HentPdlPerson.PdlPerson pdlPerson;
    private final UnleashService unleashService = mock(UnleashService.class);

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    static final String FNR = "0123456789";
    String[] testFnrsTilBarna = {"12345678910", "12345678911", "12345678912"};

    @Before
    public void setup() {

        when(norg2Client.hentTilhorendeEnhet(anyString())).thenReturn(new Enhet());
        when(dkifClient.hentKontaktInfo(any())).thenReturn(new DkifKontaktinfo());
        when(personClient.hentSikkerhetstiltak(any())).thenReturn(null);
        when(egenAnsattClient.erEgenAnsatt(any())).thenReturn(true);
        when(pdlClient.hentPersonBolk(any())).thenReturn(hentPersonBolk(testFnrsTilBarna));
        personService = new PersonService(norg2Client, personClient, egenAnsattClient, dkifClient, kodeverkService, veilarbportefoljeClient, difiCient, null, unleashService);
        personV2Service = new PersonV2Service(pdlClient, authService, dkifClient, norg2Client, personClient, pamClient, egenAnsattClient, veilarbportefoljeClient, kodeverkService);
        pdlPerson = hentPerson(FNR);
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

    public PdlClientImpl configurPdlClient(String responseFile) {
        String apiUrl = configurApiResponse(responseFile);
        return new PdlClientImpl(apiUrl, () -> "SYSTEM_USER_TOKEN");
    }

    public HentPdlPerson.PdlPerson hentPerson(String fnr) {
        PdlClientImpl pdlClient = configurPdlClient("pdl-hentPerson-response.json");
        return pdlClient.hentPerson(fnr, "USER_TOKEN");
    }

    public List<HentPdlPerson.Barn> hentPersonBolk(String[] fnrs) {
        String apiUrl = configurApiResponse("pdl-hentPersonBolk-response.json");
        PdlClientImpl pdlClient = new PdlClientImpl(apiUrl, () -> "SYSTEM_USER_TOKEN");

        return pdlClient.hentPersonBolk(fnrs);
    }

    public HentPdlPerson.Familiemedlem hentPartner(String fnr) {
        PdlClientImpl pdlClient = configurPdlClient("pdl-hentPartner-response.json");
        return pdlClient.hentPartner(fnr, "USER_TOKEN");
    }

    public HentPdlPerson.GeografiskTilknytning hentGeografisktilknytning(String fnr) {
        PdlClientImpl pdlClient = configurPdlClient("pdl-hentGeografiskTilknytning-response.json");
        return pdlClient.hentGeografiskTilknytning(fnr, "USER_TOKEN");
    }

    public HentPdlPerson.PdlPerson hentFamiliemedlem(String fnr) {
        PdlClientImpl pdlClient = configurPdlClient("pdl-hentPersonMedIngenBarn-responsen.json");
        return pdlClient.hentPerson(fnr, "USER_TOKEN");
    }

    @Test
    public void hentFamilieRelasjonerSkalHenteForeldreOgBarnRelasjoner() {
        List<HentPdlPerson.Familierelasjoner> familierelasjoner = pdlPerson.getFamilierelasjoner();

        assertEquals("12345678910", familierelasjoner.get(0).getRelatertPersonsIdent());
        assertEquals("BARN", familierelasjoner.get(0).getRelatertPersonsRolle());

        assertEquals("12345678911", familierelasjoner.get(1).getRelatertPersonsIdent());
        assertEquals("BARN", familierelasjoner.get(1).getRelatertPersonsRolle());

        assertEquals("12345678912", familierelasjoner.get(2).getRelatertPersonsIdent());
        assertEquals("BARN", familierelasjoner.get(2).getRelatertPersonsRolle());
    }

    @Test
    public void hentFnrTilBarnaTest() {
        List<HentPdlPerson.Familierelasjoner> familierelasjoner = pdlPerson.getFamilierelasjoner();
        String[] fnrListe = personV2Service.hentFnrTilBarna(familierelasjoner);

        assertEquals(3, fnrListe.length);

        for(int i =0; i<testFnrsTilBarna.length; i++) {
            assertEquals(testFnrsTilBarna[i], fnrListe[i]);
        }
    }

    @Test
    public void hentOpplysningerTilBarnaMedKodeOkFraPdlTest() {
        List<HentPdlPerson.Barn> hentPersonBolk = hentPersonBolk(testFnrsTilBarna);

        assertEquals(3, hentPersonBolk.size());

        List<HentPdlPerson.Barn> filterPersonBolkMedOkStatus = ofNullable(hentPersonBolk).stream().flatMap(Collection::stream)
                                                                        .filter(status -> status.getCode().equals("ok"))
                                                                        .collect(Collectors.toList());

        assertEquals(1, filterPersonBolkMedOkStatus.size());
    }

    @Test
    public void hentDiskresjonsKodeTilAdressebeskyttetPersonTest() {
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
        HentPdlPerson.Navn navn = pdlPerson.getNavn().get(0);

        assertEquals("NATURLIG", navn.getFornavn());
        assertEquals("GLITRENDE", navn.getMellomnavn());
        assertEquals("STAFFELI", navn.getEtternavn());
        assertEquals("NATURLIG STAFFELI", navn.getForkortetNavn());
    }

    @Test
    public void unngoArrayIndexOutOfBoundExceptionNorListeErTomIPdlTest() {
        String doedsfall = ofNullable(PersonV2DataMapper.getFirstElement(pdlPerson.getDoedsfall())).map(HentPdlPerson.Doedsfall::getDoedsdato).orElse(null);
        assertNull(doedsfall);
    }

    @Test
    public void hentPartnerInformasjonTest() {
        String fnrTilPartner = personV2Service.hentFnrTilPartner(pdlPerson.getSivilstand());
        Bostedsadresse personsBostedsAdresse = pdlPerson.getBostedsadresse().get(0);

        assertEquals("2134567890", fnrTilPartner);

        HentPdlPerson.Familiemedlem partnerInformasjon = hentPartner(fnrTilPartner);
        Familiemedlem partner = PersonV2DataMapper.familiemedlemMapper(partnerInformasjon, personsBostedsAdresse);

        assertEquals("TYKKMAGET GASELLE", partner.getForkortetNavn());
        assertEquals("1981-12-13", partner.getFodselsdato());
    }

    @Test
    public void hentGeografiskTilknytningTest() {
        HentPdlPerson.GeografiskTilknytning geografiskTilknytning = hentGeografisktilknytning(FNR);

        assertEquals("0570", geografiskTilknytning.getGtKommune());
        assertEquals("OSLO", geografiskTilknytning.getGtBydel());
        assertEquals("NORGE", geografiskTilknytning.getGtLand());
    }

    @Test
    public void getLandKodeFraKontaktadresseTest() {
        Kontaktadresse.UtenlandskAdresseIFrittFormat midlertidigAdresseUtland = pdlPerson.getKontaktadresse().get(0).getUtenlandskAdresseIFrittFormat();
        Optional<String> landkode = ofNullable(midlertidigAdresseUtland).map(Kontaktadresse.UtenlandskAdresseIFrittFormat::getLandkode);

        assertEquals(landkode.get(), "FRA");

        Kontaktadresse.UtenlandskAdresseIFrittFormat nullMidlertidigAdresseUtland = null;
        Optional<String> nullLandkode = ofNullable(nullMidlertidigAdresseUtland).map(Kontaktadresse.UtenlandskAdresseIFrittFormat::getLandkode);

        assertTrue(nullLandkode.isEmpty());
    }

    @Test
    public void getPostnummerFraBostedsadresseTest() {
        Bostedsadresse bostedsadresse = pdlPerson.getBostedsadresse().get(0);
        Optional<String> postnummer = ofNullable(bostedsadresse).map(Bostedsadresse::getVegadresse).map(Adresse.Vegadresse::getPostnummer);

        assertEquals("0560", postnummer.get());

        Bostedsadresse nullBostedsadresse = null;
        Optional<String> nullPostnummer = ofNullable(nullBostedsadresse).map(Bostedsadresse::getVegadresse).map(Adresse.Vegadresse::getPostnummer);

        assertTrue(nullPostnummer.isEmpty());
    }

    @Test
    public void flettBarnInformasjonTest() {
        PersonV2Data personV2Data = lagPersonV2Data();

        assertEquals(0, personV2Data.getBarn().size());

        personV2Service.flettBarnInformasjon(pdlPerson.getFamilierelasjoner(), personV2Data); // Forsøker å flette person med 3 barn hvor informasjonen til bare 1 barn er tilgjemgelig i PDL

        assertEquals(1, personV2Data.getBarn().size()); // Fant bare 1 av 3 barna med "ok"(gyldig) status fra hentPersonBolk operasjonen

        pdlPerson = hentFamiliemedlem("12345678910");  // Hent person med ingen barn for ex.Opplysninger til et barn selv
        personV2Data = lagPersonV2Data();

        personV2Service.flettBarnInformasjon(pdlPerson.getFamilierelasjoner(), personV2Data); // Forsøker å flette person som har ingen barn

        assertEquals(Collections.emptyList(), personV2Data.getBarn());     // Ingen barn blir lagt i personV2Data
    }

    @Test
    public void harFamiliamedlemSammeBostedSomPersonTest() {
        Bostedsadresse personsBostedsAdresse = pdlPerson.getBostedsadresse().get(0);
        Bostedsadresse familiemedlemsBostedsAdresse = new Bostedsadresse();

        Adresse.Vegadresse medlemsVegAdresse = new Adresse.Vegadresse()
                .setMatrikkelId(123L).setAdressenavn("ARENDALSGATE").setHusbokstav("A").setHusnummer("21").setKommunenummer("0570").setPostnummer("0560").setPoststed("OSLO").setTilleggsnavn("ARENDAL");
        familiemedlemsBostedsAdresse.setVegadresse(medlemsVegAdresse);

        boolean harSammeBbosted = PersonV2DataMapper.harFamiliamedlemSammeBostedSomPerson(familiemedlemsBostedsAdresse, personsBostedsAdresse); // Sammeligner to ulike bostedsadresser

        assertFalse(harSammeBbosted);

        familiemedlemsBostedsAdresse = hentFamiliemedlem("12345678910").getBostedsadresse().get(0);

        harSammeBbosted = PersonV2DataMapper.harFamiliamedlemSammeBostedSomPerson(familiemedlemsBostedsAdresse, personsBostedsAdresse);  // Sammeligner to like bostedsadresser

        assertTrue(harSammeBbosted);
    }

    @Test
    public void telfonNummerMapperTest() {
        HentPdlPerson.Telefonnummer telefonNrFraPdl = pdlPerson.getTelefonnummer().get(0);   //telefonNrFraPdl: landkode: +47 nummer: 33333333
        String telefonNummer = PersonV2DataMapper.telefonNummerMapper(telefonNrFraPdl);
        assertEquals("+4733333333", telefonNummer);
    }

    @Test
    public void leggKrrTelefonNrIListeTest() {
        String telefonNrFraKrr = "+4622222222";
        HentPdlPerson.Telefonnummer telefonNrFraPdl = pdlPerson.getTelefonnummer().get(0);
        List<String> telefonListe = personV2Service.leggKrrTelefonNrIListe(telefonNrFraKrr, telefonNrFraPdl);  // Legger telefonnummere fra PDL og KRR som er ulike til en liste

        assertEquals(2, telefonListe.size());
        assertEquals("+4733333333", telefonListe.get(0));
        assertEquals("+4622222222", telefonListe.get(1));

        telefonNrFraKrr = "+4733333333";
        telefonListe = personV2Service.leggKrrTelefonNrIListe(telefonNrFraKrr, telefonNrFraPdl); // Legger telefonnummere fra PDL og KRR som er like til en liste

        assertEquals(1, telefonListe.size());
        assertEquals("+4733333333", telefonListe.get(0));

        telefonNrFraKrr = null;
        telefonListe = personV2Service.leggKrrTelefonNrIListe(telefonNrFraKrr, telefonNrFraPdl); // Legger telefonnummere fra PDL og KRR til en liste hvor telefonnummer fra KRR er null

        assertEquals(1, telefonListe.size());
        assertEquals("+4733333333", telefonListe.get(0));

        telefonNrFraPdl = null;
        telefonNrFraKrr = "+4733333333";
        telefonListe = personV2Service.leggKrrTelefonNrIListe(telefonNrFraKrr, telefonNrFraPdl); // Legger telefonnummere fra PDL og KRR til en liste hvor telefonnummer fra PDL er null

        assertEquals(1, telefonListe.size());
        assertEquals("+4733333333", telefonListe.get(0));
    }

    public PersonV2Data lagPersonV2Data() {
        PersonV2Data personV2Data = new PersonV2Data();
        Bostedsadresse personsBostedsAdresse = PersonV2DataMapper.getFirstElement(pdlPerson.getBostedsadresse());

        personV2Data.setBostedsadresse(personsBostedsAdresse);
        personV2Data.setTelefon(new ArrayList<>(List.of("+4733333333")));

        return personV2Data;

    }
}
