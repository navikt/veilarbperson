package no.nav.fo.veilarbperson.services;

import no.nav.tjeneste.virksomhet.organisasjonenhet.v1.FinnNAVKontorForGeografiskNedslagsfeltBolkUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v1.OrganisasjonEnhetV1;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v1.informasjon.WSDetaljertEnhet;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v1.meldinger.WSFinnNAVKontorForGeografiskNedslagsfeltBolkRequest;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v1.meldinger.WSFinnNAVKontorForGeografiskNedslagsfeltBolkResponse;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class EnhetService {

    private final OrganisasjonEnhetV1 organisasjonenhet;

    public EnhetService(OrganisasjonEnhetV1 organisasjonenhet) {
        this.organisasjonenhet = organisasjonenhet;
    }

    public Enhet hentBehandlendeEnhet(String geografiskNedslagsfelt) {
        WSFinnNAVKontorForGeografiskNedslagsfeltBolkRequest request = new WSFinnNAVKontorForGeografiskNedslagsfeltBolkRequest().withGeografiskNedslagsfeltListe(geografiskNedslagsfelt);
        WSFinnNAVKontorForGeografiskNedslagsfeltBolkResponse response = null;
        try {
            response = organisasjonenhet.finnNAVKontorForGeografiskNedslagsfeltBolk(request);
        } catch (FinnNAVKontorForGeografiskNedslagsfeltBolkUgyldigInput finnNAVKontorForGeografiskNedslagsfeltBolkUgyldigInput) {
            finnNAVKontorForGeografiskNedslagsfeltBolkUgyldigInput.printStackTrace();
        }
        WSDetaljertEnhet wsEnhet = hentEnhet(response);
        return  mapTilEnhet(wsEnhet);
    }

    private Enhet mapTilEnhet(WSDetaljertEnhet wsEnhet) {
        if (wsEnhet != null) {
            return new Enhet()
                    .withEnhetsnummer(wsEnhet.getEnhetId())
                    .withNavn(wsEnhet.getNavn());
        }
        return null;
    }

    private WSDetaljertEnhet hentEnhet(WSFinnNAVKontorForGeografiskNedslagsfeltBolkResponse response) {
        if (responseHarEnhet(response)) {
            return response.getEnheterForGeografiskNedslagsfeltListe().get(0).getEnhetListe().get(0);
        }
        return null;
    }

    private boolean responseHarEnhet(WSFinnNAVKontorForGeografiskNedslagsfeltBolkResponse response) {
        return response != null
                && listeErIkkeTom(response.getEnheterForGeografiskNedslagsfeltListe())
                && listeErIkkeTom(response.getEnheterForGeografiskNedslagsfeltListe().get(0).getEnhetListe());
    }

    private boolean listeErIkkeTom(List<?> liste) {
        return liste != null
                && liste.size() > 0
                && liste.get(0) != null;
    }
}
