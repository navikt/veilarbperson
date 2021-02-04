package no.nav.veilarbperson.service;

import no.nav.common.json.JsonUtils;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.pam.CvIkkeTilgang;
import no.nav.veilarbperson.client.pam.PamClient;
import no.nav.veilarbperson.client.person.domain.TpsPerson;
import no.nav.veilarbperson.client.veilarboppfolging.UnderOppfolging;
import no.nav.veilarbperson.client.veilarboppfolging.VeilarboppfolgingClient;
import no.nav.veilarbperson.utils.TestUtils;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import static no.nav.common.rest.client.RestUtils.MEDIA_TYPE_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CvJobbprofilServiceTest {

    private AuthService authService;

    private PersonService personService;

    private VeilarboppfolgingClient veilarboppfolgingClient;

    private PamClient pamClient;

    private CvJobbprofilService cvJobbprofilService;

    @Before
    public void setup() {
        authService = mock(AuthService.class);
        personService = mock(PersonService.class);
        veilarboppfolgingClient = mock(VeilarboppfolgingClient.class);
        pamClient = mock(PamClient.class);

        cvJobbprofilService = new CvJobbprofilService(authService, personService, veilarboppfolgingClient, pamClient);
    }

    @Test
    public void should_return_error_when_not_internal_user() {
        when(authService.erInternBruker()).thenReturn(false);

        ResponseEntity<String> response = cvJobbprofilService.hentCvJobbprofilJsonV2(Fnr.of("1234"));

        String expectedJson = JsonUtils.toJson(new CvJobbprofilService.CvIkkeTilgangResponse(CvIkkeTilgang.IKKE_TILGANG_TIL_BRUKER));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(expectedJson, response.getBody());
    }

    @Test
    public void should_return_error_when_not_authorized() {
        when(authService.erInternBruker()).thenReturn(true);
        when(authService.harLesetilgang(any())).thenReturn(false);

        ResponseEntity<String> response = cvJobbprofilService.hentCvJobbprofilJsonV2(Fnr.of("1234"));

        String expectedJson = JsonUtils.toJson(new CvJobbprofilService.CvIkkeTilgangResponse(CvIkkeTilgang.IKKE_TILGANG_TIL_BRUKER));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(expectedJson, response.getBody());
    }

    @Test
    public void should_return_error_if_user_is_dead() {
        when(authService.erInternBruker()).thenReturn(true);
        when(authService.harLesetilgang(any())).thenReturn(true);
        when(personService.hentPerson(any())).thenReturn(new TpsPerson().setDodsdato("SOME DATE"));

        ResponseEntity<String> response = cvJobbprofilService.hentCvJobbprofilJsonV2(Fnr.of("1234"));

        String expectedJson = JsonUtils.toJson(new CvJobbprofilService.CvIkkeTilgangResponse(CvIkkeTilgang.BRUKER_ER_DOED));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(expectedJson, response.getBody());
    }

    @Test
    public void should_return_error_if_user_is_not_under_oppfolging() {
        when(authService.erInternBruker()).thenReturn(true);
        when(authService.harLesetilgang(any())).thenReturn(true);
        when(personService.hentPerson(any())).thenReturn(new TpsPerson());
        when(veilarboppfolgingClient.hentUnderOppfolgingStatus(any())).thenReturn(new UnderOppfolging());

        ResponseEntity<String> response = cvJobbprofilService.hentCvJobbprofilJsonV2(Fnr.of("1234"));

        String expectedJson = JsonUtils.toJson(new CvJobbprofilService.CvIkkeTilgangResponse(CvIkkeTilgang.BRUKER_IKKE_UNDER_OPPFOLGING));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(expectedJson, response.getBody());
    }

    @Test
    public void should_throw_404_if_cv_status_is_404() {
        when(authService.erInternBruker()).thenReturn(true);
        when(authService.harLesetilgang(any())).thenReturn(true);
        when(personService.hentPerson(any())).thenReturn(new TpsPerson());
        when(veilarboppfolgingClient.hentUnderOppfolgingStatus(any())).thenReturn(new UnderOppfolging().setUnderOppfolging(true));
        when(pamClient.hentCvOgJobbprofilJsonV2(any())).thenReturn(
                new Response.Builder()
                        .request(new Request.Builder().url("http://local.test").build())
                        .protocol(Protocol.HTTP_1_1)
                        .message("")
                        .code(404)
                        .build()
        );

        try {
            cvJobbprofilService.hentCvJobbprofilJsonV2(Fnr.of("1234"));
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        }
    }

    @Test
    public void should_return_error_if_cv_status_is_406() {
        when(authService.erInternBruker()).thenReturn(true);
        when(authService.harLesetilgang(any())).thenReturn(true);
        when(personService.hentPerson(any())).thenReturn(new TpsPerson());
        when(veilarboppfolgingClient.hentUnderOppfolgingStatus(any())).thenReturn(new UnderOppfolging().setUnderOppfolging(true));
        when(pamClient.hentCvOgJobbprofilJsonV2(any())).thenReturn(
                new Response.Builder()
                        .request(new Request.Builder().url("http://local.test").build())
                        .protocol(Protocol.HTTP_1_1)
                        .message("")
                        .code(406)
                        .build()
        );

        ResponseEntity<String> response = cvJobbprofilService.hentCvJobbprofilJsonV2(Fnr.of("1234"));

        String expectedJson = JsonUtils.toJson(new CvJobbprofilService.CvIkkeTilgangResponse(CvIkkeTilgang.BRUKER_IKKE_GODKJENT_SAMTYKKE));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(expectedJson, response.getBody());
    }

    @Test
    public void should_throw_404_if_cv_is_empty() {
        when(authService.erInternBruker()).thenReturn(true);
        when(authService.harLesetilgang(any())).thenReturn(true);
        when(personService.hentPerson(any())).thenReturn(new TpsPerson());
        when(veilarboppfolgingClient.hentUnderOppfolgingStatus(any())).thenReturn(new UnderOppfolging().setUnderOppfolging(true));
        when(pamClient.hentCvOgJobbprofilJsonV2(any())).thenReturn(
                new Response.Builder()
                        .request(new Request.Builder().url("http://local.test").build())
                        .protocol(Protocol.HTTP_1_1)
                        .message("")
                        .body(ResponseBody.create(MEDIA_TYPE_JSON, "{}"))
                        .code(200)
                        .build()
        );

        try {
            cvJobbprofilService.hentCvJobbprofilJsonV2(Fnr.of("1234"));
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        }
    }

    @Test
    public void should_return_cv_json() {
        String pamCvJobbprofilJson = TestUtils.readTestResourceFile("pam-cv-jobbprofil.json");

        when(authService.erInternBruker()).thenReturn(true);
        when(authService.harLesetilgang(any())).thenReturn(true);
        when(personService.hentPerson(any())).thenReturn(new TpsPerson());
        when(veilarboppfolgingClient.hentUnderOppfolgingStatus(any())).thenReturn(new UnderOppfolging().setUnderOppfolging(true));
        when(pamClient.hentCvOgJobbprofilJsonV2(any())).thenReturn(
                new Response.Builder()
                        .request(new Request.Builder().url("http://local.test").build())
                        .protocol(Protocol.HTTP_1_1)
                        .message("")
                        .body(ResponseBody.create(MEDIA_TYPE_JSON, pamCvJobbprofilJson))
                        .code(200)
                        .build()
        );

        ResponseEntity<String> response = cvJobbprofilService.hentCvJobbprofilJsonV2(Fnr.of("1234"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pamCvJobbprofilJson, response.getBody());
    }

}
