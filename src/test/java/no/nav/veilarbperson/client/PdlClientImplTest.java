package no.nav.veilarbperson.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.nav.common.json.JsonUtils;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.pdl.GqlRequest;
import no.nav.veilarbperson.client.pdl.HentPdlPerson;
import no.nav.veilarbperson.client.pdl.PdlClientImpl;
import no.nav.veilarbperson.client.pdl.PdlPersonVariables;
import no.nav.veilarbperson.client.pdl.domain.*;
import no.nav.veilarbperson.utils.FileUtils;
import no.nav.veilarbperson.utils.TestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

public class PdlClientImplTest {

    private static Fnr FNR = TestUtils.fodselsnummerForDato("1980-01-01");
    private static String USER_TOKEN = "USER_TOKEN";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    @Test
    public void hentePerson_skal_lage_riktig_request() {
        String hentPersonResponseJson = TestUtils.readTestResourceFile("pdl-hentPerson-response.json");
        String apiUrl = "http://localhost:" + wireMockRule.port();

        givenThat(post(urlEqualTo("/graphql"))
                .withHeader("Nav-Consumer-Token", equalTo("Bearer SYSTEM_USER_TOKEN"))
                .withHeader("Authorization", equalTo("Bearer USER_TOKEN"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(hentPersonResponseJson))
        );

        PdlClientImpl pdlClient = new PdlClientImpl(apiUrl, () -> "SYSTEM_USER_TOKEN");

        pdlClient.hentPerson(FNR, "USER_TOKEN");
    }

    @Test
    public void hentePerson_skal_parse_request() {
        String hentPersonResponseJson = TestUtils.readTestResourceFile("pdl-hentPerson-response.json");
        String apiUrl = "http://localhost:" + wireMockRule.port();

        givenThat(post(anyUrl())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(hentPersonResponseJson))
        );

        PdlClientImpl pdlClient = new PdlClientImpl(apiUrl, () -> "SYSTEM_USER_TOKEN");

        HentPdlPerson.PdlPerson person = pdlClient.hentPerson(FNR, USER_TOKEN);

        HentPdlPerson.Navn navn = person.getNavn().get(0);
        assertEquals("NATURLIG", navn.getFornavn());
        assertEquals("GLITRENDE", navn.getMellomnavn());
        assertEquals("STAFFELI", navn.getEtternavn());
        assertEquals("NATURLIG STAFFELI", navn.getForkortetNavn());

        HentPdlPerson.Folkeregisteridentifikator identifikator = person.getFolkeregisteridentifikator().get(0);
        assertEquals("0123456789", identifikator.getIdentifikasjonsnummer());
        assertEquals("I_BRUK", identifikator.getStatus());
        assertEquals("FNR", identifikator.getType());

        HentPdlPerson.Sivilstand sivilstand = person.getSivilstand().get(0);
        assertEquals(LocalDate.of(2020,06,01), sivilstand.getGyldigFraOgMed());
        assertEquals("GIFT", sivilstand.getType());

        assertTrue(person.getKjoenn().isEmpty());

        HentPdlPerson.Foedsel foedsel = person.getFoedsel().get(0);
        assertEquals(LocalDate.of(1981,12,13), foedsel.getFoedselsdato());

        HentPdlPerson.Familierelasjoner familierelasjoner = person.getFamilierelasjoner().get(0);
        assertEquals("MOR", familierelasjoner.getMinRolleForPerson());
        assertEquals("BARN", familierelasjoner.getRelatertPersonsRolle());
        assertEquals("12345678910", familierelasjoner.getRelatertPersonsIdent());

        HentPdlPerson.Statsborgerskap statsborgerskap = person.getStatsborgerskap().get(0);
        assertEquals("NORGE", statsborgerskap.getLand());

        Bostedsadresse bostedsadresse = person.getBostedsadresse().get(0);
        Bostedsadresse.Vegadresse vegadresse = bostedsadresse.getVegadresse();
        assertEquals("ARENDALSGATE", vegadresse.getAdressenavn());
        assertEquals("A", vegadresse.getHusbokstav());
        assertEquals("21", vegadresse.getHusnummer());
        assertEquals("0560", vegadresse.getPostnummer());
        assertEquals("0570", vegadresse.getKommunenummer());
        assertEquals("ARENDAL", vegadresse.getTilleggsnavn());

        HentPdlPerson.Telefonnummer telefonnummer = person.getTelefonnummer().get(0);
        assertEquals("33333333", telefonnummer.getNummer());
        assertEquals("+47", telefonnummer.getLandskode());
        assertEquals("1", telefonnummer.getPrioritet());
        assertEquals("PDL", telefonnummer.getMetadata().getMaster());

        HentPdlPerson.Adressebeskyttelse adressebeskyttelse = person.getAdressebeskyttelse().get(0);
        assertEquals("UGRADERT", adressebeskyttelse.getGradering());

        Oppholdsadresse oppholdsadresse = person.getOppholdsadresse().get(0);
        Oppholdsadresse.Matrikkeladresse matrikkeladresse = oppholdsadresse.getMatrikkeladresse();
        assertEquals(123456789L, matrikkeladresse.getMatrikkelId());
        assertEquals("kommunenummer", matrikkeladresse.getKommunenummer());
        assertEquals("postnummer", matrikkeladresse.getPostnummer());
        assertEquals("bruksenhetsnummer", matrikkeladresse.getBruksenhetsnummer());
        assertEquals("tilleggsnavn", matrikkeladresse.getTilleggsnavn());

        Kontaktadresse kontaktAdresse = person.getKontaktadresse().get(0);

        LocalDateTime localDateTime = LocalDateTime.of(LocalDate.of(2021,01,15), LocalTime.of(11,58,57));
        assertEquals(localDateTime, kontaktAdresse.getGyldigFraOgMed());
        assertNull(kontaktAdresse.getGyldigTilOgMed());

        Kontaktadresse.Vegadresse kontaktsVegadresse = kontaktAdresse.getVegadresse();
        assertEquals(123456789L, kontaktsVegadresse.getMatrikkelId());
        assertEquals("postnummer", kontaktsVegadresse.getPostnummer());
        assertEquals("adressenavn", kontaktsVegadresse.getAdressenavn());
        assertEquals("husbokstav", kontaktsVegadresse.getHusbokstav());
        assertEquals("husnummer", kontaktsVegadresse.getHusnummer());
        assertEquals("kommunenummer", kontaktsVegadresse.getKommunenummer());
        assertEquals("tilleggsnavn", kontaktsVegadresse.getTilleggsnavn());

        Kontaktadresse.Postboksadresse kontaktsPostboksadresse = person.getKontaktadresse().get(1).getPostboksadresse();
        assertEquals("postnummer", kontaktsPostboksadresse.getPostnummer());
        assertEquals("postboks", kontaktsPostboksadresse.getPostboks());
        assertEquals("postbokseier", kontaktsPostboksadresse.getPostbokseier());

        Kontaktadresse.Utenlandskadresse utenlandskAdresse = kontaktAdresse.getUtenlandskAdresse();
        assertEquals("adressenavnNummer", utenlandskAdresse.getAdressenavnNummer());
        assertEquals("bygningEtasjeLeilighet", utenlandskAdresse.getBygningEtasjeLeilighet());
        assertEquals("postboksNummerNavn", utenlandskAdresse.getPostboksNummerNavn());
        assertEquals("postkode", utenlandskAdresse.getPostkode());
        assertEquals("bySted", utenlandskAdresse.getBySted());
        assertEquals("regionDistriktOmraade", utenlandskAdresse.getRegionDistriktOmraade());
        assertEquals("landkode", utenlandskAdresse.getLandkode());

        Kontaktadresse.PostadresseIFrittFormat postadresse = kontaktAdresse.getPostadresseIFrittFormat();
        assertEquals("SOT6", postadresse.getAdresselinje1());
        assertEquals("POSTBOKS 2094 VIKA", postadresse.getAdresselinje2());
        assertNull(postadresse.getAdresselinje3());
        assertEquals("0125", postadresse.getPostnummer());

        Kontaktadresse.UtenlandskAdresseIFrittFormat utenlandskAdresseIFrittFormat = kontaktAdresse.getUtenlandskAdresseIFrittFormat();
        assertEquals("Postboks 100", utenlandskAdresseIFrittFormat.getAdresselinje1());
        assertEquals("12345", utenlandskAdresseIFrittFormat.getAdresselinje2());
        assertNull(utenlandskAdresseIFrittFormat.getAdresselinje3());
        assertEquals("FRA", utenlandskAdresseIFrittFormat.getLandkode());
    }

