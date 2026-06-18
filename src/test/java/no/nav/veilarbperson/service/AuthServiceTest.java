package no.nav.veilarbperson.service;

import com.nimbusds.jwt.JWTClaimsSet;
import no.nav.common.audit_log.cef.CefMessage;
import no.nav.common.audit_log.log.AuditLogger;
import no.nav.common.audit_log.log.AuditLoggerImpl;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.token_client.client.AzureAdOnBehalfOfTokenClient;
import no.nav.common.types.identer.Fnr;
import no.nav.common.types.identer.NavIdent;
import no.nav.poao_tilgang.client.Decision;
import no.nav.poao_tilgang.client.NavAnsattTilgangTilEksternBrukerKjernereglerPolicyInput;
import no.nav.poao_tilgang.client.PoaoTilgangClient;
import no.nav.poao_tilgang.client.api.ApiResult;
import no.nav.veilarbperson.config.EnvironmentProperties;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

public class AuthServiceTest {
	private final AuthContextHolder authContextHolder = mock(AuthContextHolder.class);
	private final AktorOppslagClient aktorOppslagClient = mock(AktorOppslagClient.class);
	private final AzureAdOnBehalfOfTokenClient azureAdOnBehalfOfTokenClient = mock(AzureAdOnBehalfOfTokenClient.class);
	private final EnvironmentProperties environmentProperties = mock(EnvironmentProperties.class);
	private final AuditLogger auditLogger = mock(AuditLoggerImpl.class);
	private final PoaoTilgangClient poaoTilgangClient = mock(PoaoTilgangClient.class);


	private final AuthService authService = new AuthService(
			aktorOppslagClient,
			authContextHolder,
			environmentProperties,
			azureAdOnBehalfOfTokenClient,
			poaoTilgangClient,
			auditLogger
	);

	@Test
	public void harLesetilgang_EksternBrukerNivaa4_Permit() {
		Fnr fodselsnr = new Fnr("123");
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.claim("roles", "EKSTERN")
				.claim("acr", "Level4")
				.claim("pid", fodselsnr.get())
				.build();
		when(authContextHolder.getUid()).thenReturn(Optional.of(fodselsnr.get()));
		when(authContextHolder.erEksternBruker()).thenReturn(true);
		when(authContextHolder.getIdTokenClaims()).thenReturn(Optional.of(claims));

		when(poaoTilgangClient.evaluatePolicy(any())).thenReturn(new ApiResult<>(null, Decision.Permit.INSTANCE));

		Boolean answer = authService.harLesetilgang(fodselsnr);

		Assertions.assertEquals(true, answer);
		verify(auditLogger, times(1)).log(any(CefMessage.class));

	}

	@Test
	public void harLesetilgang_EksternBrukerIkkeNivaa4_Forbidden() {
		Fnr fodselsnr = new Fnr("123");
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.claim("roles", "EKSTERN")
				.claim("acr", "Level3")
				.claim("pid", fodselsnr.get())
				.build();
		when(authContextHolder.getUid()).thenReturn(Optional.of(fodselsnr.get()));
		when(authContextHolder.erEksternBruker()).thenReturn(true);
		when(authContextHolder.getIdTokenClaims()).thenReturn(Optional.of(claims));

		when(poaoTilgangClient.evaluatePolicy(any())).thenReturn(new ApiResult<>(null, Decision.Permit.INSTANCE));


		Assertions.assertThrows(ResponseStatusException.class, () -> authService.harLesetilgang(fodselsnr));
		verify(auditLogger, times(0)).log(any(CefMessage.class));

	}

	@Test
	public void harLesetilgang_EksternBrukerNivaa4_Deny() {
		Fnr fodselsnr = new Fnr("123");
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.claim("roles", "EKSTERN")
				.claim("acr", "Level4")
				.claim("pid", "4321")
				.build();
		when(authContextHolder.getUid()).thenReturn(Optional.of("4321"));
		when(authContextHolder.erEksternBruker()).thenReturn(true);
		when(authContextHolder.getIdTokenClaims()).thenReturn(Optional.of(claims));

		when(poaoTilgangClient.evaluatePolicy(any())).thenReturn(new ApiResult<>(null, new Decision.Deny("","")));

		Boolean answer = authService.harLesetilgang(fodselsnr);

		Assertions.assertEquals(false, answer);
		verify(auditLogger, times(1)).log(any(CefMessage.class));
	}

