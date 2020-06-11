package no.nav.veilarbperson.client.kodeverk;

import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLEnkeltKodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLKode;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLTerm;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class KodeverkTest {

    @Test
    public void getNavnHenterRiktigNavn() {
        List<XMLKode> xmlKoder = new ArrayList<>();
        xmlKoder.add(new XMLKode().withNavn("GIF").withTerm(new XMLTerm()
                .withNavn("Gift")
                .withSpraak("nb")));
        xmlKoder.add(new XMLKode().withNavn("SKIL").withTerm(new XMLTerm()
                .withNavn("Skilt")
                .withSpraak("nb")));
        XMLEnkeltKodeverk enkeltKodeverk = new XMLEnkeltKodeverk().withKode(xmlKoder);
        Kodeverk kodeverk = new KodeverkImpl(enkeltKodeverk);

        String navn = kodeverk.getNavn("GIF", "nb");

        assertThat(navn, is(equalTo("Gift")));
    }

    @Test
    public void getNavnReturnererKodeNarIngenFunn() {
        final String RAR_DEKODE = "RAR_DEKODE";

        List<XMLKode> xmlKoder = new ArrayList<>();
        xmlKoder.add(new XMLKode().withNavn("GIF").withTerm(new XMLTerm()
                .withNavn("Gift")
                .withSpraak("nb")));
        xmlKoder.add(new XMLKode().withNavn("SKIL").withTerm(new XMLTerm()
                .withNavn("Skilt")
                .withSpraak("nb")));
        XMLEnkeltKodeverk enkeltKodeverk = new XMLEnkeltKodeverk().withKode(xmlKoder);
        Kodeverk kodeverk = new KodeverkImpl(enkeltKodeverk);

        String navn = kodeverk.getNavn(RAR_DEKODE, "nb");

        assertThat(navn, is(equalTo(RAR_DEKODE)));
    }

}