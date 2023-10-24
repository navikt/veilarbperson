package no.nav.veilarbperson.config;

import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.health.selftest.SelfTestCheck;
import no.nav.common.health.selftest.SelfTestChecks;
import no.nav.common.health.selftest.SelfTestMeterBinder;
import no.nav.veilarbperson.client.digdir.DigdirClient;
import no.nav.veilarbperson.client.kodeverk.KodeverkClient;
import no.nav.veilarbperson.client.nom.SkjermetClient;
import no.nav.veilarbperson.client.pdl.PdlClient;
import no.nav.veilarbperson.client.regoppslag.RegoppslagClient;
import no.nav.veilarbperson.client.veilarbregistrering.VeilarbregistreringClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SelftTestConfig {

    @Bean
    public SelfTestChecks selfTestChecks(
            AktorOppslagClient aktorOppslagClient,
            DigdirClient digdirClient,
            KodeverkClient kodeverkClient,
            SkjermetClient skjermetClient,
            Norg2Client norg2Client,
            PdlClient pdlClient,
            VeilarbregistreringClient veilarbregistreringClient,
            RegoppslagClient regoppslagClient
    ) {
        return new SelfTestChecks(List.of(
                new SelfTestCheck("AktorOppslagClient", true, aktorOppslagClient),
                new SelfTestCheck("Digitalkontakinformasjon (DIGDIR)", false, digdirClient),
                new SelfTestCheck("Felles kodeverk", false, kodeverkClient),
                new SelfTestCheck("Norg2", false, norg2Client),
                new SelfTestCheck("PDL", true, pdlClient),
                new SelfTestCheck("skjermede-personer", true, skjermetClient),
                new SelfTestCheck("Veilarbregistrering", false, veilarbregistreringClient),
                new SelfTestCheck("Regoppslag", false, regoppslagClient)
        ));
    }

    @Bean
    public SelfTestMeterBinder selfTestMeterBinder(SelfTestChecks selfTestChecks) {
        return new SelfTestMeterBinder(selfTestChecks);
    }

}