    @Test
    public void henteVergeOgFullmakt_skal_parse_request() {
        String hentVergeOgFullmaktResponseJson = TestUtils.readTestResourceFile("pdl-hentVergeOgFullmakt-response.json");
        String apiUrl = "http://localhost:" + wireMockRule.port();

        givenThat(post(anyUrl())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(hentVergeOgFullmaktResponseJson))
        );

        PdlClientImpl pdlClient = new PdlClientImpl(apiUrl, () -> "SYSTEM_USER_TOKEN");

        HentPdlPerson.VergeOgFullmakt vergeOgFullmakt = pdlClient.hentVergeOgFullmakt(FNR, USER_TOKEN);

        HentPdlPerson.VergemaalEllerFremtidsfullmakt vergemaal = vergeOgFullmakt.getVergemaalEllerFremtidsfullmakt().get(0);
        HentPdlPerson.VergeEllerFullmektig vergeEllerFullmektig = vergemaal.getVergeEllerFullmektig();

        assertEquals(Vergetype.MIDLERTIDIG_FOR_VOKSEN, vergemaal.getType());
        assertEquals("VergemallEmbete", vergemaal.getEmbete());
        assertEquals(VergemaalEllerFullmaktOmfangType.OEKONOMISKE_INTERESSER, vergeEllerFullmektig.getOmfang());
        assertEquals("VergeMotpartsPersonident1", vergeEllerFullmektig.getMotpartsPersonident());
        assertEquals("vergeEtternavn1", vergeEllerFullmektig.getNavn().getEtternavn());

