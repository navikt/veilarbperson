package no.nav.veilarbperson.consumer.kodeverk;

import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLEnkeltKodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLIdentifiserbarEntitet;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLKode;

import java.util.List;
import java.util.stream.Stream;

public class KodeverkImpl implements Kodeverk {

    private final List<XMLKode> koder;

    public KodeverkImpl(XMLEnkeltKodeverk kodeverkResponse) {
        this.koder = kodeverkResponse.getKode();
    }

    public String getNavn(String kode, String sprak) {
        Stream<XMLKode> relevanteKoder = koder.stream()
                .filter(kodeverkselement -> kodeverkselement.getNavn().equals(kode));

        return relevanteKoder
                .flatMap(relevantKode -> relevantKode
                        .getTerm()
                        .stream()
                        .filter(term -> term.getSpraak().equals(sprak))
                )
                .map(XMLIdentifiserbarEntitet::getNavn)
                .findFirst().orElse(kode);
    }
}
