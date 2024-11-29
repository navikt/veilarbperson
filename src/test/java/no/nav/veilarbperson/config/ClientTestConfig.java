package no.nav.veilarbperson.config;

import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.client.aktoroppslag.BrukerIdenter;
import no.nav.common.client.norg2.Enhet;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.health.selftest.SelfTestChecks;
import no.nav.common.metrics.MetricsClient;
import no.nav.common.token_client.client.AzureAdOnBehalfOfTokenClient;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.EksternBrukerId;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.digdir.DigdirClient;
import no.nav.veilarbperson.client.digdir.DigdirKontaktinfo;
import no.nav.veilarbperson.client.kodeverk.KodeverkClient;
import no.nav.veilarbperson.client.nom.SkjermetClient;
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.OppslagArbeidssoekerregisteretClient;
import no.nav.veilarbperson.client.oppslagArbeidssoekerregisteret.OppslagArbeidssoekerregisteretClientImpl;
import no.nav.veilarbperson.client.pdl.HentPerson;
import no.nav.veilarbperson.client.pdl.PdlClient;
import no.nav.veilarbperson.client.pdl.domain.PdlRequest;
import no.nav.veilarbperson.client.veilarboppfolging.UnderOppfolging;
import no.nav.veilarbperson.client.veilarboppfolging.VeilarboppfolgingClient;
import org.springframework.cloud.contract.wiremock.WireMockConfiguration;
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static no.nav.veilarbperson.utils.TestData.TEST_AKTOR_ID;
import static no.nav.veilarbperson.utils.TestData.TEST_FNR;
import static org.mockito.Mockito.mock;

@Configuration
@Import({WireMockConfiguration.class})
public class ClientTestConfig {

    public static final int WIREMOCK_PORT = 8081;

    @Bean
    WireMockConfigurationCustomizer optionsCustomizer() {
        return config -> config.port(WIREMOCK_PORT);
    }

    @Bean
    public AktorOppslagClient aktorOppslagClient() {
        return new AktorOppslagClient() {
            @Override
            public HealthCheckResult checkHealth() {
                return HealthCheckResult.healthy();
            }

            @Override
            public Fnr hentFnr(AktorId aktorId) {
                return TEST_FNR;
            }

            @Override
            public AktorId hentAktorId(Fnr fnr) {
                return TEST_AKTOR_ID;
            }

            @Override
            public Map<AktorId, Fnr> hentFnrBolk(List<AktorId> list) {
                return null;
            }

            @Override
            public Map<Fnr, AktorId> hentAktorIdBolk(List<Fnr> list) {
                return null;
            }

            @Override
            public BrukerIdenter hentIdenter(EksternBrukerId brukerId) {
                return null;
            }
        };
    }

    @Bean
    public Norg2Client norg2Client() {
        return new Norg2Client() {
            @Override
            public List<no.nav.common.client.norg2.Enhet> alleAktiveEnheter() {
                return Collections.emptyList();
            }

            @Override
            public no.nav.common.client.norg2.Enhet hentEnhet(String s) {
                return null;
            }

            @Override
            public Enhet hentTilhorendeEnhet(String s, Diskresjonskode diskresjonskode, boolean b) {
                return null;
            }

            @Override
            public HealthCheckResult checkHealth() {
                return HealthCheckResult.healthy();
            }
        };
    }

    @Bean
    public DigdirClient digdirClient() {
        return new DigdirClient() {
            @Override
            public DigdirKontaktinfo hentKontaktInfo(Fnr fnr) {
                return new DigdirKontaktinfo();
            }

            @Override
            public HealthCheckResult checkHealth() {
                return HealthCheckResult.healthy();
            }
        };
    }

    @Bean
    public SkjermetClient skjermetClient() {
        return new SkjermetClient() {
            @Override
            public Boolean hentSkjermet(Fnr fodselsnummer) {
                return false;
            }

            @Override
            public HealthCheckResult checkHealth() {
                return HealthCheckResult.healthy();
            }
        };
    }

    @Bean
    public KodeverkClient kodeverkClient() {
        return new KodeverkClient() {
            @Override
            public Map<String, String> hentKodeverkBeskrivelser(String kodeverksnavn) {
                return Collections.emptyMap();
            }

            @Override
            public HealthCheckResult checkHealth() {
                return HealthCheckResult.healthy();
            }
        };
    }


    @Bean
    public VeilarboppfolgingClient veilarboppfolgingClient() {
        return new VeilarboppfolgingClient() {
            @Override
            public UnderOppfolging hentUnderOppfolgingStatus(Fnr fnr) {
                return null;
            }

            @Override
            public HealthCheckResult checkHealth() {
                return HealthCheckResult.healthy();
            }
        };
    }

    @Bean
    public PdlClient pdlClient() {
        return new PdlClient() {

            @Override
            public HentPerson.Person hentPerson(PdlRequest pdlRequest) {
                return null;
            }

            @Override
            public HentPerson.Verge hentVerge(PdlRequest pdlRequest) {
                return null;
            }

            @Override
            public HentPerson.PersonNavn hentPersonNavn(PdlRequest pdlRequest) {
                return null;
            }

            @Override
            public List<HentPerson.PersonFraBolk> hentPersonBolk(List<Fnr> personIdenter, String behandlingsnummer) {
                return null;
            }

            @Override
            public HentPerson.GeografiskTilknytning hentGeografiskTilknytning(PdlRequest pdlRequest) {
                return null;
            }

            @Override
            public HentPerson.HentSpraakTolk hentTilrettelagtKommunikasjon(PdlRequest pdlRequest) {
                return null;
            }

            @Override
            public HealthCheckResult checkHealth() {
                return HealthCheckResult.healthy();
            }
        };
    }

    @Bean
    public SelfTestChecks selfTestChecks() {
        return mock(SelfTestChecks.class);
    }

    @Bean
    public OppslagArbeidssoekerregisteretClient oppslagArbeidssoekerregisteretClient() {
        return new OppslagArbeidssoekerregisteretClientImpl(
                "http://localhost:" + WIREMOCK_PORT, () -> "", "veilarbperson");
    }

    @Bean
    public AzureAdOnBehalfOfTokenClient azureAdOnBehalfOfTokenClient() {
        return mock(AzureAdOnBehalfOfTokenClient.class);
    }

    @Bean
    public MetricsClient metricsClient() {
        return mock(MetricsClient.class);
    }

}
