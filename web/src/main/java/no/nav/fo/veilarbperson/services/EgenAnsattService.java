package no.nav.fo.veilarbperson.services;


import no.nav.tjeneste.pip.egen.ansatt.v1.*;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class EgenAnsattService {

    private static final Logger LOG = getLogger(EgenAnsattService.class);

    private final EgenAnsattV1 egenAnsattV1;

    public EgenAnsattService(EgenAnsattV1 egenAnsattV1) {
        this.egenAnsattV1 = egenAnsattV1;
    }

    public boolean erEgenAnsatt(String ident) {
        final WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest request = new WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest().withIdent(ident);

        WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse wsEgenAnsatt = egenAnsattV1.hentErEgenAnsattEllerIFamilieMedEgenAnsatt(request);
        LOG.info("Kaller isEgenAnsatt");
        return wsEgenAnsatt.isEgenAnsatt();
    }

}