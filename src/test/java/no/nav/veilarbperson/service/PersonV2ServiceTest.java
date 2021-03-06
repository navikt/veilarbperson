package no.nav.veilarbperson.service;

import no.nav.common.client.norg2.Enhet;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.dkif.DkifClient;
import no.nav.veilarbperson.client.dkif.DkifKontaktinfo;
import no.nav.veilarbperson.client.egenansatt.EgenAnsattClient;
import no.nav.veilarbperson.client.pdl.HentPerson;
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
import java.util.*;
import java.util.stream.Collectors;

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
    private VeilarbportefoljeClient veilarbportefoljeClient = mock(VeilarbportefoljeClient.class);
    private AuthService authService = mock(AuthService.class);
    private PersonV2Service personV2Service;
    private HentPerson.Person person;

    private static final String USER_TOKEN = "USER_TOKEN";
    private static Fnr FNR = TestUtils.fodselsnummerForDato("1980-01-01");
    Fnr[] testFnrsTilBarna = {Fnr.of("12345678910"), Fnr.of("12345678911")};

    @Before
    public void setup() {
        when(norg2Client.hentTilhorendeEnhet(anyString())).thenReturn(new Enhet());
        when(dkifClient.hentKontaktInfo(any())).thenReturn(new DkifKontaktinfo());
        when(personClient.hentSikkerhetstiltak(any())).thenReturn(null);
        when(egenAnsattClient.erEgenAnsatt(any())).thenReturn(true);
        when(pdlClient.hentPersonBolk(any())).thenReturn(hentPersonBolk(testFnrsTilBarna));
        when(pdlClient.hentPersonNavn(any(), any())).thenReturn(hentPersonNavn(FNR));
        when(kodeverkService.getPoststedForPostnummer(any())).thenReturn("POSTSTED");
        when(kodeverkService.getBeskrivelseForLandkode(any())).thenReturn("LANDKODE");
        when(kodeverkService.getBeskrivelseForKommunenummer(any())).thenReturn("KOMMUNE");
        when(kodeverkService.getBeskrivelseForSpraakKode("EN")).thenReturn("Engelsk");
        when(kodeverkService.getBeskrivelseForSpraakKode("NO")).thenReturn("Norsk");
        when(kodeverkService.getBeskrivelseForTema("AAP")).thenReturn("Arbeidsavklaringpenger");
        when(kodeverkService.getBeskrivelseForTema("DAG")).thenReturn("Dagpenger");
        when(pdlClient.hentTilrettelagtKommunikasjon(any(), any())).thenReturn(hentTilrettelagtKommunikasjon(FNR));

        personV2Service = new PersonV2Service(pdlClient, authService, dkifClient, norg2Client, personClient, egenAnsattClient, veilarbportefoljeClient, kodeverkService);
        person = hentPerson(FNR);
    }

    public HentPerson.Person hentPerson(Fnr fnr) {
        PdlClientImpl pdlClient = configurPdlClient("pdl-hentPerson-response.json");
        return pdlClient.hentPerson(fnr, USER_TOKEN);
    }

    public HentPerson.PersonNavn hentPersonNavn(Fnr fnr) {
        PdlClientImpl pdlClient = configurPdlClient("pdl-hentPersonNavn-response.json");
        return pdlClient.hentPersonNavn(fnr, USER_TOKEN);
    }

    public List<HentPerson.Barn> hentPersonBolk(Fnr[] fnrs) {
        String apiUrl = configurApiResponse("pdl-hentPersonBolk-response.json");
        PdlClientImpl pdlClient = new PdlClientImpl(apiUrl, () -> "SYSTEM_USER_TOKEN");
        return pdlClient.hentPersonBolk(fnrs);
    }

    public HentPerson.Familiemedlem hentPartner(Fnr fnr) {
        PdlClientImpl pdlClient = configurPdlClient("pdl-hentPartner-response.json");
        return pdlClient.hentPartner(fnr, USER_TOKEN);
    }

    public HentPerson.VergeOgFullmakt hentVergeOgFullmakt(Fnr fnr) {
        PdlClientImpl pdlClient = configurPdlClient("pdl-hentVergeOgFullmakt-response.json");
        return pdlClient.hentVergeOgFullmakt(fnr, USER_TOKEN);
    }

    public HentPerson.GeografiskTilknytning hentGeografisktilknytning(Fnr fnr) {
        PdlClientImpl pdlClient = configurPdlClient("pdl-hentGeografiskTilknytning-response.json");
        return pdlClient.hentGeografiskTilknytning(fnr, USER_TOKEN);
    }

    public HentPerson.Person hentFamiliemedlem(Fnr fnr) {
        PdlClientImpl pdlClient = configurPdlClient("pdl-hentPersonMedIngenBarn-responsen.json");
        return pdlClient.hentPerson(fnr, USER_TOKEN);
    }

    public HentPerson.HentSpraakTolk hentTilrettelagtKommunikasjon(Fnr fnr) {
        PdlClientImpl pdlClient = configurPdlClient("pdl-hentTilrettelagtKommunikasjon-response.json");
        return pdlClient.hentTilrettelagtKommunikasjon(fnr, USER_TOKEN);
    }

    @Test
    public void hentFamilieRelasjonerSkalHenteForeldreOgBarnRelasjoner() {
        List<HentPerson.Familierelasjoner> familierelasjoner = person.getFamilierelasjoner();

        assertEquals("12345678910", familierelasjoner.get(0).getRelatertPersonsIdent());
        assertEquals("BARN", familierelasjoner.get(0).getRelatertPersonsRolle());

        assertEquals("12345678911", familierelasjoner.get(1).getRelatertPersonsIdent());
        assertEquals("BARN", familierelasjoner.get(1).getRelatertPersonsRolle());
    }

    @Test
    public void hentFnrTilBarnaTest() {
        List<HentPerson.Familierelasjoner> familierelasjoner = person.getFamilierelasjoner();
        Fnr[] fnrListe = personV2Service.hentFnrTilBarna(familierelasjoner);

        assertEquals(2, fnrListe.length);

        for (int i = 0; i < testFnrsTilBarna.length; i++) {
            assertEquals(testFnrsTilBarna[i], fnrListe[i]);
        }
    }

    @Test
    public void hentOpplysningerTilBarnaMedKodeOkFraPdlTest() {
        List<HentPerson.Barn> hentPersonBolk = hentPersonBolk(testFnrsTilBarna);

        assertEquals(3, hentPersonBolk.size());

        List<HentPerson.Barn> filterPersonBolkMedOkStatus = Optional.of(hentPersonBolk).stream().flatMap(Collection::stream)
                                                                        .filter(status -> status.getCode().equals("ok"))
                                                                        .collect(Collectors.toList());

        assertEquals(1, filterPersonBolkMedOkStatus.size());
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
    public void hentPartnerInformasjonTest() {
        Fnr fnrTilPartner = personV2Service.hentFnrTilPartner(person.getSivilstand());
        Bostedsadresse personsBostedsAdresse = person.getBostedsadresse().get(0);

        assertEquals("2134567890", fnrTilPartner.get());

        HentPerson.Familiemedlem partnerInformasjon = hentPartner(fnrTilPartner);
        Familiemedlem partner = PersonV2DataMapper.familiemedlemMapper(partnerInformasjon, personsBostedsAdresse);

        assertEquals("TYKKMAGET GASELLE", partner.getForkortetNavn());
        assertEquals(LocalDate.of(1981,12,13), partner.getFodselsdato());
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
    public void flettBarnInformasjonTest() {
        PersonV2Data personV2Data = getPersonV2Data();

        assertEquals(0, personV2Data.getBarn().size());

        personV2Service.flettBarnInformasjon(person.getFamilierelasjoner(), personV2Data); // Forsøker å flette person med 3 barn hvor informasjonen til bare 1 barn er tilgjemgelig i PDL

        assertEquals(1, personV2Data.getBarn().size()); // Fant bare 1 av 3 barna med "ok"(gyldig) status fra hentPersonBolk operasjonen

        person = hentFamiliemedlem(Fnr.of("12345678910"));  // Hent person med ingen barn for ex.Opplysninger til et barn selv
        personV2Data = getPersonV2Data();

        personV2Service.flettBarnInformasjon(person.getFamilierelasjoner(), personV2Data); // Forsøker å flette person som har ingen barn

        assertEquals(Collections.emptyList(), personV2Data.getBarn());     // Ingen barn blir lagt i personV2Data
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
        List<Telefon> telefonListeFraPdl = PersonV2DataMapper.mapTelefonNrFraPdl(person.getTelefonnummer());
        personV2Service.leggKrrTelefonNrIListe(telefonNrFraKrr, telefonListeFraPdl);  //Legger telefonnummere fra PDL og KRR som er ulike, til en liste

        assertEquals(2, telefonListeFraPdl.size());
        assertEquals("+4733333333", telefonListeFraPdl.get(0).getTelefonNr());
        assertEquals("+4622222222", telefonListeFraPdl.get(1).getTelefonNr());

        telefonNrFraKrr = "+4733333333";
        personV2Service.leggKrrTelefonNrIListe(telefonNrFraKrr, telefonListeFraPdl); //Legger telefonnummere fra PDL og KRR som er like, til en liste

        assertEquals(2, telefonListeFraPdl.size());
        assertEquals("+4733333333", telefonListeFraPdl.get(0).getTelefonNr());

        telefonNrFraKrr = "+4811111111";
        personV2Service.leggKrrTelefonNrIListe(telefonNrFraKrr, telefonListeFraPdl); //Legger en ny telefonnr fra KRR til en pdlTelefonNrListe

        assertEquals(3, telefonListeFraPdl.size());
        assertEquals("+4811111111", telefonListeFraPdl.get(2).getTelefonNr());
        assertEquals("3", telefonListeFraPdl.get(2).getPrioritet());

        telefonNrFraKrr = null;
        personV2Service.leggKrrTelefonNrIListe(telefonNrFraKrr, telefonListeFraPdl); //Legger telefonnummere fra PDL og KRR til en liste hvor telefonnummer fra KRR er null

        assertEquals(3, telefonListeFraPdl.size());
        assertEquals("+4733333333", telefonListeFraPdl.get(0).getTelefonNr());

        telefonListeFraPdl = new ArrayList<>();
        telefonNrFraKrr = "+4733333333";
        personV2Service.leggKrrTelefonNrIListe(telefonNrFraKrr, telefonListeFraPdl); //Legger telefonnummere fra PDL og KRR til en liste hvor telefonnummer fra PDL er null

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
    }

    @Test
    public void flettMotpartsPersonNavnTilFullmakt() {
        HentPerson.VergeOgFullmakt vergeOgFullmaktFraPdl = hentVergeOgFullmakt(FNR);
        VergeOgFullmaktData vergeOgFullmaktData = VergeOgFullmaktDataMapper.toVergeOgFullmaktData(vergeOgFullmaktFraPdl);
        personV2Service.flettMotpartsPersonNavnTilFullmakt(vergeOgFullmaktData, USER_TOKEN);

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
        TilrettelagtKommunikasjonData tilrettelagtKommunikasjonData = personV2Service.hentSpraakTolkInfo(FNR, "USER_TOKEN");
        assertEquals("Engelsk", tilrettelagtKommunikasjonData.getTalespraak());
        assertEquals("Norsk", tilrettelagtKommunikasjonData.getTegnspraak());
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
