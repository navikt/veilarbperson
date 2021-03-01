package no.nav.veilarbperson.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.nav.common.json.JsonUtils;
import no.nav.veilarbperson.client.pdl.GqlRequest;
import no.nav.veilarbperson.client.pdl.HentPdlPerson;
import no.nav.veilarbperson.client.pdl.PdlClientImpl;
import no.nav.veilarbperson.client.pdl.PdlPersonVariables;
import no.nav.veilarbperson.client.pdl.domain.Bostedsadresse;
import no.nav.veilarbperson.client.pdl.domain.Kontaktadresse;
import no.nav.veilarbperson.client.pdl.domain.Oppholdsadresse;
import no.nav.veilarbperson.utils.FileUtils;
import no.nav.veilarbperson.utils.TestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.web.server.ResponseStatusException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

public class PdlClientImplTest {

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

        pdlClient.hentPerson("IDENT", "USER_TOKEN");
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

        HentPdlPerson.PdlPerson person = pdlClient.hentPerson("IDENT", "USER_TOKEN");

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
        assertEquals("2020-06-01", sivilstand.getGyldigFraOgMed());
        assertEquals("GIFT", sivilstand.getType());

        assertTrue(person.getDoedsfall().isEmpty());

        HentPdlPerson.Foedsel foedsel = person.getFoedsel().get(0);
        assertEquals("1981-12-13", foedsel.getFoedselsdato());

        HentPdlPerson.Familierelasjoner familierelasjoner = person.getFamilierelasjoner().get(0);
        assertEquals("MOR", familierelasjoner.getMinRolleForPerson());
        assertEquals("BARN", familierelasjoner.getRelatertPersonsRolle());
        assertEquals("12345678910", familierelasjoner.getRelatertPersonsIdent());

        HentPdlPerson.Statsborgerskap statsborgerskap = person.getStatsborgerskap().get(0);
        assertEquals("NORGE", statsborgerskap.getLand());
        assertEquals("1980-12-24", statsborgerskap.getGyldigFraOgMed());
        assertEquals("2010-12-30", statsborgerskap.getGyldigTilOgMed());

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

        Kontaktadresse.Vegadresse kontaktsVegadresse = kontaktAdresse.getVegadresse();
        assertEquals(123456789L, kontaktsVegadresse.getMatrikkelId());
        assertEquals("postnummer", kontaktsVegadresse.getPostnummer());
        assertEquals("adressenavn", kontaktsVegadresse.getAdressenavn());
        assertEquals("husbokstav", kontaktsVegadresse.getHusbokstav());
        assertEquals("husnummer", kontaktsVegadresse.getHusnummer());
        assertEquals("kommunenummer", kontaktsVegadresse.getKommunenummer());
        assertEquals("tilleggsnavn", kontaktsVegadresse.getTilleggsnavn());

        Kontaktadresse.Postboksadresse kontaktsPostboksadresse = kontaktAdresse.getPostboksadresse();
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
            pdlClient.hentPerson("IDENT", "USER_TOKEN");
        });
    }

    @Test
    public void rawRequest_skal_sende_raw_request_og_returnere_raw_response() {
        String hentPersonResponseJson = TestUtils.readTestResourceFile("pdl-hentPerson-response.json");
        String hentPersonRequest = FileUtils.getResourceFileAsString("graphql/hentPerson.gql");
        String apiUrl = "http://localhost:" + wireMockRule.port();

        String jsonRequest = JsonUtils.toJson(new GqlRequest<>(hentPersonRequest, new PdlPersonVariables.HentPersonVariables("TEST_IDENT", false)));

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