	@Test
	public void harLesetilgang_Veileder_Permit() {
		Fnr fodselsnr = new Fnr("123");
		NavIdent navIdent = new NavIdent("A678910");
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.claim("roles", "INTERN")
				.claim("NAVident", navIdent.get())
				.claim("acr", "Level4")
				.claim("oid", "00000000-0000-0001-0000-0000000003e8")
				.build();
		when(authContextHolder.requireIdTokenClaims()).thenReturn(claims);
		when(authContextHolder.erInternBruker()).thenReturn(true);
		when(authContextHolder.getIdTokenClaims()).thenReturn(Optional.of(claims));

		when(poaoTilgangClient.evaluatePolicy(any())).thenReturn(new ApiResult<>(null, Decision.Permit.INSTANCE));

		Boolean answer = authService.harLesetilgang(fodselsnr);

		Assertions.assertEquals(true, answer);
		verify(auditLogger, times(1)).log(any(CefMessage.class));

	}

	@Test
	public void harLesetilgang_Veileder_Deny() {
		Fnr fodselsnr = new Fnr("123");
		NavIdent navIdent = new NavIdent("A678910");
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.claim("roles", "INTERN")
				.claim("NAVident", navIdent.get())
				.claim("acr", "Level4")
				.claim("oid", "00000000-0000-0001-0000-0000000003e8")
				.build();
		when(authContextHolder.requireIdTokenClaims()).thenReturn(claims);
		when(authContextHolder.erInternBruker()).thenReturn(true);
		when(authContextHolder.getIdTokenClaims()).thenReturn(Optional.of(claims));

		when(poaoTilgangClient.evaluatePolicy(any())).thenReturn(new ApiResult<>(null, new Decision.Deny("","")));

		Boolean answer = authService.harLesetilgang(fodselsnr);

		Assertions.assertEquals(false, answer);
		verify(auditLogger, times(1)).log(any(CefMessage.class));
	}

	@Test
	public void harLesetilgang_IngenRolle_Forbidden() {
		Fnr fodselsnr = new Fnr("123");
		NavIdent navIdent = new NavIdent("A678910");
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.claim("NAVident", navIdent.get())
				.claim("acr", "Level4")
				.claim("oid", "00000000-0000-0001-0000-0000000003e8")
				.build();
		when(authContextHolder.requireIdTokenClaims()).thenReturn(claims);
		when(authContextHolder.getIdTokenClaims()).thenReturn(Optional.of(claims));

		when(poaoTilgangClient.evaluatePolicy(any())).thenReturn(new ApiResult<>(null, new Decision.Deny("","")));

		Assertions.assertThrows(ResponseStatusException.class, () -> authService.harLesetilgang(fodselsnr));
	}

	@Test
	public void harLesetilgang_Systembruker_Permit() {
		Fnr fodselsnr = new Fnr("123");
		NavIdent navIdent = new NavIdent("A678910");
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.claim("roles", Collections.singletonList("access_as_application"))
				.claim("NAVident", navIdent.get())
				.claim("acr", "Level4")
				.claim("oid", "00000000-0000-0001-0000-0000000003e8")
				.claim("iss", "tokendings")
				.build();
		when(authContextHolder.requireIdTokenClaims()).thenReturn(claims);
		when(authContextHolder.erSystemBruker()).thenReturn(true);
		when(authContextHolder.getIdTokenClaims()).thenReturn(Optional.of(claims));
		when(environmentProperties.getNaisAadIssuer()).thenReturn("tokendings");

		when(poaoTilgangClient.evaluatePolicy(any())).thenReturn(new ApiResult<>(null, new Decision.Deny("","")));

		Boolean answer = authService.harLesetilgang(fodselsnr);

		Assertions.assertEquals(true, answer);
		verify(auditLogger, times(0)).log(any(CefMessage.class));
	}

