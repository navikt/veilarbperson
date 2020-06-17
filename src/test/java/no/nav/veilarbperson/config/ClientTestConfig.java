package no.nav.veilarbperson.config;

import no.nav.common.client.aktorregister.AktorregisterClient;
import no.nav.common.client.aktorregister.IdentOppslag;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.health.HealthCheckResult;
import no.nav.veilarbperson.client.dkif.DkifClient;
import no.nav.veilarbperson.client.dkif.DkifKontaktinfo;
import no.nav.veilarbperson.client.egenansatt.EgenAnsattClient;
import no.nav.veilarbperson.client.kodeverk.KodeverkClient;
import no.nav.veilarbperson.client.person.PersonClient;
import no.nav.veilarbperson.client.person.domain.TpsPerson;
import no.nav.veilarbperson.client.veilarbportefolje.Personinfo;
import no.nav.veilarbperson.client.veilarbportefolje.VeilarbportefoljeClient;
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
            public String hentFnr(String s) {
                return TEST_FNR;
            }

            @Override
            public String hentAktorId(String s) {
                return TEST_AKTOR_ID;
            }

            @Override
            public List<IdentOppslag> hentFnr(List<String> list) {
                return Collections.singletonList(new IdentOppslag(TEST_AKTOR_ID, TEST_FNR));
            }

            @Override
            public List<IdentOppslag> hentAktorId(List<String> list) {
                return Collections.singletonList(new IdentOppslag(TEST_FNR, TEST_AKTOR_ID));
            }

            @Override
            public HealthCheckResult checkHealth() {
                return HealthCheckResult.healthy();
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
            public DkifKontaktinfo hentKontaktInfo(String fnr) {
                return new DkifKontaktinfo();
            }

            @Override
            public HealthCheckResult checkHealth() {
                return HealthCheckResult.healthy();
            }
        };
    }

    @Bean
    public EgenAnsattClient egenAnsattClient() {
        return new EgenAnsattClient() {
            @Override
            public boolean erEgenAnsatt(String ident) {
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
            public TpsPerson hentPerson(String ident) {
                return new TpsPerson()
                        .setFornavn("Test")
                        .setEtternavn("Testersen")
                        .setSammensattNavn("Test Testersen")
                        .setFodselsnummer(ident);
            }

            @Override
            public String hentSikkerhetstiltak(String ident) {
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
            public Personinfo hentPersonInfo(String fodselsnummer) {
                return new Personinfo();
            }

            @Override
            public HealthCheckResult checkHealth() {
                return HealthCheckResult.healthy();
            }
        };
    }

}
