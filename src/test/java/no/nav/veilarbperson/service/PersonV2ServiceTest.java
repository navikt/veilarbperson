package no.nav.veilarbperson.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.common.client.norg2.Enhet;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.featuretoggle.UnleashClient;
import no.nav.common.sts.SystemUserTokenProvider;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.difi.DifiCient;
import no.nav.veilarbperson.client.dkif.DkifClient;
import no.nav.veilarbperson.client.dkif.DkifKontaktinfo;
import no.nav.veilarbperson.client.nom.SkjermetClient;
import no.nav.veilarbperson.client.pdl.HentPerson;
import no.nav.veilarbperson.client.pdl.PdlAuth;
import no.nav.veilarbperson.client.pdl.PdlClient;
import no.nav.veilarbperson.client.pdl.UserTokenProviderPdl;
import no.nav.veilarbperson.client.pdl.domain.*;
import no.nav.veilarbperson.client.person.PersonClient;
import no.nav.veilarbperson.client.pdl.domain.RelasjonsBosted;
import no.nav.veilarbperson.client.person.TpsPerson;
import no.nav.veilarbperson.config.PdlClientTestConfig;
import no.nav.veilarbperson.domain.PersonNavnV2;
import no.nav.veilarbperson.domain.PersonV2Data;
import no.nav.veilarbperson.domain.TilrettelagtKommunikasjonData;
import no.nav.veilarbperson.domain.VergeOgFullmaktData;
import no.nav.veilarbperson.utils.PersonV2DataMapper;
import no.nav.veilarbperson.utils.TestUtils;
import no.nav.veilarbperson.utils.VergeOgFullmaktDataMapper;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Optional.ofNullable;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PersonV2ServiceTest extends PdlClientTestConfig {
    private Norg2Client norg2Client = mock(Norg2Client.class);
    private DkifClient dkifClient = mock(DkifClient.class);
    private PersonClient personClient = mock(PersonClient.class);
    private PdlClient pdlClient;
    private KodeverkService kodeverkService = mock(KodeverkService.class);
    private SkjermetClient skjermetClient = mock(SkjermetClient.class);
    private AuthService authService = mock(AuthService.class);
    private SystemUserTokenProvider systemUserTokenProvider = mock(SystemUserTokenProvider.class);
    private PersonV2Service personV2Service;
    private HentPerson.Person person;
    private static final PdlAuth PDL_AUTH = new PdlAuth("USER_TOKEN", Optional.of("SYSTEM_TOKEN"));
    private static Fnr FNR = Fnr.of("0123456789");
    private String fnrRelatertSivilstand = "2134567890";
    private String fnrBarn1 = "12345678910";
    private String fnrBarn2 = "12345678911";
    List<Fnr> testFnrsTilBarna = new ArrayList<>(List.of(Fnr.of(fnrBarn1), Fnr.of(fnrBarn2)));

    @Before
    public void setup() {
        pdlClient = getPdlClient();
        when(systemUserTokenProvider.getSystemUserToken()).thenReturn("SYSTEM_USER_TOKEN");
        when(norg2Client.hentTilhorendeEnhet(anyString(), any(), anyBoolean())).thenReturn(new Enhet());
        when(dkifClient.hentKontaktInfo(any())).thenReturn(new DkifKontaktinfo());
        when(personClient.hentPerson(FNR)).thenReturn(new TpsPerson().setKontonummer("123456789"));
        UserTokenProviderPdl tokenProvider = new UserTokenProviderPdl(() -> "test");

        personV2Service = new PersonV2Service(
                pdlClient,
                mock(DifiCient.class),
                authService,
                dkifClient,
                norg2Client,
                personClient,
                mock(UnleashClient.class),
                skjermetClient,
                kodeverkService,
                tokenProvider,
                systemUserTokenProvider);
        person = hentPerson(FNR);
    }

    public HentPerson.Person hentPerson(Fnr fnr) {
        configurePdlResponse("pdl-hentPerson-response.json", fnr.get());
        return pdlClient.hentPerson(fnr, PDL_AUTH);
    }

    public HentPerson.PersonNavn hentPersonNavn(Fnr fnr) {
        configurePdlResponse("pdl-hentPersonNavn-response.json", fnr.get());
        return pdlClient.hentPersonNavn(fnr, PDL_AUTH);
    }

    public HentPerson.VergeOgFullmakt hentVergeOgFullmakt(Fnr fnr) {
        configurePdlResponse("pdl-hentVergeOgFullmakt-response.json", fnr.get());
        return pdlClient.hentVergeOgFullmakt(fnr, PDL_AUTH);
    }

    public HentPerson.GeografiskTilknytning hentGeografisktilknytning(Fnr fnr) {
        configurePdlResponse("pdl-hentGeografiskTilknytning-response.json", "hentGeografiskTilknytning");
        return pdlClient.hentGeografiskTilknytning(fnr, PDL_AUTH);
    }

    public HentPerson.Person hentPersonUtenBarnOgSivilstand(Fnr fnr) {
        configurePdlResponse("pdl-hentPersonMedIngenBarn-responsen.json", fnr.get());
        return pdlClient.hentPerson(fnr, PDL_AUTH);
    }

    public HentPerson.Person hentPersonSomErUgift(Fnr fnr) {
        configurePdlResponse("pdl-hentPersonUgift-response.json", fnr.get());
        return pdlClient.hentPerson(fnr, PDL_AUTH);
    }


    public HentPerson.HentSpraakTolk hentTilrettelagtKommunikasjon(Fnr fnr) {
        configurePdlResponse("pdl-hentTilrettelagtKommunikasjon-response.json", fnr.get());
        return pdlClient.hentTilrettelagtKommunikasjon(fnr, PDL_AUTH);
    }

    @Test
    public void hentFamilieRelasjonerSkalHenteForeldreOgBarnRelasjoner() {
        List<HentPerson.ForelderBarnRelasjon> familierelasjoner = person.getForelderBarnRelasjon();

        assertEquals(fnrBarn1, familierelasjoner.get(0).getRelatertPersonsIdent());
        assertEquals("BARN", familierelasjoner.get(0).getRelatertPersonsRolle());

        assertEquals(fnrBarn2, familierelasjoner.get(1).getRelatertPersonsIdent());
        assertEquals("BARN", familierelasjoner.get(1).getRelatertPersonsRolle());
    }

    @Test
    public void hentFnrTilBarnaTest() {
        List<HentPerson.ForelderBarnRelasjon> familierelasjoner = person.getForelderBarnRelasjon();
        List<Fnr> fnrListe = personV2Service.hentBarnaFnr(familierelasjoner);

        assertEquals(2, fnrListe.size());

        for (int i = 0; i < testFnrsTilBarna.size(); i++) {
            assertEquals(testFnrsTilBarna.get(i), fnrListe.get(i));
        }
    }

    @Test
    public void hentOpplysningerTilBarnaMedKodeOkFraPdlTest() {
        configurePdlResponse("pdl-hentPersonBolkRelatertVedSivilstand-response.json", fnrRelatertSivilstand);
        configurePdlResponse("pdl-hentPersonBolk-response.json", fnrBarn1, fnrBarn2);
        hentGeografisktilknytning(FNR); // Må ha med fnr fordi dette flettes
        List<Familiemedlem> barn = personV2Service.hentFlettetPerson(FNR).getBarn();

        assertEquals(1, barn.size());
    }

    @Test
    public void hentDiskresjonsKodeTilAdressebeskyttetPersonTest() {
        HentPerson.Adressebeskyttelse adressebeskyttelse = PersonV2DataMapper.getFirstElement(person.getAdressebeskyttelse());
        String gradering = adressebeskyttelse.getGradering();
        String diskresjonskode = Diskresjonskode.mapKodeTilTall(gradering);

        assertEquals(Diskresjonskode.UGRADERT.toString(), gradering);
        assertEquals(null, diskresjonskode);

        String kode6Bruker = "STRENGT_FORTROLIG";
        assertEquals("6", Diskresjonskode.mapKodeTilTall(kode6Bruker));

        String kode7Bruker = "FORTROLIG";
        assertEquals("7", Diskresjonskode.mapKodeTilTall(kode7Bruker));
    }

    @Test
    public void hentNavnTest() {
        HentPerson.Navn navn = person.getNavn().get(0);

        assertEquals("NATURLIG", navn.getFornavn());
        assertEquals("GLITRENDE", navn.getMellomnavn());
        assertEquals("STAFFELI", navn.getEtternavn());
        assertEquals("NATURLIG STAFFELI", navn.getForkortetNavn());
    }

    @Test
    public void getFirstElementFraListeTest() {
        String identStatus = ofNullable(PersonV2DataMapper.getFirstElement(person.getFolkeregisteridentifikator())).map(HentPerson.Folkeregisteridentifikator::getStatus).orElse(null);
        assertEquals("I_BRUK", identStatus);

        String kjoenn = ofNullable(PersonV2DataMapper.getFirstElement(person.getKjoenn())).map(HentPerson.Kjoenn::getKjoenn).orElse(null);
        assertNull(kjoenn);
    }

    @Test
    public void hentGeografiskTilknytningTest() {
        HentPerson.GeografiskTilknytning geografiskTilknytning = hentGeografisktilknytning(FNR);

        assertEquals("0570", geografiskTilknytning.getGtKommune());
        assertEquals("OSLO", geografiskTilknytning.getGtBydel());
        assertEquals("NORGE", geografiskTilknytning.getGtLand());
    }

    @Test
    public void getLandKodeFraKontaktadresseTest() {
        Kontaktadresse.UtenlandskAdresseIFrittFormat midlertidigAdresseUtland = person.getKontaktadresse().get(0).getUtenlandskAdresseIFrittFormat();
        Optional<String> landkode = ofNullable(midlertidigAdresseUtland).map(Kontaktadresse.UtenlandskAdresseIFrittFormat::getLandkode);

        assertEquals(landkode.get(), "FRA");

        Kontaktadresse.UtenlandskAdresseIFrittFormat nullMidlertidigAdresseUtland = null;
        Optional<String> nullLandkode = ofNullable(nullMidlertidigAdresseUtland).map(Kontaktadresse.UtenlandskAdresseIFrittFormat::getLandkode);

        assertTrue(nullLandkode.isEmpty());
    }

    @Test
    public void getPostnummerFraBostedsadresseTest() {
        Bostedsadresse bostedsadresse = person.getBostedsadresse().get(0);
        Optional<String> postnummer = ofNullable(bostedsadresse).map(Bostedsadresse::getVegadresse).map(Adresse.Vegadresse::getPostnummer);

        assertEquals("0560", postnummer.get());

        Bostedsadresse nullBostedsadresse = null;
        Optional<String> nullPostnummer = ofNullable(nullBostedsadresse).map(Bostedsadresse::getVegadresse).map(Adresse.Vegadresse::getPostnummer);

        assertTrue(nullPostnummer.isEmpty());
    }

    @Test
    public void flettSivilstandOgBarnInformasjonTest() {
        configurePdlResponse("pdl-hentPersonBolkRelatertVedSivilstand-response.json", fnrRelatertSivilstand);
        configurePdlResponse("pdl-hentPersonBolk-response.json", fnrBarn1, fnrBarn2);
        PersonV2Data personV2Data = new PersonV2Data();

        assertEquals(0, personV2Data.getBarn().size());
        assertNull(personV2Data.getSivilstandliste());

        personV2Service.flettBarn(person.getForelderBarnRelasjon(), personV2Data); // Forsøker å flette person med 3 barn hvor informasjonen til bare 1 barn er tilgjengelig i PDL
        personV2Service.flettSivilstand(person.getSivilstand(), personV2Data);

        assertEquals(1, personV2Data.getBarn().size()); // Fant bare 1 av 3 barna med "ok"(gyldig) status fra hentPersonBolk operasjonen
        assertNotNull(personV2Data.getSivilstandliste());
    }

    @Test
    public void flettSivilstandOgBarnInfoNarPersonHarIngenSivilstandEllerBarn() {
        PersonV2Data personV2Data = new PersonV2Data();

        assertEquals(0, personV2Data.getBarn().size());
        assertNull(personV2Data.getSivilstandliste());

        person = hentPersonUtenBarnOgSivilstand(FNR);
        personV2Service.flettBarn(person.getForelderBarnRelasjon(), personV2Data);
        personV2Service.flettSivilstand(person.getSivilstand(), personV2Data);

        assertEquals(Collections.emptyList(), personV2Data.getSivilstandliste());
        assertEquals(Collections.emptyList(), personV2Data.getBarn());
    }

    @Test
    public void flettSivilstandinfoSomErEgenAnsattMedOgUtenGraderingTest() {
        configurePdlResponse("pdl-hentPersonBolkRelatertVedSivilstand-response.json", fnrRelatertSivilstand);
        PersonV2Data personV2Data = new PersonV2Data();
        person = hentPerson(FNR);

        // flett sivilstandinfo når relatert person ikke har gradering/adressebeskyttelse
        when(skjermetClient.hentSkjermet(Fnr.of(fnrRelatertSivilstand))).thenReturn(true);
        when(authService.harLesetilgang(Fnr.of(fnrRelatertSivilstand))).thenReturn(false);
        personV2Service.flettSivilstand(person.getSivilstand(), personV2Data);

        Sivilstand sivilstand = personV2Data.getSivilstandliste().get(0);
        assertTrue(sivilstand.getSkjermet());
        assertNotNull(sivilstand.getRelasjonsBosted());
        assertNull(sivilstand.getGradering());

        // flett partnerinfo når relatert person har gradering/adressebeskyttelse
        when(skjermetClient.hentSkjermet(Fnr.of(fnrRelatertSivilstand))).thenReturn(true);
        when(authService.harLesetilgang(Fnr.of(fnrRelatertSivilstand))).thenReturn(false);
        givenThat(
                post(WireMock.urlEqualTo("/graphql"))
                        .withRequestBody(containing(fnrRelatertSivilstand))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withBody(TestUtils.readTestResourceFile("pdl-hentPersonBolkRelatertVedSivilstand-response.json").replace("UGRADERT", "FORTROLIG"))
                        )
        );
        personV2Service.flettSivilstand(person.getSivilstand(), personV2Data);

        sivilstand = personV2Data.getSivilstandliste().get(0);
        assertNull(sivilstand.getSkjermet());
        assertNull(sivilstand.getRelasjonsBosted());
        assertEquals(AdressebeskyttelseGradering.FORTROLIG.name(), sivilstand.getGradering());
    }

    @Test
    public void flettPartnerInfoSomErEgenAnsattTestMedNyttSkjermetAPI_UtenLeseTilgang_SomIkkeErSkjermet() {
        configurePdlResponse("pdl-hentPersonBolkRelatertVedSivilstand-response.json", fnrRelatertSivilstand);
        PersonV2Data personV2Data = new PersonV2Data();
        person = hentPerson(FNR);

        when(skjermetClient.hentSkjermet(Fnr.of(fnrRelatertSivilstand))).thenReturn(false);
        when(authService.harLesetilgang(Fnr.of(fnrRelatertSivilstand))).thenReturn(false);
        personV2Service.flettSivilstand(person.getSivilstand(),personV2Data);

        Sivilstand sivilstand = personV2Data.getSivilstandliste().get(0);
        assertNull(sivilstand.getSkjermet());
        assertNull(sivilstand.getGradering());
        assertEquals(RelasjonsBosted.UKJENT_BOSTED, sivilstand.getRelasjonsBosted());
    }

    @Test
    public void flettPartnerInfoSomErEgenAnsattTestMedNyttSkjermetAPI_UtenLeseTilgang_SomErSkjermet() {
        configurePdlResponse("pdl-hentPersonBolkRelatertVedSivilstand-response.json", fnrRelatertSivilstand);
        person = hentPerson(FNR);
        PersonV2Data personV2Data = PersonV2DataMapper.toPersonV2Data(person);

        when(skjermetClient.hentSkjermet(Fnr.of(fnrRelatertSivilstand))).thenReturn(true);
        when(authService.harLesetilgang(Fnr.of(fnrRelatertSivilstand))).thenReturn(false);
        personV2Service.flettSivilstand(person.getSivilstand(), personV2Data);

        Sivilstand sivilstand = personV2Data.getSivilstandliste().get(0);
        assertTrue(sivilstand.getSkjermet());
        assertNull(sivilstand.getGradering());
        assertEquals(RelasjonsBosted.SAMME_BOSTED, sivilstand.getRelasjonsBosted());
    }

    @Test
    public void flettSivilstandinfoTest_MedLesetilgang() {
        configurePdlResponse("pdl-hentPersonBolkRelatertVedSivilstand-response.json", fnrRelatertSivilstand);
        PersonV2Data personV2Data = new PersonV2Data();
        person = hentPerson(FNR);

        when(skjermetClient.hentSkjermet(Fnr.of(fnrRelatertSivilstand))).thenReturn(false);
        when(authService.harLesetilgang(Fnr.of(fnrRelatertSivilstand))).thenReturn(true);
        personV2Service.flettSivilstand(person.getSivilstand(), personV2Data);

        Sivilstand sivilstand = personV2Data.getSivilstandliste().get(0);
        assertEquals("GIFT", sivilstand.getSivilstand());
        assertEquals(LocalDate.of(2020, 6, 1), sivilstand.getFraDato());
        assertFalse(sivilstand.getSkjermet());
        assertEquals(AdressebeskyttelseGradering.UGRADERT.name(), sivilstand.getGradering());
        assertEquals(RelasjonsBosted.UKJENT_BOSTED, sivilstand.getRelasjonsBosted());
        assertEquals("FREG", sivilstand.getMaster());
        assertEquals(LocalDateTime.parse("2022-04-22T14:51:20"), sivilstand.getRegistrertDato());
    }

    @Test
    public void flettSivilstandUtenRelatertPersonTest() {
        PersonV2Data personV2Data = new PersonV2Data();
        person = hentPersonSomErUgift(Fnr.of("01234567899"));

        personV2Service.flettSivilstand(person.getSivilstand(), personV2Data);
        Sivilstand sivilstand = personV2Data.getSivilstandliste().get(0);

        assertEquals("UGIFT", sivilstand.getSivilstand());
        assertNull(sivilstand.getSkjermet());
        assertNull(sivilstand.getGradering());
        assertNull(sivilstand.getRelasjonsBosted());
    }

    @Test
    public void flettSivilstandMedFlereSivilstanderTest() {
        configurePdlResponse("pdl-hentPersonMedToSivilstander-response.json", "01234567899");
        configurePdlResponse("pdl-hentPersonBolkRelatertVedSivilstand-response.json", "27057612970");
        configurePdlResponse("pdl-hentPersonBolkRelatertVedSivilstand-response.json", "2134567890");
        PersonV2Data personV2Data = new PersonV2Data();
        person = pdlClient.hentPerson(Fnr.of("01234567899"), PDL_AUTH);

        when(authService.harLesetilgang(Fnr.of(fnrRelatertSivilstand))).thenReturn(true);
        personV2Service.flettSivilstand(person.getSivilstand(), personV2Data);
        List<Sivilstand> sivilstands = personV2Data.getSivilstandliste();

        assertEquals("SEPARERT_PARTNER", sivilstands.get(0).getSivilstand());
        assertEquals(LocalDate.of(2018, 2, 26), sivilstands.get(0).getFraDato());
        assertEquals("FREG", sivilstands.get(0).getMaster());

        assertEquals("GIFT", sivilstands.get(1).getSivilstand());
        assertEquals(LocalDate.of(2022, 3, 7), sivilstands.get(1).getFraDato());
        assertEquals(RelasjonsBosted.UKJENT_BOSTED, sivilstands.get(1).getRelasjonsBosted());
        assertEquals("PDL", sivilstands.get(1).getMaster());
    }

    @Test
    public void harFamiliemedlemSammeBostedSomPersonTest() {
        Bostedsadresse personsBostedsAdresse = person.getBostedsadresse().get(0);
        Bostedsadresse familiemedlemsBostedsAdresse = new Bostedsadresse();

        Bostedsadresse.Vegadresse medlemsVegAdresse = new Bostedsadresse.Vegadresse()
                .setMatrikkelId(123456789L)
                .setAdressenavn("ARENDALSGATE")
                .setHusnummer("21")
                .setHusbokstav("B")
                .setKommunenummer("0570")
                .setPostnummer("0560")
                .setPoststed("OSLO")
                .setTilleggsnavn("ARENDAL");
        familiemedlemsBostedsAdresse.setVegadresse(medlemsVegAdresse);
        RelasjonsBosted harSammeBosted = PersonV2DataMapper.erSammeAdresse(familiemedlemsBostedsAdresse, personsBostedsAdresse); // Sammeligner to ulike bostedsadresser

        assertEquals(RelasjonsBosted.ANNET_BOSTED, harSammeBosted);

        medlemsVegAdresse.setHusbokstav("A");
        harSammeBosted = PersonV2DataMapper.erSammeAdresse(familiemedlemsBostedsAdresse, personsBostedsAdresse);  // Sammeligner to like bostedsadresser

        assertEquals(RelasjonsBosted.SAMME_BOSTED, harSammeBosted);
    }

    @Test
    public void telfonNummerMapperTest() {
        List<HentPerson.Telefonnummer> telefonListeFraPdl = person.getTelefonnummer();   //telefonNrFraPdl: landkode: +47 nummer: 33333333
        Telefon telefonNummer = PersonV2DataMapper.telefonNummerMapper(telefonListeFraPdl.get(0));
        assertEquals("+4733333333", telefonNummer.getTelefonNr());
    }

    @Test
    public void leggKrrTelefonNrIListeTest() {
        String telefonNrFraKrr = "+4622222222";
        String registrertDato = "2018-10-01T11:38:22,000+00:00";
        List<Telefon> telefonListeFraPdl = PersonV2DataMapper.mapTelefonNrFraPdl(person.getTelefonnummer());
        personV2Service.leggKrrTelefonNrIListe(telefonNrFraKrr, registrertDato, telefonListeFraPdl);  //Legger telefonnummere fra PDL og KRR som er ulike, til en liste

        assertEquals(2, telefonListeFraPdl.size());
        assertEquals("+4733333333", telefonListeFraPdl.get(0).getTelefonNr());
        assertEquals("+4622222222", telefonListeFraPdl.get(1).getTelefonNr());

        telefonNrFraKrr = "+4733333333";
        personV2Service.leggKrrTelefonNrIListe(telefonNrFraKrr, registrertDato, telefonListeFraPdl); //Legger telefonnummere fra PDL og KRR som er like, til en liste

        assertEquals(2, telefonListeFraPdl.size());
        assertEquals("+4733333333", telefonListeFraPdl.get(0).getTelefonNr());

        telefonNrFraKrr = "+4811111111";
        personV2Service.leggKrrTelefonNrIListe(telefonNrFraKrr, registrertDato, telefonListeFraPdl); //Legger en ny telefonnr fra KRR til en pdlTelefonNrListe

        assertEquals(3, telefonListeFraPdl.size());
        assertEquals("+4811111111", telefonListeFraPdl.get(2).getTelefonNr());
        assertEquals("3", telefonListeFraPdl.get(2).getPrioritet());

        telefonNrFraKrr = null;
        personV2Service.leggKrrTelefonNrIListe(telefonNrFraKrr, registrertDato, telefonListeFraPdl); //Legger telefonnummere fra PDL og KRR til en liste hvor telefonnummer fra KRR er null

        assertEquals(3, telefonListeFraPdl.size());
        assertEquals("+4733333333", telefonListeFraPdl.get(0).getTelefonNr());

        telefonListeFraPdl = new ArrayList<>();
        telefonNrFraKrr = "+4733333333";
        personV2Service.leggKrrTelefonNrIListe(telefonNrFraKrr, registrertDato, telefonListeFraPdl); //Legger telefonnummere fra PDL og KRR til en liste hvor telefonnummer fra PDL er null

        assertEquals(1, telefonListeFraPdl.size());
        assertEquals("+4733333333", telefonListeFraPdl.get(0).getTelefonNr());
    }

    @Test
    public void flettBeskrivelseFraKodeverkTest() {
        mockKodeverk();
        person = pdlClient.hentPerson(Fnr.of("0123456789"), PDL_AUTH);
        PersonV2Data personV2Data = PersonV2DataMapper.toPersonV2Data(person);

        personV2Service.flettKodeverk(personV2Data);
        Bostedsadresse bostedsadresse = personV2Data.getBostedsadresse();
        Oppholdsadresse oppholdsadresse = personV2Data.getOppholdsadresse();
        Kontaktadresse kontaktadresse = personV2Data.getKontaktadresser().get(0);

        assertEquals("POSTSTED", bostedsadresse.getVegadresse().getPoststed());
        assertEquals("KOMMUNE", bostedsadresse.getVegadresse().getKommune());
        assertEquals("LANDKODE", bostedsadresse.getUtenlandskAdresse().getLandkode());

        assertEquals("POSTSTED", oppholdsadresse.getMatrikkeladresse().getPoststed());
        assertEquals("KOMMUNE", oppholdsadresse.getMatrikkeladresse().getKommune());
        assertEquals("LANDKODE", oppholdsadresse.getUtenlandskAdresse().getLandkode());

        assertEquals("POSTSTED", kontaktadresse.getPostadresseIFrittFormat().getPoststed());
        assertEquals("KOMMUNE", kontaktadresse.getVegadresse().getKommune());
        assertEquals("LANDKODE", kontaktadresse.getUtenlandskAdresseIFrittFormat().getLandkode());
    }

    @Test
    public void flettBeskrivelseForFullmaktOmraader() {
        mockKodeverk();
        HentPerson.VergeOgFullmakt vergeOgFullmaktFraPdl = hentVergeOgFullmakt(FNR);
        VergeOgFullmaktData vergeOgFullmaktData = VergeOgFullmaktDataMapper.toVergeOgFullmaktData(vergeOgFullmaktFraPdl);
        personV2Service.flettBeskrivelseForFullmaktOmraader(vergeOgFullmaktData);

        List<VergeOgFullmaktData.Omraade> omraader = vergeOgFullmaktData.getFullmakt().get(0).getOmraader();

        assertEquals("AAP", omraader.get(0).getKode());
        assertEquals("DAG", omraader.get(1).getKode());

        assertEquals("Arbeidsavklaringpenger", omraader.get(0).getBeskrivelse());
        assertEquals("Dagpenger", omraader.get(1).getBeskrivelse());

        List<VergeOgFullmaktData.Omraade> omraade = vergeOgFullmaktData.getFullmakt().get(1).getOmraader();

        assertEquals("*", omraade.get(0).getKode());
        assertEquals("alle ytelser", omraade.get(0).getBeskrivelse());
    }

    @Test
    public void flettMotpartsPersonNavnTilFullmakt() {
        configurePdlResponse("pdl-hentPersonNavn-response.json", "motpartsPersonident1");
        configurePdlResponse("pdl-hentPersonNavn-response.json", "motpartsPersonident2");
        HentPerson.VergeOgFullmakt vergeOgFullmaktFraPdl = hentVergeOgFullmakt(FNR);
        VergeOgFullmaktData vergeOgFullmaktData = VergeOgFullmaktDataMapper.toVergeOgFullmaktData(vergeOgFullmaktFraPdl);
        personV2Service.flettMotpartsPersonNavnTilFullmakt(vergeOgFullmaktData, PDL_AUTH);

        List<VergeOgFullmaktData.Fullmakt> fullmaktListe =  vergeOgFullmaktData.getFullmakt();

        assertEquals("NORDMANN OLA", fullmaktListe.get(0).getMotpartsPersonNavn().getForkortetNavn());
    }

    @Test
    public void personNavnMapperTest() {
        PersonNavnV2 navn = PersonV2DataMapper.navnMapper(hentPersonNavn(FNR).getNavn());
        assertEquals("OLA", navn.getFornavn());
        assertEquals("NORDMANN", navn.getEtternavn());
        assertEquals("NORDMANN OLA", navn.getForkortetNavn());
    }

    @Test
    public void hentSpraakTolkInfoTest() {
        mockKodeverk();
        hentTilrettelagtKommunikasjon(FNR);
        TilrettelagtKommunikasjonData tilrettelagtKommunikasjonData = personV2Service.hentSpraakTolkInfo(FNR);
        assertEquals("Engelsk", tilrettelagtKommunikasjonData.getTalespraak());
        assertEquals("Norsk", tilrettelagtKommunikasjonData.getTegnspraak());
    }

    @Test
    public void hentSikkerhetstiltakTest() {
        person = pdlClient.hentPerson(Fnr.of("0123456789"), PDL_AUTH);
        PersonV2Data personV2Data = PersonV2DataMapper.toPersonV2Data(person);

        assertEquals("Fysisk utestengelse", personV2Data.getSikkerhetstiltak());
    }

    @Test
    public void filtrerGjeldendeEndringsInfoTest() {
        List<HentPerson.Metadata.Endringer> endringerListe = person.getTelefonnummer().get(0).getMetadata().getEndringer();

        Optional<HentPerson.Metadata.Endringer> filtrertEndringer = PersonV2DataMapper.finnForsteEndring(endringerListe);
        assertEquals(Optional.of("OPPRETT"), filtrertEndringer.map(HentPerson.Metadata.Endringer::getType));
    }

    @Test
    public void parseDateFromDateTimeTest() {
        String telefonRegistrertDatoIKrr = "2018-09-01T11:38:22,000+00:00";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss,000+00:00");
        LocalDateTime dateTime = LocalDateTime.parse(telefonRegistrertDatoIKrr, dateTimeFormatter);
        String registrertDato = PersonV2DataMapper.formateDateFromLocalDateTime(dateTime);
        assertEquals("01.09.2018", registrertDato);

        LocalDateTime telefonRegistrertDatoIPdl = person.getTelefonnummer().get(0).getMetadata().getEndringer().get(0).getRegistrert();

        LocalDateTime dateTime1 = LocalDateTime.parse(telefonRegistrertDatoIPdl.toString(), ISO_LOCAL_DATE_TIME);
        String registrertDato1 = PersonV2DataMapper.formateDateFromLocalDateTime(dateTime1);
        assertEquals("08.09.2021", registrertDato1);
    }

    private void mockKodeverk() {
        when(kodeverkService.getPoststedForPostnummer(any())).thenReturn("POSTSTED");
        when(kodeverkService.getBeskrivelseForLandkode(any())).thenReturn("LANDKODE");
        when(kodeverkService.getBeskrivelseForKommunenummer(any())).thenReturn("KOMMUNE");
        when(kodeverkService.getBeskrivelseForSpraakKode("EN")).thenReturn("Engelsk");
        when(kodeverkService.getBeskrivelseForSpraakKode("NO")).thenReturn("Norsk");
        when(kodeverkService.getBeskrivelseForTema("AAP")).thenReturn("Arbeidsavklaringpenger");
        when(kodeverkService.getBeskrivelseForTema("DAG")).thenReturn("Dagpenger");
    }
}
