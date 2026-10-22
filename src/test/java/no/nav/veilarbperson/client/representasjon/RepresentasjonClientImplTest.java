package no.nav.veilarbperson.client.representasjon;

import no.nav.common.types.identer.Fnr;
import okhttp3.*;
import okio.Buffer;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

public class RepresentasjonClientImplTest {
    private static final String PATH = "/api/v2/internbruker/fullmakt/bruker-som-fullmaktsgiver/alle-fullmakter";
    private static final String FNR = "01010112345";
    private static final String FULLMAKT = """
            {
              "fullmaktId":"fd915799-88bf-4f19-b8c3-d2a547be6730",
              "fullmaktsgiver":"01010112345",
              "fullmektig":"02020212345",
              "gyldigFraOgMed":"2026-01-01",
              "gyldigTilOgMed":null,
              "leserettigheter":["AAP","DAG"],
              "skriverettigheter":["DAG"],
              "endringslogg":[{"ubrukt":"metadata"}]
            }
            """;

    private int status;
    private String body;
    private Request sisteRequest;

    private RepresentasjonClientImpl client() {
        OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(chain -> {
            sisteRequest = chain.request();
            return new Response.Builder()
                    .request(sisteRequest)
                    .protocol(Protocol.HTTP_1_1)
                    .code(status)
                    .message("Test response")
                    .body(ResponseBody.create(MediaType.get("application/json"), body))
                    .build();
        }).build();
        return new RepresentasjonClientImpl("http://representasjon.test", () -> "TEST_TOKEN", httpClient);
    }

    private void svar(int status, String body) {
        this.status = status;
        this.body = body;
    }

    @Test
    public void senderPostMedIdentOgOboTokenOgParserV2() throws IOException {
        svar(200, "[" + FULLMAKT + "]");

        var fullmakter = client().hentFullmakt(FNR);

        assertEquals(1, fullmakter.size());
        var fullmakt = fullmakter.getFirst();
        assertEquals(Fnr.of(FNR), fullmakt.getFullmaktsgiver());
        assertEquals(Fnr.of("02020212345"), fullmakt.getFullmektig());
        assertEquals(LocalDate.of(2026, 1, 1), fullmakt.getGyldigFraOgMed());
        assertNull(fullmakt.getGyldigTilOgMed());
        assertEquals(List.of("AAP", "DAG"), fullmakt.getLeserettigheter());
        assertEquals(List.of("DAG"), fullmakt.getSkriverettigheter());
        assertFalse(fullmakt.toString().contains(FNR));
        assertFalse(fullmakt.toString().contains("02020212345"));
        assertEquals(PATH, sisteRequest.url().encodedPath());
        assertNull(sisteRequest.url().query());
        assertEquals("POST", sisteRequest.method());
        assertEquals("Bearer TEST_TOKEN", sisteRequest.header("Authorization"));
        assertEquals("application/json", sisteRequest.header("Accept"));
        assertEquals("application/json; charset=utf-8", sisteRequest.body().contentType().toString());
        Buffer requestBody = new Buffer();
        sisteRequest.body().writeTo(requestBody);
        assertEquals("{\"ident\":\"" + FNR + "\"}", requestBody.readUtf8());
    }

    @Test
    public void parserSluttdatoOgTommeRettigheter() throws IOException {
        svar(200, "[" + FULLMAKT.replace("\"gyldigTilOgMed\":null", "\"gyldigTilOgMed\":\"2027-02-01\"")
                .replace("[\"AAP\",\"DAG\"]", "[]").replace("[\"DAG\"]", "[]") + "]");

        var fullmakt = client().hentFullmakt(FNR).getFirst();

        assertEquals(LocalDate.of(2027, 2, 1), fullmakt.getGyldigTilOgMed());
        assertTrue(fullmakt.getLeserettigheter().isEmpty());
        assertTrue(fullmakt.getSkriverettigheter().isEmpty());
    }

    @Test
    public void tomListeErGyldig() throws IOException {
        svar(200, "[]");
        assertTrue(client().hentFullmakt(FNR).isEmpty());
    }

    @Test
    public void manglendeRequiredFeltFeilerOgsaaForNullableSluttdato() {
        for (String felt : List.of(
                "\"fullmaktsgiver\":\"01010112345\",",
                "\"fullmektig\":\"02020212345\",",
                "\"gyldigFraOgMed\":\"2026-01-01\",",
                "\"gyldigTilOgMed\":null,",
                "\"leserettigheter\":[\"AAP\",\"DAG\"],",
                "\"skriverettigheter\":[\"DAG\"],")) {
            svar(200, "[" + FULLMAKT.replace(felt, "") + "]");
            assertUgyldigRespons();
        }
    }

    @Test
    public void nullRequiredFeltOgUgyldigeVerdierFeilerUtenPersondata() {
        for (String body : List.of(
                FULLMAKT.replace("\"fullmaktsgiver\":\"01010112345\"", "\"fullmaktsgiver\":null"),
                FULLMAKT.replace("\"fullmektig\":\"02020212345\"", "\"fullmektig\":null"),
                FULLMAKT.replace("\"fullmaktsgiver\":\"01010112345\"", "\"fullmaktsgiver\":\"\""),
                FULLMAKT.replace("\"fullmektig\":\"02020212345\"", "\"fullmektig\":\" \""),
                FULLMAKT.replace("\"2026-01-01\"", "null"),
                FULLMAKT.replace("\"2026-01-01\"", "\"ugyldig-sensitiv-verdi\""),
                FULLMAKT.replace("[\"AAP\",\"DAG\"]", "null"),
                FULLMAKT.replace("[\"DAG\"]", "null"),
                FULLMAKT.replace("[\"DAG\"]", "[null]"),
                FULLMAKT.replace("[\"DAG\"]", "[\"\"]"))) {
            svar(200, "[" + body + "]");
            assertUgyldigRespons();
        }
    }

    @Test
    public void feilResponsformOgManglendeBodyFeiler() {
        for (String body : List.of("", "null", "{}", "[null]", "[", "[\"sensitiv-verdi\"]")) {
            svar(200, body);
            var feil = assertThrows(IllegalStateException.class, () -> client().hentFullmakt(FNR));
            assertFalse(feil.toString().contains(FNR));
            assertNull(feil.getCause());
        }
        svar(204, "");
        assertThrows(IllegalStateException.class, () -> client().hentFullmakt(FNR));
    }

    @Test
    public void httpFeilGirIkkeTomListeEllerLekkasjerFraFeilbody() {
        for (int status : List.of(400, 403, 500)) {
            svar(status, "sensitiv-feilbody");
            var feil = assertThrows(RuntimeException.class, () -> client().hentFullmakt(FNR));
            assertFalse(feil.toString().contains("sensitiv-feilbody"));
            assertFalse(feil.toString().contains(FNR));
            assertNull(feil.getCause());
        }
    }

    @Test
    public void transportfeilPropageres() {
        var feil = new IOException("Representasjon utilgjengelig");
        OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(chain -> {
            throw feil;
        }).build();
        var client = new RepresentasjonClientImpl("http://representasjon.test", () -> "TEST_TOKEN", httpClient);

        assertSame(feil, assertThrows(IOException.class, () -> client.hentFullmakt(FNR)));
    }

    private void assertUgyldigRespons() {
        var feil = assertThrows(IllegalStateException.class, () -> client().hentFullmakt(FNR));
        assertEquals("Ugyldig fullmaktsrespons fra representasjon", feil.getMessage());
        assertNull(feil.getCause());
    }
}
