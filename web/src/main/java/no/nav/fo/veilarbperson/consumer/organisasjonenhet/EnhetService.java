package no.nav.fo.veilarbperson.consumer.organisasjonenhet;

import no.nav.tjeneste.virksomhet.organisasjonenhet.v1.HentEnhetBolkUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v1.OrganisasjonEnhetV1;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v1.informasjon.WSDetaljertEnhet;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v1.meldinger.WSHentEnhetBolkRequest;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v1.meldinger.WSHentEnhetBolkResponse;

import java.util.List;

public class EnhetService {

    private final OrganisasjonEnhetV1 organisasjonenhet;

    public EnhetService(OrganisasjonEnhetV1 organisasjonenhet) {
        this.organisasjonenhet = organisasjonenhet;
    }

    public Enhet hentBehandlendeEnhet(String geografiskNedslagsfelt) {
        WSHentEnhetBolkRequest request = new WSHentEnhetBolkRequest().withEnhetIdListe(geografiskNedslagsfelt);

        try {
            return mapTilEnhet(hentEnhet(organisasjonenhet.hentEnhetBolk(request)));
        } catch (HentEnhetBolkUgyldigInput hentEnhetBolkUgyldigInput) {
            hentEnhetBolkUgyldigInput.printStackTrace();
            return null;
        }
    }

    private Enhet mapTilEnhet(WSDetaljertEnhet wsEnhet) {
        if (wsEnhet != null) {
            return new Enhet()
                    .withEnhetsnummer(wsEnhet.getEnhetId())
                    .withNavn(wsEnhet.getNavn());
        }
        return null;
    }

    private WSDetaljertEnhet hentEnhet(WSHentEnhetBolkResponse response) {
        List<WSDetaljertEnhet> liste = response.getEnhetListe();
        return liste.isEmpty() ? null : liste.get(0);
    }

}
