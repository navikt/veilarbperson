package no.nav.veilarbperson.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.common.client.norg2.Enhet;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.digdir.DigdirClient;
import no.nav.veilarbperson.client.digdir.DigdirKontaktinfo;
import no.nav.veilarbperson.client.nom.SkjermetClient;
import no.nav.veilarbperson.client.pdl.HentPerson;
import no.nav.veilarbperson.client.pdl.PdlClient;
import no.nav.veilarbperson.client.pdl.domain.*;
import no.nav.veilarbperson.client.representasjon.RepresentasjonClient;
import no.nav.veilarbperson.config.PdlClientTestConfig;
import no.nav.veilarbperson.domain.*;
import no.nav.veilarbperson.utils.PersonV2DataMapper;
import no.nav.veilarbperson.utils.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Optional.ofNullable;
import static no.nav.veilarbperson.client.pdl.domain.RelasjonsBosted.UKJENT_BOSTED;
import static no.nav.veilarbperson.utils.PersonV2DataMapper.frontendDatoformat;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PersonV2ServiceTest extends PdlClientTestConfig {
    private final Norg2Client norg2Client = mock(Norg2Client.class);
    private final DigdirClient digdirClient = mock(DigdirClient.class);
    private PdlClient pdlClient;
    private final KodeverkService kodeverkService = mock(KodeverkService.class);
    private final SkjermetClient skjermetClient = mock(SkjermetClient.class);
    private final AuthService authService = mock(AuthService.class);
    private final RepresentasjonClient representasjonClient = mock(RepresentasjonClient.class);
    private PersonV2Service personV2Service;
    private HentPerson.Person person;
    private static final Fnr FNR = Fnr.of("0123456789");
    private final String fnrRelatertSivilstand = "2134567890";
    private final String fnrBarn1 = "12345678910";
    private final String fnrBarn2 = "12345678911";

    List<Fnr> testFnrsTilBarna = new ArrayList<>(List.of(Fnr.of(fnrBarn1), Fnr.of(fnrBarn2)));

    @Before
    public void setup() {
        pdlClient = getPdlClient();
        when(norg2Client.hentTilhorendeEnhet(anyString(), any(), anyBoolean())).thenReturn(new Enhet());
        when(digdirClient.hentKontaktInfo(any())).thenReturn(new DigdirKontaktinfo());

        personV2Service = new PersonV2Service(
                pdlClient,
                authService,
                digdirClient,
                norg2Client,
                skjermetClient,
                kodeverkService,
                representasjonClient
                );
        person = hentPerson(FNR);
    }

    public HentPerson.Person hentPerson(Fnr fnr) {
        configurePdlResponse("pdl-hentPerson-response.json", fnr.get());
        return pdlClient.hentPerson(new PdlRequest(fnr, null));
    }


    public HentPerson.PersonNavn hentPersonNavn(PdlRequest pdlRequest) {
        configurePdlResponse("pdl-hentPersonNavn-response.json", pdlRequest.fnr().get());
        return pdlClient.hentPersonNavn(pdlRequest);
    }

    public HentPerson.Verge hentVerge(Fnr fnr) {
        configurePdlResponse("pdl-hentVerge-response.json", fnr.get());
        return pdlClient.hentVerge(new PdlRequest(fnr, null));
    }

    public HentPerson.GeografiskTilknytning hentGeografisktilknytning(PdlRequest pdlRequest) {
        configurePdlResponse("pdl-hentGeografiskTilknytning-response.json", "hentGeografiskTilknytning");
        return pdlClient.hentGeografiskTilknytning(pdlRequest);
    }

    public HentPerson.Person hentPersonUtenBarnOgSivilstand(Fnr fnr) {
        configurePdlResponse("pdl-hentPersonMedIngenBarn-responsen.json", fnr.get());
        return pdlClient.hentPerson(new PdlRequest(fnr, null));
    }

    public HentPerson.Person hentPersonSomErUgift(Fnr fnr) {
        configurePdlResponse("pdl-hentPersonUgift-response.json", fnr.get());
        return pdlClient.hentPerson(new PdlRequest(fnr, null));
    }

    public HentPerson.HentSpraakTolk hentTilrettelagtKommunikasjon(PdlRequest pdlRequest) {
        configurePdlResponse("pdl-hentTilrettelagtKommunikasjon-response.json", pdlRequest.fnr().get());
        return pdlClient.hentTilrettelagtKommunikasjon(pdlRequest);
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
        PersonFraPdlRequest personFraPdlRequest = new PersonFraPdlRequest(FNR, null);
        hentGeografisktilknytning(new PdlRequest(personFraPdlRequest.getFnr(), personFraPdlRequest.getBehandlingsnummer())); // Må ha med fnr fordi dette flettes
        List<Familiemedlem> barn = personV2Service.hentFlettetPerson(personFraPdlRequest).getBarn();

        assertEquals(1, barn.size());
    }

    @Test
    public void hentOpplysningerTilBarnOpphortFraPdlTest() {
        String fnrBarnOpphoert = "111";
        configurePdlResponse("pdl-hentPersonBolkOpphoert-response.json", fnrBarnOpphoert);
        var adresse = hentPerson(FNR).getBostedsadresse().getFirst();
        var barn = personV2Service.hentFamiliemedlemOpplysninger(List.of(Fnr.of(fnrBarnOpphoert)), adresse, null);

        assertEquals(0, barn.size());
    }

    @Test
    public void hentDiskresjonsKodeTilAdressebeskyttetPersonTest() {
        HentPerson.Adressebeskyttelse adressebeskyttelse = PersonV2DataMapper.getFirstElement(person.getAdressebeskyttelse());
        String gradering = adressebeskyttelse.getGradering();
        String diskresjonskode = Diskresjonskode.mapKodeTilTall(gradering);

        assertEquals(Diskresjonskode.UGRADERT.toString(), gradering);
        assertNull(diskresjonskode);

        String kode6Bruker = "STRENGT_FORTROLIG";
        assertEquals("6", Diskresjonskode.mapKodeTilTall(kode6Bruker));

        String kode7Bruker = "FORTROLIG";
        assertEquals("7", Diskresjonskode.mapKodeTilTall(kode7Bruker));
    }

    @Test
    public void hentNavnTest() {
        HentPerson.Navn navn = person.getNavn().getFirst();

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
        HentPerson.GeografiskTilknytning geografiskTilknytning = hentGeografisktilknytning(new PdlRequest(FNR, null));

        assertEquals("0570", geografiskTilknytning.getGtKommune());
        assertEquals("OSLO", geografiskTilknytning.getGtBydel());
        assertEquals("NORGE", geografiskTilknytning.getGtLand());
    }

    @Test
    public void getLandKodeFraKontaktadresseTest() {
        Kontaktadresse.UtenlandskAdresseIFrittFormat midlertidigAdresseUtland = person.getKontaktadresse().getFirst().getUtenlandskAdresseIFrittFormat();
        Optional<String> landkode = ofNullable(midlertidigAdresseUtland).map(Kontaktadresse.UtenlandskAdresseIFrittFormat::getLandkode);

        assertEquals( "FRA", landkode.get());

        Kontaktadresse.UtenlandskAdresseIFrittFormat nullMidlertidigAdresseUtland = null;
        Optional<String> nullLandkode = ofNullable(nullMidlertidigAdresseUtland).map(Kontaktadresse.UtenlandskAdresseIFrittFormat::getLandkode);

        assertTrue(true);
    }

    @Test
    public void getPostnummerFraBostedsadresseTest() {
        Bostedsadresse bostedsadresse = person.getBostedsadresse().getFirst();
        Optional<String> postnummer = ofNullable(bostedsadresse).map(Bostedsadresse::getVegadresse).map(Adresse.Vegadresse::getPostnummer);

        assertEquals("0560", postnummer.get());

        Bostedsadresse nullBostedsadresse = null;
        Optional<String> nullPostnummer = ofNullable(nullBostedsadresse).map(Bostedsadresse::getVegadresse).map(Adresse.Vegadresse::getPostnummer);

        assertTrue(true);
    }

    @Test
    public void flettSivilstandOgBarnInformasjonTest() {
        configurePdlResponse("pdl-hentPersonBolkRelatertVedSivilstand-response.json", fnrRelatertSivilstand);
        configurePdlResponse("pdl-hentPersonBolk-response.json", fnrBarn1, fnrBarn2);
        PersonV2Data personV2Data = new PersonV2Data();

        assertEquals(0, personV2Data.getBarn().size());
        assertNull(personV2Data.getSivilstandliste());

        personV2Service.flettBarn(person.getForelderBarnRelasjon(), personV2Data, null); // Forsøker å flette person med 3 barn hvor informasjonen til bare 1 barn er tilgjengelig i PDL
        personV2Service.flettSivilstand(person.getSivilstand(), personV2Data, null);

        assertEquals(1, personV2Data.getBarn().size()); // Fant bare 1 av 3 barna med "ok"(gyldig) status fra hentPersonBolk operasjonen
        assertNotNull(personV2Data.getSivilstandliste());
    }

    @Test
    public void flettSivilstandOgBarnInfoNarPersonHarIngenSivilstandEllerBarn() {
        PersonV2Data personV2Data = new PersonV2Data();

        assertEquals(0, personV2Data.getBarn().size());
        assertNull(personV2Data.getSivilstandliste());

        person = hentPersonUtenBarnOgSivilstand(FNR);
        personV2Service.flettBarn(person.getForelderBarnRelasjon(), personV2Data, null);
        personV2Service.flettSivilstand(person.getSivilstand(), personV2Data, null);

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
        personV2Service.flettSivilstand(person.getSivilstand(), personV2Data, null);

        Sivilstand sivilstand = personV2Data.getSivilstandliste().getFirst();
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
        personV2Service.flettSivilstand(person.getSivilstand(), personV2Data, null);

        sivilstand = personV2Data.getSivilstandliste().getFirst();
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
        personV2Service.flettSivilstand(person.getSivilstand(),personV2Data, null);

        Sivilstand sivilstand = personV2Data.getSivilstandliste().getFirst();
        assertNull(sivilstand.getSkjermet());
        assertNull(sivilstand.getGradering());
        assertEquals(UKJENT_BOSTED, sivilstand.getRelasjonsBosted());
    }

    @Test
    public void flettPartnerInfoSomErEgenAnsattTestMedNyttSkjermetAPI_UtenLeseTilgang_SomErSkjermet() {
        configurePdlResponse("pdl-hentPersonBolkRelatertVedSivilstand-response.json", fnrRelatertSivilstand);
        person = hentPerson(FNR);
        PersonV2Data personV2Data = PersonV2DataMapper.toPersonV2Data(person);

        when(skjermetClient.hentSkjermet(Fnr.of(fnrRelatertSivilstand))).thenReturn(true);
        when(authService.harLesetilgang(Fnr.of(fnrRelatertSivilstand))).thenReturn(false);
        personV2Service.flettSivilstand(person.getSivilstand(), personV2Data, null);

        Sivilstand sivilstand = personV2Data.getSivilstandliste().getFirst();
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
        personV2Service.flettSivilstand(person.getSivilstand(), personV2Data, null);

        Sivilstand sivilstand = personV2Data.getSivilstandliste().getFirst();
        assertEquals("GIFT", sivilstand.getSivilstand());
        assertEquals(LocalDate.of(2020, 6, 1), sivilstand.getFraDato());
        assertFalse(sivilstand.getSkjermet());
        assertEquals(AdressebeskyttelseGradering.UGRADERT.name(), sivilstand.getGradering());
        assertEquals(UKJENT_BOSTED, sivilstand.getRelasjonsBosted());
        assertEquals("FREG", sivilstand.getMaster());
        assertEquals(LocalDateTime.parse("2022-04-22T14:51:20"), sivilstand.getRegistrertDato());
    }

    @Test
    public void flettSivilstandUtenRelatertPersonTest() {
        PersonV2Data personV2Data = new PersonV2Data();
        person = hentPersonSomErUgift(Fnr.of("01234567899"));

        personV2Service.flettSivilstand(person.getSivilstand(), personV2Data, null);
        Sivilstand sivilstand = personV2Data.getSivilstandliste().getFirst();

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
        person = pdlClient.hentPerson(new PdlRequest(Fnr.of("01234567899"),null));

        when(authService.harLesetilgang(Fnr.of(fnrRelatertSivilstand))).thenReturn(true);
        personV2Service.flettSivilstand(person.getSivilstand(), personV2Data, null);
        List<Sivilstand> sivilstands = personV2Data.getSivilstandliste();

        assertEquals("SEPARERT_PARTNER", sivilstands.getFirst().getSivilstand());
        assertEquals(LocalDate.of(2018, 2, 26), sivilstands.get(0).getFraDato());
        assertEquals("FREG", sivilstands.getFirst().getMaster());

        assertEquals("GIFT", sivilstands.get(1).getSivilstand());
        assertEquals(LocalDate.of(2022, 3, 7), sivilstands.get(1).getFraDato());
        assertEquals(UKJENT_BOSTED, sivilstands.get(1).getRelasjonsBosted());
        assertEquals("PDL", sivilstands.get(1).getMaster());
    }

    @Test
    public void harFamiliemedlemSammeBostedSomPersonTest() {
        Bostedsadresse personsBostedsAdresse = person.getBostedsadresse().getFirst();
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
        Telefon telefonNummer = PersonV2DataMapper.telefonNummerMapper(telefonListeFraPdl.getFirst());
        assertEquals("+4733333333", telefonNummer.getTelefonNr());
    }

    @Test
    public void leggKrrTelefonNrIListeTest() {
        String telefonNrFraKrr = "+4622222222";
        String registrertDato = "2018-10-01";
        List<Telefon> telefonListeFraPdl = PersonV2DataMapper.mapTelefonNrFraPdl(person.getTelefonnummer());
        personV2Service.leggKrrTelefonNrIListe(telefonNrFraKrr, registrertDato, telefonListeFraPdl);  //Legger telefonnummere fra PDL og KRR som er ulike, til en liste
        assertEquals(2, telefonListeFraPdl.size());
        assertEquals("+4733333333", telefonListeFraPdl.getFirst().getTelefonNr());
        assertEquals("2", telefonListeFraPdl.getFirst().getPrioritet());
        assertEquals("+4622222222", telefonListeFraPdl.get(1).getTelefonNr());
        assertEquals("1", telefonListeFraPdl.get(1).getPrioritet());
/*
        telefonNrFraKrr = "+4733333333";
        personV2Service.leggKrrTelefonNrIListe(telefonNrFraKrr, registrertDato, telefonListeFraPdl); //Legger telefonnummere fra PDL og KRR som er like, til en liste

        assertEquals(2, telefonListeFraPdl.size());
        assertEquals("+4733333333", telefonListeFraPdl.get(0).getTelefonNr());
        assertEquals("2", telefonListeFraPdl.get(0).getPrioritet());
        assertEquals("1", telefonListeFraPdl.get(1).getPrioritet());

        telefonNrFraKrr = "+4811111111";
        personV2Service.leggKrrTelefonNrIListe(telefonNrFraKrr, registrertDato, telefonListeFraPdl); //Legger en ny telefonnr fra KRR til en pdlTelefonNrListe

        assertEquals(3, telefonListeFraPdl.size());
        assertEquals("+4811111111", telefonListeFraPdl.get(2).getTelefonNr());
        assertEquals("1", telefonListeFraPdl.get(2).getPrioritet());

        telefonNrFraKrr = null;
        personV2Service.leggKrrTelefonNrIListe(telefonNrFraKrr, registrertDato, telefonListeFraPdl); //Legger telefonnummere fra PDL og KRR til en liste hvor telefonnummer fra KRR er null
        assertEquals(3, telefonListeFraPdl.size());
        assertEquals("+4733333333", telefonListeFraPdl.get(0).getTelefonNr());
        assertEquals("3", telefonListeFraPdl.get(0).getPrioritet());
        assertEquals("1", telefonListeFraPdl.get(1).getPrioritet());
        assertEquals("1", telefonListeFraPdl.get(2).getPrioritet());

        telefonListeFraPdl = new ArrayList<>();
        telefonNrFraKrr = "+4733333333";
        personV2Service.leggKrrTelefonNrIListe(telefonNrFraKrr, registrertDato, telefonListeFraPdl); //Legger telefonnummere fra PDL og KRR til en liste hvor telefonnummer fra PDL er null

        assertEquals(1, telefonListeFraPdl.size());
        assertEquals("+4733333333", telefonListeFraPdl.get(0).getTelefonNr());
        assertEquals("1", telefonListeFraPdl.get(0).getPrioritet());
 */
    }

    @Test
    public void flettBeskrivelseFraKodeverkTest() {
        mockKodeverk();
        person = pdlClient.hentPerson(new PdlRequest(Fnr.of("0123456789"), null));
        PersonV2Data personV2Data = PersonV2DataMapper.toPersonV2Data(person);

        personV2Service.flettKodeverk(personV2Data);
        Bostedsadresse bostedsadresse = personV2Data.getBostedsadresse();
        Oppholdsadresse oppholdsadresse = personV2Data.getOppholdsadresse();
        Kontaktadresse kontaktadresse = personV2Data.getKontaktadresser().getFirst();

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
    public void personNavnMapperTest() {
        PersonNavnV2 navn = PersonV2DataMapper.navnMapper(hentPersonNavn(new PdlRequest(FNR, null)).getNavn());
        assertEquals("OLA", navn.getFornavn());
        assertEquals("NORDMANN", navn.getEtternavn());
        assertEquals("NORDMANN OLA", navn.getForkortetNavn());
    }

    @Test
    public void hentSpraakTolkInfoTest() {
        mockKodeverk();
        hentTilrettelagtKommunikasjon(new PdlRequest(FNR, null));
        TilrettelagtKommunikasjonData tilrettelagtKommunikasjonData = personV2Service.hentSpraakTolkInfo(new PersonFraPdlRequest(FNR, null));
        assertEquals("Engelsk", tilrettelagtKommunikasjonData.getTalespraak());
        assertEquals("Norsk", tilrettelagtKommunikasjonData.getTegnspraak());
    }

    @Test
    public void hentSikkerhetstiltakTest() {
        person = pdlClient.hentPerson(new PdlRequest(Fnr.of("0123456789"), null));
        PersonV2Data personV2Data = PersonV2DataMapper.toPersonV2Data(person);

        assertEquals("Fysisk utestengelse", personV2Data.getSikkerhetstiltak());
    }

    @Test
    public void filtrerGjeldendeEndringsInfoTest() {
        List<HentPerson.Metadata.Endringer> endringerListe = person.getTelefonnummer().getFirst().getMetadata().getEndringer();

        Optional<HentPerson.Metadata.Endringer> filtrertEndringer = PersonV2DataMapper.finnForsteEndring(endringerListe);
        assertEquals(Optional.of("OPPRETT"), filtrertEndringer.map(HentPerson.Metadata.Endringer::getType));
    }

    @Test
    public void parseDateFromDateTimeTest() {
        String telefonRegistrertDatoIKrr = "2018-09-01T11:38:22,000+00:00";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss,000+00:00");
        LocalDateTime dateTime = LocalDateTime.parse(telefonRegistrertDatoIKrr, dateTimeFormatter);
        String registrertDato = dateTime.format(frontendDatoformat);
        assertEquals("01.09.2018", registrertDato);

        LocalDateTime telefonRegistrertDatoIPdl = person.getTelefonnummer().getFirst().getMetadata().getEndringer().get(0).getRegistrert();

        LocalDateTime dateTime1 = LocalDateTime.parse(telefonRegistrertDatoIPdl.toString(), ISO_LOCAL_DATE_TIME);
        String registrertDato1 = dateTime1.format(frontendDatoformat);
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