        HentPdlPerson.Fullmakt fullmakt = vergeOgFullmakt.getFullmakt().get(0);

        assertEquals("motpartsPersonident1", fullmakt.getMotpartsPersonident());
        assertEquals("motpartsRolle1", fullmakt.getMotpartsRolle());
        assertEquals(2, fullmakt.getOmraader().length);
    }

    @Test
    public void hentePerson_skal_sjekke_feil() {
        String hentPersonErrorResponseJson = TestUtils.readTestResourceFile("pdl-hentPerson-error-response.json");
        String apiUrl = "http://localhost:" + wireMockRule.port();

        givenThat(post(anyUrl())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(hentPersonErrorResponseJson))
        );

        PdlClientImpl pdlClient = new PdlClientImpl(apiUrl, () -> "SYSTEM_USER_TOKEN");

        assertThrows(ResponseStatusException.class, () -> {
            pdlClient.hentPerson(FNR, "USER_TOKEN");
        });
    }

    @Test
    public void rawRequest_skal_sende_raw_request_og_returnere_raw_response() {
        String hentPersonResponseJson = TestUtils.readTestResourceFile("pdl-hentPerson-response.json");
        String hentPersonRequest = FileUtils.getResourceFileAsString("graphql/hentPerson.gql");
        String apiUrl = "http://localhost:" + wireMockRule.port();

        String jsonRequest = JsonUtils.toJson(new GqlRequest<>(hentPersonRequest, new PdlPersonVariables.HentPersonVariables(Fnr.of("TEST_IDENT"), false)));

        givenThat(post(urlEqualTo("/graphql"))
                .withRequestBody(equalToJson(jsonRequest))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(hentPersonResponseJson))
        );

        PdlClientImpl pdlClient = new PdlClientImpl(apiUrl, () -> "SYSTEM_USER_TOKEN");

        String response = pdlClient.rawRequest(jsonRequest, "USER_TOKEN");
        assertEquals(hentPersonResponseJson, response);
    }

}
