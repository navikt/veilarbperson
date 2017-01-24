package no.nav.fo.veilarbperson.kodeverk;

import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.*;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class KodeverkTest {

    @Test
    public void getNavnHenterRiktigNavn() throws Exception {
        List<XMLKode> xmlKoder = new ArrayList<>();
        xmlKoder.add(new XMLKode().withNavn("GIF").withTerm(new XMLTerm()
                .withNavn("Gift")
                .withSpraak("nb")));
        xmlKoder.add(new XMLKode().withNavn("SKIL").withTerm(new XMLTerm()
                .withNavn("Skilt")
                .withSpraak("nb")));
        XMLEnkeltKodeverk enkeltKodeverk = new XMLEnkeltKodeverk().withKode(xmlKoder);
        Kodeverk kodeverk = new Kodeverk(enkeltKodeverk);

        Optional<String> navn = kodeverk.getNavn("GIF", "nb");

        assertThat(navn.get(), is(equalTo("Gift")));
    }

    @Test
    public void getNavnReturnererEmptyNarIngenFunn() throws Exception {
        List<XMLKode> xmlKoder = new ArrayList<>();
        xmlKoder.add(new XMLKode().withNavn("GIF").withTerm(new XMLTerm()
                .withNavn("Gift")
                .withSpraak("nb")));
        xmlKoder.add(new XMLKode().withNavn("SKIL").withTerm(new XMLTerm()
                .withNavn("Skilt")
                .withSpraak("nb")));
        XMLEnkeltKodeverk enkeltKodeverk = new XMLEnkeltKodeverk().withKode(xmlKoder);
        Kodeverk kodeverk = new Kodeverk(enkeltKodeverk);

        Optional<String> navn = kodeverk.getNavn("RAR_DEKODE", "nb");

        assertThat(navn.isPresent(), is(false));
    }

}