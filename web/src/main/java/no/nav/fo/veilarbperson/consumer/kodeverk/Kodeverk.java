package no.nav.fo.veilarbperson.consumer.kodeverk;

import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Kodeverk {

    private final List<XMLKode> koder;

    Kodeverk(XMLEnkeltKodeverk kodeverkResponse) {
        this.koder = kodeverkResponse.getKode();
    }

    Optional<String> getNavn(String kode, String sprak) {
        Stream<XMLKode> relevanteKoder = koder.stream()
                .filter(kodeverkselement -> kodeverkselement.getNavn().equals(kode));

        return relevanteKoder.flatMap(relevantKode -> relevantKode.getTerm()
                .stream()
                .filter(term -> term.getSpraak().equals(sprak)))
                .map(XMLIdentifiserbarEntitet::getNavn)
                .findFirst();

    }
}
