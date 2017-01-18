package no.nav.fo.veilarbperson.services;
//
import no.nav.tjeneste.pip.egenansatt.v1.EgenAnsattV1;
import no.nav.tjeneste.pip.egenansatt.v1.meldinger.HentErEgenAnsattEllerIFamilieMedEgenAnsattRequest;
import no.nav.tjeneste.pip.egenansatt.v1.meldinger.HentErEgenAnsattEllerIFamilieMedEgenAnsattResponse;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import static org.slf4j.LoggerFactory.getLogger;

public class EgenAnsattService {

    private static final Logger logger = getLogger(EgenAnsattService.class);

    @Autowired
    private EgenAnsattV1 egenAnsattV1;

    public boolean erEgenAnsatt(String ident) {
        final HentErEgenAnsattEllerIFamilieMedEgenAnsattRequest request = new HentErEgenAnsattEllerIFamilieMedEgenAnsattRequest().withIdent(ident);

        HentErEgenAnsattEllerIFamilieMedEgenAnsattResponse wsEgenAnsatt = egenAnsattV1.hentErEgenAnsattEllerIFamilieMedEgenAnsatt(request);
        return wsEgenAnsatt.isEgenAnsatt();
    }

}