	@Test
	public void harLesetilgangFamiliemedlem_Veileder_Permit() {
		Fnr fodselsnr = new Fnr("123");
		NavIdent navIdent = new NavIdent("A678910");
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.claim("roles", "INTERN")
				.claim("NAVident", navIdent.get())
				.claim("acr", "Level4")
				.claim("oid", "00000000-0000-0001-0000-0000000003e8")
				.build();
		when(authContextHolder.requireIdTokenClaims()).thenReturn(claims);
		when(authContextHolder.erInternBruker()).thenReturn(true);
		when(authContextHolder.getIdTokenClaims()).thenReturn(Optional.of(claims));

		when(poaoTilgangClient.evaluatePolicy(any())).thenReturn(new ApiResult<>(null, Decision.Permit.INSTANCE));

		Boolean answer = authService.harLesetilgangFamiliemedlem(fodselsnr);

		Assertions.assertEquals(true, answer);
		verify(poaoTilgangClient).evaluatePolicy(argThat(p -> p instanceof NavAnsattTilgangTilEksternBrukerKjernereglerPolicyInput));
	}

	@Test
	public void harLesetilgangFamiliemedlem_Veileder_Deny() {
		Fnr fodselsnr = new Fnr("123");
		NavIdent navIdent = new NavIdent("A678910");
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.claim("roles", "INTERN")
				.claim("NAVident", navIdent.get())
				.claim("acr", "Level4")
				.claim("oid", "00000000-0000-0001-0000-0000000003e8")
				.build();
		when(authContextHolder.requireIdTokenClaims()).thenReturn(claims);
		when(authContextHolder.erInternBruker()).thenReturn(true);
		when(authContextHolder.getIdTokenClaims()).thenReturn(Optional.of(claims));

		when(poaoTilgangClient.evaluatePolicy(any())).thenReturn(new ApiResult<>(null, new Decision.Deny("", "")));

		Boolean answer = authService.harLesetilgangFamiliemedlem(fodselsnr);

		Assertions.assertEquals(false, answer);
		verify(poaoTilgangClient).evaluatePolicy(argThat(p -> p instanceof NavAnsattTilgangTilEksternBrukerKjernereglerPolicyInput));
	}

	@Test
	public void harLesetilgangFamiliemedlem_EksternBrukerNivaa4_Permit() {
		Fnr fodselsnr = new Fnr("123");
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.claim("roles", "EKSTERN")
				.claim("acr", "Level4")
				.claim("pid", fodselsnr.get())
				.build();
		when(authContextHolder.getUid()).thenReturn(Optional.of(fodselsnr.get()));
		when(authContextHolder.erEksternBruker()).thenReturn(true);
		when(authContextHolder.getIdTokenClaims()).thenReturn(Optional.of(claims));

		when(poaoTilgangClient.evaluatePolicy(any())).thenReturn(new ApiResult<>(null, Decision.Permit.INSTANCE));

		Boolean answer = authService.harLesetilgangFamiliemedlem(fodselsnr);

		Assertions.assertEquals(true, answer);
		verify(auditLogger, times(1)).log(any(CefMessage.class));
	}

	@Test
	public void harLesetilgangFamiliemedlem_Systembruker_Permit() {
		Fnr fodselsnr = new Fnr("123");
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.claim("roles", Collections.singletonList("access_as_application"))
				.claim("iss", "tokendings")
				.build();
		when(authContextHolder.requireIdTokenClaims()).thenReturn(claims);
		when(authContextHolder.erSystemBruker()).thenReturn(true);
		when(authContextHolder.getIdTokenClaims()).thenReturn(Optional.of(claims));
		when(environmentProperties.getNaisAadIssuer()).thenReturn("tokendings");

		Boolean answer = authService.harLesetilgangFamiliemedlem(fodselsnr);

		Assertions.assertEquals(true, answer);
		verify(auditLogger, times(0)).log(any(CefMessage.class));
	}

	@Test
	public void harLesetilgangFamiliemedlem_IngenRolle_Forbidden() {
		Fnr fodselsnr = new Fnr("123");
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.claim("acr", "Level4")
				.build();
		when(authContextHolder.requireIdTokenClaims()).thenReturn(claims);
		when(authContextHolder.getIdTokenClaims()).thenReturn(Optional.of(claims));

		Assertions.assertThrows(ResponseStatusException.class, () -> authService.harLesetilgangFamiliemedlem(fodselsnr));
	}
}
