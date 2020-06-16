package no.nav.veilarbperson.controller;

import no.nav.common.abac.Pep;
import no.nav.common.client.aktorregister.AktorregisterClient;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.health.selftest.SelfTestCheck;
import no.nav.common.health.selftest.SelfTestUtils;
import no.nav.common.health.selftest.SelftTestCheckResult;
import no.nav.common.health.selftest.SelftestHtmlGenerator;
import no.nav.veilarbperson.client.dkif.DkifClient;
import no.nav.veilarbperson.client.egenansatt.EgenAnsattClient;
import no.nav.veilarbperson.client.kodeverk.KodeverkClient;
import no.nav.veilarbperson.client.person.PersonClient;
import no.nav.veilarbperson.client.veilarbportefolje.VeilarbportefoljeClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

import static no.nav.common.health.selftest.SelfTestUtils.checkAllParallel;

@RestController
@RequestMapping("/internal")
public class InternalController {

    private final List<SelfTestCheck> selftestChecks;

    @Autowired
    public InternalController(
            AktorregisterClient aktorregisterClient,
            Pep veilarbPep,
            DkifClient dkifClient,
            EgenAnsattClient egenAnsattClient,
            KodeverkClient kodeverkClient,
            PersonClient personClient,
            VeilarbportefoljeClient veilarbportefoljeClient,
            Norg2Client norg2Client
    ) {
        this.selftestChecks = Arrays.asList(
                new SelfTestCheck("Aktorregister", true, aktorregisterClient),
                new SelfTestCheck("ABAC", true, veilarbPep.getAbacClient()),
                new SelfTestCheck("Digitalkontakinformasjon (DKIF)", false, dkifClient),
                new SelfTestCheck("EgenAnsatt_v1 (SOAP) ", false, egenAnsattClient),
                new SelfTestCheck("Felles kodeverk", false, kodeverkClient),
                new SelfTestCheck("Person_v3 (SOAP)", true, personClient),
                new SelfTestCheck("Veilarbportefolje", false, veilarbportefoljeClient),
                new SelfTestCheck("Norg2", false, norg2Client)
        );
    }

    @GetMapping("/isReady")
    public void isReady() {}

    @GetMapping("/isAlive")
    public void isAlive() {}

    @GetMapping("/selftest")
    public ResponseEntity selftest() {
        List<SelftTestCheckResult> checkResults = checkAllParallel(selftestChecks);
        String html = SelftestHtmlGenerator.generate(checkResults);
        int status = SelfTestUtils.findHttpStatusCode(checkResults, true);

        return ResponseEntity
                .status(status)
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }


}

