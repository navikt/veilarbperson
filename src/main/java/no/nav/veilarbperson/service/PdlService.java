package no.nav.veilarbperson.service;

import no.nav.veilarbperson.client.pdl.HentPersonData;
import no.nav.veilarbperson.client.pdl.PdlClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PdlService {

    private final PdlClient pdlClient;

    private final AuthService authService;

    @Autowired
    public PdlService(PdlClient pdlClient, AuthService authService) {
        this.pdlClient = pdlClient;
        this.authService = authService;
    }

    public HentPersonData.PdlPerson hentPerson(String personIdent) {
        return pdlClient.hentPerson(personIdent, authService.getInnloggetBrukerToken());
    }

}
