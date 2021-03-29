package no.nav.veilarbperson.config;

import no.nav.common.client.aktorregister.AktorregisterClient;
import no.nav.common.client.aktorregister.IdentOppslag;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.featuretoggle.UnleashService;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.health.selftest.SelfTestChecks;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.difi.DifiCient;
import no.nav.veilarbperson.client.difi.HarLoggetInnRespons;
import no.nav.veilarbperson.client.dkif.DkifClient;
import no.nav.veilarbperson.client.dkif.DkifKontaktinfo;
import no.nav.veilarbperson.client.egenansatt.EgenAnsattClient;
import no.nav.veilarbperson.client.kodeverk.KodeverkClient;
import no.nav.veilarbperson.client.pam.PamClient;
import no.nav.veilarbperson.client.pdl.HentPdlPerson;
import no.nav.veilarbperson.client.pdl.PdlClient;
import no.nav.veilarbperson.client.person.PersonClient;
import no.nav.veilarbperson.client.person.domain.TpsPerson;
import no.nav.veilarbperson.client.veilarboppfolging.UnderOppfolging;
import no.nav.veilarbperson.client.veilarboppfolging.VeilarboppfolgingClient;
import no.nav.veilarbperson.client.veilarbportefolje.Personinfo;
import no.nav.veilarbperson.client.veilarbportefolje.VeilarbportefoljeClient;
import okhttp3.Response;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static no.nav.veilarbperson.utils.TestData.TEST_AKTOR_ID;
import static no.nav.veilarbperson.utils.TestData.TEST_FNR;


@Configuration
public class ClientTestConfig {

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
            public List<IdentOppslag> hentFnr(List<AktorId> aktorIdListe) {
                return null;
            }

            @Override
            public List<IdentOppslag> hentAktorId(List<Fnr> fnrListe) {
                return null;
            }

            @Override
            public List<AktorId> hentAktorIder(Fnr fnr) {
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
            public HentPdlPerson.PdlPerson hentPerson(Fnr personIdent, String userToken) {
                return null;
            }

            @Override
            public HentPdlPerson.Familiemedlem hentPartner(Fnr personIdent, String userToken) {
                return null;
            }

            @Override
            public HentPdlPerson.PersonNavn hentPersonNavn(Fnr personIdent, String userToken) { return null; }

            @Override
            public HentPdlPerson.VergeOgFullmakt hentVergeOgFullmakt(Fnr personIdent, String userToken) { return null; }

            @Override
            public List<HentPdlPerson.Barn> hentPersonBolk(Fnr[] personIdent) {
                return null;
            }

            @Override
            public HentPdlPerson.GeografiskTilknytning hentGeografiskTilknytning(Fnr personIdent, String userToken) { return null; }

            @Override
            public HentPdlPerson.HentSpraakTolk hentTilrettelagtKommunikasjon(Fnr personIdent, String userToken) { return null; }

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
            public String hentCvOgJobbprofilJson(Fnr fnr) {
                return null;
            }

            @Override
            public Response hentCvOgJobbprofilJsonV2(Fnr fnr, boolean erBrukerManuell) {
                return null;
            }

            @Override
            public HealthCheckResult checkHealth() {
                return HealthCheckResult.healthy();
            }
        };
    }

    @Bean
    public UnleashService unleashService() {
        return Mockito.mock(UnleashService.class);
    }

    @Bean
    public SelfTestChecks selfTestChecks() {
        return Mockito.mock(SelfTestChecks.class);
    }


}
