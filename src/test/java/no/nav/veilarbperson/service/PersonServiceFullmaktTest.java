package no.nav.veilarbperson.service;

import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.pdl.HentPerson;
import no.nav.veilarbperson.client.pdl.PdlClient;
import no.nav.veilarbperson.client.pdl.domain.PdlRequest;
import no.nav.veilarbperson.client.representasjon.ReprFullmaktV2Data;
import no.nav.veilarbperson.client.representasjon.RepresentasjonClient;
import no.nav.veilarbperson.domain.FullmaktDTO;
import no.nav.veilarbperson.domain.PersonRequest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PersonServiceFullmaktTest {
    private static final Fnr GIVER = Fnr.of("01010112345");
    private static final Fnr MEKTIG = Fnr.of("02020212345");
    private static final Fnr UTGAATT_MEKTIG = Fnr.of("03030312345");
    private static final LocalDate IDAG = LocalDate.of(2026, 10, 22);
    private static final PersonRequest REQUEST = new PersonRequest(GIVER, "B555");
    private final PdlClient pdlClient = mock(PdlClient.class);
    private final RepresentasjonClient representasjonClient = mock(RepresentasjonClient.class);
    private final KodeverkService kodeverkService = mock(KodeverkService.class);
    private final PersonService service = new PersonService(pdlClient, null, null, null, null,
            kodeverkService, representasjonClient);

    @Before
    public void setup() {
        when(pdlClient.hentPersonNavn(new PdlRequest(GIVER, "B555"))).thenReturn(navn("Giver"));
        when(pdlClient.hentPersonNavn(new PdlRequest(MEKTIG, "B555"))).thenReturn(navn("Fullmektig"));
        when(kodeverkService.getBeskrivelseForTema("AAP")).thenReturn("Arbeidsavklaringspenger");
    }

    @Test
    public void filtrererPaaSluttdatoOgBeholderFremtidigStartOgIngenSluttdato() throws IOException {
        when(representasjonClient.hentFullmakt(GIVER.get())).thenReturn(List.of(
                fullmakt(UTGAATT_MEKTIG, IDAG.minusYears(1), IDAG.minusDays(1)),
                fullmakt(MEKTIG, IDAG.minusDays(10), IDAG),
                fullmakt(MEKTIG, IDAG.minusDays(10), IDAG.plusDays(1)),
                fullmakt(MEKTIG, IDAG.plusYears(2), IDAG.plusYears(3)),
                fullmakt(MEKTIG, IDAG.minusYears(1), null),
                fullmakt(MEKTIG, IDAG.plusYears(2), null)));

        var resultat = service.hentFullmakt(REQUEST, IDAG).getFullmakt();

        assertEquals(5, resultat.size());
        assertEquals(IDAG, resultat.get(0).getGyldigTilOgMed());
        assertEquals(IDAG.plusDays(1), resultat.get(1).getGyldigTilOgMed());
        assertEquals(IDAG.plusYears(2), resultat.get(2).getGyldigFraOgMed());
        assertNull(resultat.get(3).getGyldigTilOgMed());
        assertNull(resultat.get(4).getGyldigTilOgMed());
        assertEquals(IDAG.plusYears(2), resultat.get(4).getGyldigFraOgMed());
        verify(pdlClient).hentPersonNavn(new PdlRequest(GIVER, "B555"));
        verify(pdlClient).hentPersonNavn(new PdlRequest(MEKTIG, "B555"));
        verifyNoMoreInteractions(pdlClient);
    }

    @Test
    public void tomListeOgBareUtgaatteEllerTidligAvsluttedeGir204UtenNavneoppslag() throws IOException {
        for (List<ReprFullmaktV2Data> fullmakter : List.of(
                List.<ReprFullmaktV2Data>of(),
                List.of(fullmakt(MEKTIG, IDAG.minusYears(1), IDAG.minusDays(1))),
                List.of(fullmakt(MEKTIG, IDAG.plusDays(10), IDAG.minusDays(1))))) {
            when(representasjonClient.hentFullmakt(GIVER.get())).thenReturn(fullmakter);

            var feil = assertThrows(ResponseStatusException.class, () -> service.hentFullmakt(REQUEST, IDAG));

            assertEquals(HttpStatus.NO_CONTENT, feil.getStatusCode());
        }
        verifyNoInteractions(pdlClient, kodeverkService);
    }

    @Test
    public void konsistenskontrollSkjerForHeleListenFoerNavneoppslag() throws IOException {
        when(representasjonClient.hentFullmakt(GIVER.get())).thenReturn(List.of(
                fullmakt(MEKTIG, IDAG, null),
                new ReprFullmaktV2Data(MEKTIG, GIVER, IDAG.minusYears(1), IDAG.minusDays(1), List.of(), List.of())));

        var feil = assertThrows(IllegalStateException.class, () -> service.hentFullmakt(REQUEST, IDAG));

        assertFalse(feil.toString().contains(GIVER.get()));
        assertFalse(feil.toString().contains(MEKTIG.get()));
        verifyNoInteractions(pdlClient, kodeverkService);
    }

    @Test
    public void berikerBeggeNavnOgTemabeskrivelserOgDeduplisererBarePerRequest() throws IOException {
        when(representasjonClient.hentFullmakt(GIVER.get())).thenReturn(List.of(
                fullmakt(MEKTIG, IDAG, null), fullmakt(MEKTIG, IDAG.plusDays(1), null)));

        var resultat = service.hentFullmakt(REQUEST, IDAG).getFullmakt().getFirst();

        assertEquals(GIVER.get(), resultat.getFullmaktsgiver());
        assertEquals(MEKTIG.get(), resultat.getFullmektig());
        assertEquals("Giver Mellom Testesen", resultat.getFullmaktsgiverNavn());
        assertEquals("Fullmektig Mellom Testesen", resultat.getFullmektigsNavn());
        assertEquals("Arbeidsavklaringspenger", resultat.getOmraade().get(0).getTema());
        assertEquals("alle ytelser", resultat.getOmraade().get(1).getTema());
        assertEquals(List.of(FullmaktDTO.OmraadeHandlingType.SKRIV), resultat.getOmraade().get(1).getHandling());
        verify(pdlClient).hentPersonNavn(new PdlRequest(GIVER, "B555"));
        verify(pdlClient).hentPersonNavn(new PdlRequest(MEKTIG, "B555"));
        verifyNoMoreInteractions(pdlClient);

        service.hentFullmakt(REQUEST, IDAG);
        verify(pdlClient, times(2)).hentPersonNavn(new PdlRequest(GIVER, "B555"));
        verify(pdlClient, times(2)).hentPersonNavn(new PdlRequest(MEKTIG, "B555"));
    }

    @Test
    public void sammeIdentIParteneHentesBareEnGang() throws IOException {
        when(representasjonClient.hentFullmakt(GIVER.get())).thenReturn(List.of(fullmakt(GIVER, IDAG, null)));
        service.hentFullmakt(REQUEST, IDAG);
        verify(pdlClient).hentPersonNavn(new PdlRequest(GIVER, "B555"));
        verifyNoMoreInteractions(pdlClient);
    }

    @Test
    public void manglendeNavnPaaHverAvParteneFeilerHeleKallet() throws IOException {
        when(representasjonClient.hentFullmakt(GIVER.get())).thenReturn(List.of(fullmakt(MEKTIG, IDAG, null)));
        for (Fnr ident : List.of(MEKTIG, GIVER)) {
            when(pdlClient.hentPersonNavn(new PdlRequest(ident, "B555")))
                    .thenReturn(new HentPerson.PersonNavn().setNavn(List.of()));

            var feil = assertThrows(ResponseStatusException.class, () -> service.hentFullmakt(REQUEST, IDAG));

            assertEquals(HttpStatus.NOT_FOUND, feil.getStatusCode());
        }
        verifyNoInteractions(kodeverkService);
    }

    @Test
    public void ufullstendigNavnGirIkkeTilsynelatendeGyldigRespons() throws IOException {
        when(representasjonClient.hentFullmakt(GIVER.get())).thenReturn(List.of(fullmakt(MEKTIG, IDAG, null)));
        var ufullstendigNavn = navn("Fullmektig");
        ufullstendigNavn.getNavn().getFirst().setEtternavn(null);
        when(pdlClient.hentPersonNavn(new PdlRequest(MEKTIG, "B555"))).thenReturn(ufullstendigNavn);

        var feil = assertThrows(ResponseStatusException.class, () -> service.hentFullmakt(REQUEST, IDAG));

        assertEquals(HttpStatus.NOT_FOUND, feil.getStatusCode());
        verifyNoInteractions(kodeverkService);
    }

    @Test
    public void pdl403OgTekniskFeilBeholderFeiltypeUtenDelvisRespons() throws IOException {
        when(representasjonClient.hentFullmakt(GIVER.get())).thenReturn(List.of(fullmakt(MEKTIG, IDAG, null)));
        for (Fnr ident : List.of(MEKTIG, GIVER)) {
            for (RuntimeException aarsak : List.of(
                    new ResponseStatusException(HttpStatus.FORBIDDEN, "Feilbody med " + ident.get()),
                    new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Feilbody med " + ident.get()),
                    new IllegalStateException("PDL utilgjengelig"))) {
                doThrow(aarsak).when(pdlClient).hentPersonNavn(new PdlRequest(ident, "B555"));

                var feil = assertThrows(RuntimeException.class, () -> service.hentFullmakt(REQUEST, IDAG));
                if (aarsak instanceof ResponseStatusException pdlFeil) {
                    assertEquals(pdlFeil.getStatusCode(), ((ResponseStatusException) feil).getStatusCode());
                    assertFalse(feil.toString().contains(ident.get()));
                    assertNull(feil.getCause());
                } else {
                    assertSame(aarsak, feil);
                }
            }
        }
        verifyNoInteractions(kodeverkService);
    }

    @Test
    public void offentligMetodeBrukerSammeFullmaktsflyt() throws IOException {
        when(representasjonClient.hentFullmakt(GIVER.get()))
                .thenReturn(List.of(fullmakt(MEKTIG, IDAG, null)));

        assertEquals(1, service.hentFullmakt(REQUEST).getFullmakt().size());
        verify(pdlClient).hentPersonNavn(new PdlRequest(GIVER, "B555"));
        verify(pdlClient).hentPersonNavn(new PdlRequest(MEKTIG, "B555"));
    }

    @Test
    public void representasjonsfeilPropageresUtenNavneoppslag() throws IOException {
        var feil = new IOException("Representasjon utilgjengelig");
        when(representasjonClient.hentFullmakt(GIVER.get())).thenThrow(feil);

        assertSame(feil, assertThrows(IOException.class, () -> service.hentFullmakt(REQUEST, IDAG)));
        verifyNoInteractions(pdlClient, kodeverkService);
    }

    private static ReprFullmaktV2Data fullmakt(Fnr fullmektig, LocalDate fra, LocalDate til) {
        return new ReprFullmaktV2Data(GIVER, fullmektig, fra, til, List.of("AAP"), List.of("*"));
    }

    private static HentPerson.PersonNavn navn(String fornavn) {
        return new HentPerson.PersonNavn().setNavn(List.of(new HentPerson.Navn()
                .setFornavn(fornavn).setMellomnavn("Mellom").setEtternavn("Testesen")
                .setForkortetNavn("Skal ikke brukes")
                .setMetadata(new HentPerson.MetadataNavn().setMaster(HentPerson.PdlNavnMaster.PDL))));
    }
}
