package no.nav.veilarbperson.consumer.organisasjonenhet;

import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.FinnNAVKontorUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.OrganisasjonEnhetV2;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.informasjon.WSGeografi;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.informasjon.WSOrganisasjonsenhet;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.meldinger.WSFinnNAVKontorRequest;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.meldinger.WSFinnNAVKontorResponse;
import no.nav.veilarbperson.config.CacheConfig;
import org.slf4j.Logger;
import org.springframework.cache.annotation.Cacheable;

import static org.slf4j.LoggerFactory.getLogger;

public class EnhetService {

    private static final Logger logger = getLogger(EnhetService.class);

    private final OrganisasjonEnhetV2 organisasjonenhet;

    public EnhetService(OrganisasjonEnhetV2 organisasjonenhet) {
        this.organisasjonenhet = organisasjonenhet;
    }

    @Cacheable(CacheConfig.ENHET)
    public Enhet hentGeografiskEnhet(String geografiskTilknytning) {
        try {
            WSFinnNAVKontorResponse response = organisasjonenhet.finnNAVKontor(lagRequest(geografiskTilknytning));
            return mapTilEnhet(response.getNAVKontor());
        } catch (FinnNAVKontorUgyldigInput e) {
            logger.error("Feil ved henting av enhet fra Norg2 " + e);
            return null;
        }
    }

    private WSFinnNAVKontorRequest lagRequest(String geografiskTilknytning) {
        return new WSFinnNAVKontorRequest().withGeografiskTilknytning(new WSGeografi().withValue(geografiskTilknytning));
    }

    private Enhet mapTilEnhet(WSOrganisasjonsenhet wsEnhet) {
        if (wsEnhet != null) {
            return new Enhet()
                    .withEnhetsnummer(wsEnhet.getEnhetId())
                    .withNavn(wsEnhet.getEnhetNavn());
        }
        return null;
    }

}
