package no.nav.veilarbperson.service;

import lombok.RequiredArgsConstructor;
import no.nav.common.featuretoggle.UnleashClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnleashService {
	private static final String UNLEASH_NIVAA4_DISABLED = "veilarbperson.nivaa4.disabled";
	private static final String UNLEASH_POAO_TILGANG_ENABLED = "veilarbperson.poao-tilgang-enabled";
	private final UnleashClient unleashClient;

	public boolean skalBrukePoaoTilgang() {
		return unleashClient.isEnabled(UNLEASH_POAO_TILGANG_ENABLED);
	}

	public boolean sjekkNivaa4() {
		return unleashClient.isEnabled(UNLEASH_NIVAA4_DISABLED);
	}

}