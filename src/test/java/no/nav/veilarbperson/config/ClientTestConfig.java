package no.nav.veilarbperson.config;

import no.nav.common.client.aktoroppslag.BrukerIdenter;
import no.nav.common.client.aktorregister.AktorregisterClient;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.featuretoggle.UnleashClient;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.health.selftest.SelfTestChecks;
import no.nav.common.rest.client.RestClient;
import no.nav.common.token_client.client.AzureAdOnBehalfOfTokenClient;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.EksternBrukerId;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.difi.DifiCient;
import no.nav.veilarbperson.client.difi.HarLoggetInnRespons;
import no.nav.veilarbperson.client.dkif.DkifClient;
import no.nav.veilarbperson.client.dkif.DkifKontaktinfo;
import no.nav.veilarbperson.client.egenansatt.EgenAnsattClient;
import no.nav.veilarbperson.client.kodeverk.KodeverkClient;
import no.nav.veilarbperson.client.nom.SkjermetClient;
import no.nav.veilarbperson.client.pam.PamClient;
import no.nav.veilarbperson.client.pdl.HentPerson;
import no.nav.veilarbperson.client.pdl.PdlAuth;
import no.nav.veilarbperson.client.pdl.PdlClient;
import no.nav.veilarbperson.client.person.PersonClient;
import no.nav.veilarbperson.client.person.domain.TpsPerson;
import no.nav.veilarbperson.client.veilarboppfolging.UnderOppfolging;
import no.nav.veilarbperson.client.veilarboppfolging.VeilarboppfolgingClient;
import no.nav.veilarbperson.client.veilarbportefolje.Personinfo;
import no.nav.veilarbperson.client.veilarbportefolje.VeilarbportefoljeClient;
import no.nav.veilarbperson.client.veilarbregistrering.VeilarbregistreringClient;
import no.nav.veilarbperson.client.veilarbregistrering.VeilarbregistreringClientImpl;
import okhttp3.Response;
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
    public AktorregisterClient aktorregisterClient() {
        return new AktorregisterClient() {
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

            @Override
            public List<AktorId> hentAktorIder(Fnr fnr) {
                return List.of(new AktorId(fnr.get()), new AktorId("1000010101001"));
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
            public no.nav.common.client.norg2.Enhet hentTilhorendeEnhet(String s) {
                return null;
            }

            @Override
            public HealthCheckResult checkHealth() {
                return HealthCheckResult.healthy();
            }
        };
    }

    @Bean
    public DkifClient dkifClient() {
        return new DkifClient() {
            @Override
            public DkifKontaktinfo hentKontaktInfo(Fnr fnr) {
                return new DkifKontaktinfo();
            }

            @Override
            public HealthCheckResult checkHealth() {
                return HealthCheckResult.healthy();
            }
        };
    }

    @Bean
    public DifiCient difiCient() {
        return new DifiCient() {
            @Override
            public HarLoggetInnRespons harLoggetInnSiste18mnd(Fnr fnr) {
                HarLoggetInnRespons harLoggetInnRespons = new HarLoggetInnRespons();
                harLoggetInnRespons.setHarbruktnivaa4(true);
//                harLoggetInnRespons.setPersonidentifikator(fnr);
                return harLoggetInnRespons;
            }
        };
    }

    @Bean
    public EgenAnsattClient egenAnsattClient() {
        return new EgenAnsattClient() {
            @Override
            public boolean erEgenAnsatt(Fnr ident) {
                return false;
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
    public PersonClient personClient() {
        return new PersonClient() {
            @Override
            public TpsPerson hentPerson(Fnr ident) {
                return new TpsPerson()
                        .setFornavn("Test")
                        .setEtternavn("Testersen")
                        .setSammensattNavn("Test Testersen")
                        .setFodselsnummer(ident);
            }

            @Override
            public String hentSikkerhetstiltak(Fnr ident) {
                return "sikkerhetstiltak";
            }

            @Override
            public HealthCheckResult checkHealth() {
                return HealthCheckResult.healthy();
            }
        };
    }

    @Bean
    public VeilarbportefoljeClient veilarbportefoljeClient() {
        return new VeilarbportefoljeClient() {
            @Override
            public Personinfo hentPersonInfo(Fnr fodselsnummer) {
                return new Personinfo();
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
            public HentPerson.Person hentPerson(Fnr personIdent, PdlAuth auth) {
                return null;
            }

            @Override
            public HentPerson.VergeOgFullmakt hentVergeOgFullmakt(Fnr personIdent, PdlAuth auth) {
                return null;
            }

            @Override
            public HentPerson.PersonNavn hentPersonNavn(Fnr personIdent, PdlAuth auth) {
                return null;
            }

            @Override
            public List<HentPerson.PersonFraBolk> hentPersonBolk(List<Fnr> personIdenter, PdlAuth auth) {
                return null;
            }

            @Override
            public HentPerson.GeografiskTilknytning hentGeografiskTilknytning(Fnr personIdent, PdlAuth auth) {
                return null;
            }

            @Override
            public HentPerson.HentSpraakTolk hentTilrettelagtKommunikasjon(Fnr personIdent, PdlAuth auth) {
                return null;
            }

            @Override
            public HealthCheckResult checkHealth() {
                return HealthCheckResult.healthy();
            }
        };
    }

    @Bean
    public PamClient pamClient() {
        return new PamClient() {
            @Override
            public Response hentCvOgJobbprofil(Fnr fnr, boolean erBrukerManuell) {
                return null;
            }

            @Override
            public HealthCheckResult checkHealth() {
                return HealthCheckResult.healthy();
            }
        };
    }

    @Bean
    public UnleashClient unleashClient() {
        return mock(UnleashClient.class);
    }

    @Bean
    public SelfTestChecks selfTestChecks() {
        return mock(SelfTestChecks.class);
    }

    @Bean
    public VeilarbregistreringClient veilarbregistreringClient() {
        return new VeilarbregistreringClientImpl(
                RestClient.baseClient(), "http://localhost:" + WIREMOCK_PORT, () -> "");
    }

    @Bean
    public AzureAdOnBehalfOfTokenClient azureAdOnBehalfOfTokenClient() {
        return mock(AzureAdOnBehalfOfTokenClient.class);
    }
}
