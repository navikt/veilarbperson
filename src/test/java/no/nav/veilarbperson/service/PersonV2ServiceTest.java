package no.nav.veilarbperson.service;

import no.nav.common.client.norg2.Enhet;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.featuretoggle.UnleashClient;
import no.nav.common.sts.SystemUserTokenProvider;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.dkif.DkifClient;
import no.nav.veilarbperson.client.dkif.DkifKontaktinfo;
import no.nav.veilarbperson.client.egenansatt.EgenAnsattClient;
import no.nav.veilarbperson.client.nom.SkjermetClient;
import no.nav.veilarbperson.client.pdl.HentPerson;
import no.nav.veilarbperson.client.pdl.PdlAuth;
import no.nav.veilarbperson.client.pdl.PdlClient;
import no.nav.veilarbperson.client.pdl.PdlClientImpl;
import no.nav.veilarbperson.client.pdl.domain.*;
import no.nav.veilarbperson.client.person.PersonClient;
import no.nav.veilarbperson.client.veilarbportefolje.VeilarbportefoljeClient;
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
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Optional.ofNullable;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PersonV2ServiceTest extends PdlClientTestConfig {
    private Norg2Client norg2Client = mock(Norg2Client.class);
    private DkifClient dkifClient = mock(DkifClient.class);
    private PersonClient personClient = mock(PersonClient.class);
    private PdlClient pdlClient = mock(PdlClient.class);
    private EgenAnsattClient egenAnsattClient = mock(EgenAnsattClient.class);
    private KodeverkService kodeverkService = mock(KodeverkService.class);
    private SkjermetClient skjermetClient = mock(SkjermetClient.class);
    private VeilarbportefoljeClient veilarbportefoljeClient = mock(VeilarbportefoljeClient.class);
    private UnleashClient unleashClient = mock(UnleashClient.class);
    private AuthService authService = mock(AuthService.class);
    private SystemUserTokenProvider systemUserTokenProvider = mock(SystemUserTokenProvider.class);
    private PersonV2Service personV2Service;
    private HentPerson.Person person;

    private static final PdlAuth PDL_AUTH = new PdlAuth("USER_TOKEN", Optional.of("SYSTEM_TOKEN"));
    private static Fnr FNR = TestUtils.fodselsnummerForDato("1980-01-01");
    List<Fnr> testFnrsTilBarna = new ArrayList<>(List.of(Fnr.of("12345678910"), Fnr.of("12345678911")));

    @Before
    public void setup() {
        when(systemUserTokenProvider.getSystemUserToken()).thenReturn("SYSTEM_USER_TOKEN");
        when(norg2Client.hentTilhorendeEnhet(anyString())).thenReturn(new Enhet());
        when(dkifClient.hentKontaktInfo(any())).thenReturn(new DkifKontaktinfo());
        when(personClient.hentSikkerhetstiltak(any())).thenReturn(null);
        when(egenAnsattClient.erEgenAnsatt(any())).thenReturn(false);
        when(unleashClient.isEnabled(any())).thenReturn(false);
        when(pdlClient.hentPersonBolk(any(), any())).thenReturn(hentPersonBolk(testFnrsTilBarna));
        when(pdlClient.hentPersonNavn(any(), any())).thenReturn(hentPersonNavn(FNR));
        when(kodeverkService.getPoststedForPostnummer(any())).thenReturn("POSTSTED");
        when(kodeverkService.getBeskrivelseForLandkode(any())).thenReturn("LANDKODE");
        when(kodeverkService.getBeskrivelseForKommunenummer(any())).thenReturn("KOMMUNE");
        when(kodeverkService.getBeskrivelseForSpraakKode("EN")).thenReturn("Engelsk");
        when(kodeverkService.getBeskrivelseForSpraakKode("NO")).thenReturn("Norsk");
        when(kodeverkService.getBeskrivelseForTema("AAP")).thenReturn("Arbeidsavklaringpenger");
        when(kodeverkService.getBeskrivelseForTema("DAG")).thenReturn("Dagpenger");
        when(pdlClient.hentTilrettelagtKommunikasjon(any(), any())).thenReturn(hentTilrettelagtKommunikasjon(FNR));

        personV2Service = new PersonV2Service(
                pdlClient,
                authService,
                dkifClient,
                norg2Client,
                personClient,
                egenAnsattClient,
                veilarbportefoljeClient,
                unleashClient,
                skjermetClient,
                kodeverkService,
                systemUserTokenProvider);
        person = hentPerson(FNR);
    }

    public HentPerson.Person hentPerson(Fnr fnr) {
        PdlClientImpl pdlClient = configurPdlClient("pdl-hentPerson-response.json");
        return pdlClient.hentPerson(fnr, PDL_AUTH);
    }

    public HentPerson.PersonNavn hentPersonNavn(Fnr fnr) {
        PdlClientImpl pdlClient = configurPdlClient("pdl-hentPersonNavn-response.json");
        return pdlClient.hentPersonNavn(fnr, PDL_AUTH);
    }

    public List<HentPerson.PersonFraBolk> hentPersonBolk(List<Fnr> fnrs) {
        String apiUrl = configurApiResponse("pdl-hentPersonBolk-response.json");
        PdlClientImpl pdlClient = new PdlClientImpl(apiUrl);
        return pdlClient.hentPersonBolk(fnrs, PDL_AUTH);
    }

    public HentPerson.VergeOgFullmakt hentVergeOgFullmakt(Fnr fnr) {
        PdlClientImpl pdlClient = configurPdlClient("pdl-hentVergeOgFullmakt-response.json");
        return pdlClient.hentVergeOgFullmakt(fnr, PDL_AUTH);
    }

    public HentPerson.GeografiskTilknytning hentGeografisktilknytning(Fnr fnr) {
        PdlClientImpl pdlClient = configurPdlClient("pdl-hentGeografiskTilknytning-response.json");
        return pdlClient.hentGeografiskTilknytning(fnr, PDL_AUTH);
    }

    public HentPerson.Person hentFamiliemedlem(Fnr fnr) {
        PdlClientImpl pdlClient = configurPdlClient("pdl-hentPersonMedIngenBarn-responsen.json");
        return pdlClient.hentPerson(fnr, PDL_AUTH);
    }

    public HentPerson.HentSpraakTolk hentTilrettelagtKommunikasjon(Fnr fnr) {
        PdlClientImpl pdlClient = configurPdlClient("pdl-hentTilrettelagtKommunikasjon-response.json");
        return pdlClient.hentTilrettelagtKommunikasjon(fnr, PDL_AUTH);
    }

    @Test
    public void hentFamilieRelasjonerSkalHenteForeldreOgBarnRelasjoner() {
        List<HentPerson.ForelderBarnRelasjon> familierelasjoner = person.getForelderBarnRelasjon();

        assertEquals("12345678910", familierelasjoner.get(0).getRelatertPersonsIdent());
        assertEquals("BARN", familierelasjoner.get(0).getRelatertPersonsRolle());

        assertEquals("12345678911", familierelasjoner.get(1).getRelatertPersonsIdent());
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
        List<HentPerson.PersonFraBolk> hentPersonBolk = hentPersonBolk(testFnrsTilBarna);

        assertEquals(4, hentPersonBolk.size());

        List<HentPerson.PersonFraBolk> filterPersonBolkMedOkStatus = Optional.of(hentPersonBolk).stream().flatMap(Collection::stream)
                                                                        .filter(status -> status.getCode().equals("ok"))
                                                                        .collect(Collectors.toList());

        assertEquals(2, filterPersonBolkMedOkStatus.size());
    }

    @Test
    public void hentDiskresjonsKodeTilAdressebeskyttetPersonTest() {
        HentPerson.Adressebeskyttelse adressebeskyttelse = PersonV2DataMapper.getFirstElement(person.getAdressebeskyttelse());
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
    public void flettPartnerOgBarnInformasjonTest() {
        PersonV2Data personV2Data = getPersonV2Data();

        assertEquals(0, personV2Data.getBarn().size());
        assertNull(personV2Data.getPartner());

        personV2Service.flettPartnerOgBarnInformasjon(person.getSivilstand(), person.getForelderBarnRelasjon(), personV2Data); // Forsøker å flette person med 3 barn hvor informasjonen til bare 1 barn er tilgjemgelig i PDL

        assertEquals(1, personV2Data.getBarn().size()); // Fant bare 1 av 3 barna med "ok"(gyldig) status fra hentPersonBolk operasjonen
        assertNotNull(personV2Data.getPartner());
    }

    @Test
    public void flettPartnerOgBarnInfoNorPersonHarIngenPartnerEllerBarn() {
        PersonV2Data personV2Data = getPersonV2Data();

        assertEquals(0, personV2Data.getBarn().size());
        assertNull(personV2Data.getPartner());

        person = hentFamiliemedlem(Fnr.of("12345678910"));
        personV2Service.flettPartnerOgBarnInformasjon(person.getSivilstand(), person.getForelderBarnRelasjon(), personV2Data);

        assertNull(personV2Data.getPartner());
        assertEquals(Collections.emptyList(), personV2Data.getBarn());
    }

    @Test
    public void flettPartnerInfoSomErEgenAnsattTest() {
        PersonV2Data personV2Data = getPersonV2Data();
        person = hentPerson(FNR);

        //flett partner info når veileder har ikke lese tilgang
        when(egenAnsattClient.erEgenAnsatt(Fnr.of("2134567890"))).thenReturn(true);
        when(authService.harLesetilgang(Fnr.of("2134567890"))).thenReturn(false);
        personV2Service.flettPartnerOgBarnInformasjon(person.getSivilstand(), person.getForelderBarnRelasjon(), personV2Data);

        Familiemedlem partner = personV2Data.getPartner();
        assertNull(partner.getForkortetNavn());
        assertEquals("MANN", partner.getKjonn());
        assertEquals("2134567890", partner.getFodselsnummer().toString());
        assertEquals(LocalDate.of(1982,12,14), partner.getFodselsdato());

        //flett partner info når veileder har lese tilgang
        when(egenAnsattClient.erEgenAnsatt(Fnr.of("2134567890"))).thenReturn(true);
        when(authService.harLesetilgang(Fnr.of("2134567890"))).thenReturn(true);
        personV2Service.flettPartnerOgBarnInformasjon(person.getSivilstand(), person.getForelderBarnRelasjon(), personV2Data);

        Familiemedlem partner1 = personV2Data.getPartner();
        assertNotNull(partner1.getForkortetNavn());
        assertNotNull(partner1.getKjonn());
        assertEquals("2134567890", partner1.getFodselsnummer().toString());
        assertEquals(LocalDate.of(1982,12,14), partner1.getFodselsdato());
    }

    @Test
    public void flettPartnerInfoSomErEgenAnsattTestMedNyttAPI_UtenLeseTilgang() {
        when(unleashClient.isEnabled(any())).thenReturn(true);
        PersonV2Data personV2Data = getPersonV2Data();
        person = hentPerson(FNR);

        when(skjermetClient.hentSkjermet(Fnr.of("2134567890"))).thenReturn(true);
        when(authService.harLesetilgang(Fnr.of("2134567890"))).thenReturn(false);
        personV2Service.flettPartnerOgBarnInformasjon(person.getSivilstand(), person.getForelderBarnRelasjon(), personV2Data);

        Familiemedlem partner = personV2Data.getPartner();
        assertNull(partner.getFornavn());
        assertNull(partner.getEtternavn());
        assertNull(partner.getForkortetNavn());
        assertEquals("MANN", partner.getKjonn());
        assertEquals("2134567890", partner.getFodselsnummer().toString());
        assertEquals(LocalDate.of(1982, 12, 14), partner.getFodselsdato());
    }

    @Test
    public void flettPartnerInfoSomErEgenAnsattTestMedNyttAPI_UtenLeseTilgang_SomErEgenAnsatt() {
        Fnr partnersFnr = Fnr.of("2134567890");
        when(unleashClient.isEnabled(any())).thenReturn(true);
        PersonV2Data personV2Data = getPersonV2Data();
        person = hentPerson(FNR);

        when(skjermetClient.hentSkjermet(partnersFnr)).thenReturn(true);
        when(authService.harLesetilgang(partnersFnr)).thenReturn(false);

        personV2Service.flettPartnerOgBarnInformasjon(person.getSivilstand(), person.getForelderBarnRelasjon(), personV2Data);

        Familiemedlem partner = personV2Data.getPartner();

        assertNull(partner.getFornavn());
        assertNull(partner.getEtternavn());
        assertNull(partner.getForkortetNavn());
        assertEquals("MANN", partner.getKjonn());
        assertEquals("2134567890", partner.getFodselsnummer().toString());
        assertEquals(LocalDate.of(1982, 12, 14), partner.getFodselsdato());
    }

    @Test
    public void flettPartnerInfoTestMedNyttAPI_MedLeseTilgang() {
        when(unleashClient.isEnabled(any())).thenReturn(true);
        PersonV2Data personV2Data = getPersonV2Data();
        person = hentPerson(FNR);

        when(skjermetClient.hentSkjermet(Fnr.of("2134567890"))).thenReturn(false);
        when(authService.harLesetilgang(Fnr.of("2134567890"))).thenReturn(true);
        personV2Service.flettPartnerOgBarnInformasjon(person.getSivilstand(), person.getForelderBarnRelasjon(), personV2Data);

        Familiemedlem partner = personV2Data.getPartner();

        assertNotNull(partner.getFornavn());
        assertNotNull(partner.getEtternavn());
        assertNotNull(partner.getForkortetNavn());
        assertEquals("MANN", partner.getKjonn());
        assertEquals("2134567890", partner.getFodselsnummer().toString());
        assertEquals(LocalDate.of(1982, 12, 14), partner.getFodselsdato());
    }

    @Test
    public void harFamiliamedlemSammeBostedSomPersonTest() {
        Bostedsadresse personsBostedsAdresse = person.getBostedsadresse().get(0);
        Bostedsadresse familiemedlemsBostedsAdresse = new Bostedsadresse();

        Bostedsadresse.Vegadresse medlemsVegAdresse = new Bostedsadresse.Vegadresse()
                .setMatrikkelId(123L).setAdressenavn("ARENDALSGATE").setHusbokstav("A").setHusnummer("21").setKommunenummer("0570").setPostnummer("0560").setPoststed("OSLO").setTilleggsnavn("ARENDAL");
        familiemedlemsBostedsAdresse.setVegadresse(medlemsVegAdresse);

        boolean harSammeBbosted = PersonV2DataMapper.harFamiliamedlemSammeBostedSomPerson(familiemedlemsBostedsAdresse, personsBostedsAdresse); // Sammeligner to ulike bostedsadresser

        assertFalse(harSammeBbosted);

        familiemedlemsBostedsAdresse = hentFamiliemedlem(Fnr.of("12345678910")).getBostedsadresse().get(0);
        harSammeBbosted = PersonV2DataMapper.harFamiliamedlemSammeBostedSomPerson(familiemedlemsBostedsAdresse, personsBostedsAdresse);  // Sammeligner to like bostedsadresser

        assertTrue(harSammeBbosted);
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
        PersonV2Data personV2Data = getPersonV2Data();
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
        TilrettelagtKommunikasjonData tilrettelagtKommunikasjonData = personV2Service.hentSpraakTolkInfo(FNR);
        assertEquals("Engelsk", tilrettelagtKommunikasjonData.getTalespraak());
        assertEquals("Norsk", tilrettelagtKommunikasjonData.getTegnspraak());
    }


    @Test
    public void filtrerGjelendeEndringsInfoTest() {
        List<HentPerson.Metadata.Endringer> endringerListe = person.getTelefonnummer().get(0).getMetadata().getEndringer();

        HentPerson.Metadata.Endringer filtrertEndringer = PersonV2DataMapper.filtrerGjelendeEndringsInfo(endringerListe);
        assertEquals("OPPRETT", filtrertEndringer.getType());
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

    public PersonV2Data getPersonV2Data() {
        PersonV2Data personV2Data = new PersonV2Data();
        Bostedsadresse personsBostedsAdresse = PersonV2DataMapper.getFirstElement(person.getBostedsadresse());
        Oppholdsadresse personsOppholdsadresse = PersonV2DataMapper.getFirstElement(person.getOppholdsadresse());
        List<Kontaktadresse> personsKontaktsadresse = person.getKontaktadresse();
        Telefon telefon = new Telefon().setPrioritet("1").setTelefonNr("+4733333333").setMaster("PDL");

        personV2Data.setBostedsadresse(personsBostedsAdresse);
        personV2Data.setOppholdsadresse(personsOppholdsadresse);
        personV2Data.setKontaktadresser(personsKontaktsadresse);
        personV2Data.setTelefon(new ArrayList<>(List.of(telefon)));

        return personV2Data;
    }
}
