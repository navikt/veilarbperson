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
import no.nav.poao_tilgang.client.PoaoTilgangClient;
import no.nav.poao_tilgang.client.api.ApiResult;
import no.nav.veilarbperson.config.EnvironmentProperties;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
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
	public void sjekkHarTilgangEksternBruker_Permit() {
		Fnr FNR = new Fnr("123");
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.claim("roles", "EKSTERN")
				.claim("acr", "Level4")
				.claim("pid", FNR.get())
				.build();
		when(authContextHolder.getUid()).thenReturn(Optional.of(FNR.get()));
		when(authContextHolder.erEksternBruker()).thenReturn(true);
		when(authContextHolder.getIdTokenClaims()).thenReturn(Optional.of(claims));

		when(poaoTilgangClient.evaluatePolicy(any())).thenReturn(new ApiResult<>(null, Decision.Permit.INSTANCE));

		Boolean answer = authService.harLesetilgang(FNR);

		Assertions.assertEquals(answer, true);
		verify(auditLogger, times(1)).log(any(CefMessage.class));

	}

	@Test
	public void sjekkHarTilgangEksternBrukerIkkeNivaa4_Deny() {
		Fnr FNR = new Fnr("123");
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.claim("roles", "EKSTERN")
				.claim("acr", "Level3")
				.claim("pid", FNR.get())
				.build();
		when(authContextHolder.getUid()).thenReturn(Optional.of(FNR.get()));
		when(authContextHolder.erEksternBruker()).thenReturn(true);
		when(authContextHolder.getIdTokenClaims()).thenReturn(Optional.of(claims));

		when(poaoTilgangClient.evaluatePolicy(any())).thenReturn(new ApiResult<>(null, Decision.Permit.INSTANCE));


		Assertions.assertThrows(ResponseStatusException.class, () -> authService.harLesetilgang(FNR));
		verify(auditLogger, times(0)).log(any(CefMessage.class));

	}

	@Test
	public void sjekkHarTilgangEksternBruker_Deny() {
		Fnr FNR = new Fnr("123");
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.claim("roles", "EKSTERN")
				.claim("acr", "Level4")
				.claim("pid", "4321")
				.build();
		when(authContextHolder.getUid()).thenReturn(Optional.of("4321"));
		when(authContextHolder.erEksternBruker()).thenReturn(true);
		when(authContextHolder.getIdTokenClaims()).thenReturn(Optional.of(claims));

		when(poaoTilgangClient.evaluatePolicy(any())).thenReturn(new ApiResult<>(null, new Decision.Deny("","")));

		Boolean answer = authService.harLesetilgang(FNR);

		Assertions.assertEquals(answer, false);
		verify(auditLogger, times(1)).log(any(CefMessage.class));
	}

	@Test
	public void sjekkVeilederHarTilgangEksternBruker_Permit() {
		Fnr FNR = new Fnr("123");
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

		Boolean answer = authService.harLesetilgang(FNR);

		Assertions.assertEquals(answer, true);
		verify(auditLogger, times(1)).log(any(CefMessage.class));

	}

	@Test
	public void sjekkVeilederHarTilgangEksternBruker_Deny() {
		Fnr FNR = new Fnr("123");
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

		Boolean answer = authService.harLesetilgang(FNR);

		Assertions.assertEquals(answer, false);
		verify(auditLogger, times(1)).log(any(CefMessage.class));
	}

	@Test
	public void sjekkSystembrukerHarTilgang_Permit() {
		Fnr FNR = new Fnr("123");
		NavIdent navIdent = new NavIdent("A678910");
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.claim("NAVident", navIdent.get())
				.claim("acr", "Level4")
				.claim("oid", "00000000-0000-0001-0000-0000000003e8")
				.build();
		when(authContextHolder.requireIdTokenClaims()).thenReturn(claims);
		when(authContextHolder.getIdTokenClaims()).thenReturn(Optional.of(claims));

		when(poaoTilgangClient.evaluatePolicy(any())).thenReturn(new ApiResult<>(null, new Decision.Deny("","")));

		Assertions.assertThrows(ResponseStatusException.class, () -> authService.harLesetilgang(FNR));
	}

	@Test
	public void sjekkIkkeRolleIkkeTilgang() {
		Fnr FNR = new Fnr("123");
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

		Boolean answer = authService.harLesetilgang(FNR);

		Assertions.assertEquals(answer, true);
		verify(auditLogger, times(0)).log(any(CefMessage.class));
	}
}
