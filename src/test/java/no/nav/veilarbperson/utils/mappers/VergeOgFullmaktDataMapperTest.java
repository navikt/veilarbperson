package no.nav.veilarbperson.utils.mappers;

import no.nav.common.types.identer.Fnr;
import no.nav.common.json.JsonUtils;
import no.nav.veilarbperson.client.pdl.HentPerson;
import no.nav.veilarbperson.client.pdl.domain.VergemaalEllerFullmaktOmfangType;
import no.nav.veilarbperson.client.pdl.domain.Vergetype;
import no.nav.veilarbperson.client.representasjon.ReprFullmaktV2Data;
import no.nav.veilarbperson.domain.PersonNavn;
import no.nav.veilarbperson.domain.FullmaktDTO;
import no.nav.veilarbperson.domain.VergeData;
import no.nav.veilarbperson.utils.TestUtils;
import no.nav.veilarbperson.utils.VergeOgFullmaktDataMapper;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static no.nav.veilarbperson.domain.FullmaktDTO.OmraadeHandlingType.*;

public class VergeOgFullmaktDataMapperTest {

    private static Fnr FNR = TestUtils.fodselsnummerForDato("1980-01-01");

    @Test
    public void fullmaktsrettigheterSamlesUtenDuplikaterOgUtenImplisittLesing() {
        var omraader = VergeOgFullmaktDataMapper.omraadeMapper(
                List.of("AAP", "DAG", "AAP"), List.of("DAG", "PEN", "PEN"));

        assertEquals(List.of("AAP", "DAG", "PEN"), omraader.stream().map(FullmaktDTO.OmraadeMedHandling::getTema).toList());
        assertEquals(List.of(LES), omraader.get(0).getHandling());
        assertEquals(List.of(LES, SKRIV), omraader.get(1).getHandling());
        assertEquals(List.of(SKRIV), omraader.get(2).getHandling());
        assertTrue(omraader.stream().noneMatch(omraade -> omraade.getHandling().contains(KOMMUNISER)));
    }

    @Test
    public void bareLesingBareSkrivingOgIngenRettigheter() {
        assertEquals(List.of(LES), VergeOgFullmaktDataMapper.omraadeMapper(List.of("*"), List.of()).getFirst().getHandling());
        assertEquals(List.of(SKRIV), VergeOgFullmaktDataMapper.omraadeMapper(List.of(), List.of("*")).getFirst().getHandling());
        assertTrue(VergeOgFullmaktDataMapper.omraadeMapper(List.of(), List.of()).isEmpty());
    }

    @Test
    public void fullmaktMapperIdentifikatorerDatoerOgFullstendigeNavn() {
        Fnr fullmektig = Fnr.of("02020212345");
        LocalDate fra = LocalDate.of(2026, 11, 1);
        LocalDate til = fra.plusYears(1);
        var navn = Map.of(
                FNR, new PersonNavn().setFornavn("Giver").setMellomnavn("Mellom").setEtternavn("Testesen"),
                fullmektig, new PersonNavn().setFornavn("Mektig").setEtternavn("Testesen"));
        var fullmakter = List.of(
                new ReprFullmaktV2Data(FNR, fullmektig, fra, til, List.of("AAP"), List.of()),
                new ReprFullmaktV2Data(FNR, fullmektig, fra, null, List.of(), List.of()));

        var resultat = VergeOgFullmaktDataMapper.toFullmaktDTO(fullmakter, navn).getFullmakt();

        assertEquals(FNR.get(), resultat.getFirst().getFullmaktsgiver());
        assertEquals(fullmektig.get(), resultat.getFirst().getFullmektig());
        assertEquals("Giver Mellom Testesen", resultat.getFirst().getFullmaktsgiverNavn());
        assertEquals("Mektig Testesen", resultat.getFirst().getFullmektigsNavn());
        assertEquals(fra, resultat.getFirst().getGyldigFraOgMed());
        assertEquals(til, resultat.getFirst().getGyldigTilOgMed());
        assertNull(resultat.getLast().getGyldigTilOgMed());
        assertTrue(resultat.getLast().getOmraade().isEmpty());
        assertThrows(NullPointerException.class, () -> VergeOgFullmaktDataMapper.toFullmaktDTO(fullmakter, Map.of()));
    }

    private HentPerson.Verge hentVerge() {
        var response = JsonUtils.getMapper().readTree(TestUtils.readTestResourceFile("pdl-hentVerge-response.json"));
        return JsonUtils.getMapper().treeToValue(response.get("data").get("hentPerson"), HentPerson.Verge.class);
    }

    @Test
    public void vergetypeOgOmfangEnumMapperTest() {
        List<HentPerson.VergemaalEllerFremtidsfullmakt> vergemaal = hentVerge().getVergemaalEllerFremtidsfullmakt();

        VergemaalEllerFullmaktOmfangType omfang1 = ofNullable(vergemaal.get(0).getVergeEllerFullmektig()).map(HentPerson.VergeEllerFullmektig::getOmfang).get();
        VergemaalEllerFullmaktOmfangType omfang2 = ofNullable(vergemaal.get(1).getVergeEllerFullmektig()).map(HentPerson.VergeEllerFullmektig::getOmfang).get();

        assertEquals(Vergetype.MIDLERTIDIG_FOR_VOKSEN, vergemaal.get(0).getType());
        assertEquals(Vergetype.STADFESTET_FREMTIDSFULLMAKT, vergemaal.get(1).getType());

        assertEquals(VergemaalEllerFullmaktOmfangType.OEKONOMISKE_INTERESSER, omfang1);
        assertEquals(VergemaalEllerFullmaktOmfangType.PERSONLIGE_INTERESSER, omfang2);
    }

    @Test
    public void toVergeOgFullmaktDataTest() {
        HentPerson.Verge vergeOgFullmaktFraPdl = hentVerge();
        VergeData.VergemaalEllerFremtidsfullmakt vergeOgFullmaktDataFirst = VergeOgFullmaktDataMapper.toVergemaalEllerFremtidsfullmakt(vergeOgFullmaktFraPdl.getVergemaalEllerFremtidsfullmakt().getFirst(), null);

        assertEquals(Vergetype.MIDLERTIDIG_FOR_VOKSEN, vergeOgFullmaktDataFirst.getType());
        assertEquals("VergemallEmbete", vergeOgFullmaktDataFirst.getEmbete());
        VergeData.VergeEllerFullmektig vergeEllerFullmektigDataFirst = vergeOgFullmaktDataFirst.getVergeEllerFullmektig();

        assertEquals("vergeFornavn1", vergeEllerFullmektigDataFirst.getNavn().getFornavn());
        assertEquals("VergeMotpartsPersonident1", vergeEllerFullmektigDataFirst.getMotpartsPersonident());
        assertEquals(VergemaalEllerFullmaktOmfangType.OEKONOMISKE_INTERESSER, vergeEllerFullmektigDataFirst.getOmfang());
        assertTrue(vergeEllerFullmektigDataFirst.getTjenesteomraade().isEmpty());

        VergeData.Folkeregistermetadata folkeregistermetadata = vergeOgFullmaktDataFirst.getFolkeregistermetadata();
        LocalDateTime localDateTime = LocalDateTime.of(LocalDate.of(2021, 03, 02), LocalTime.of(13, 00, 42));
        assertEquals(localDateTime, folkeregistermetadata.getAjourholdstidspunkt());
        assertEquals(localDateTime, folkeregistermetadata.getGyldighetstidspunkt());

        PersonNavn personnavn = new PersonNavn().setFornavn("Testfornavnavn1").setEtternavn("Testetternavn1");
        VergeData.VergemaalEllerFremtidsfullmakt vergeOgFullmaktDataLast = VergeOgFullmaktDataMapper.toVergemaalEllerFremtidsfullmakt(vergeOgFullmaktFraPdl.getVergemaalEllerFremtidsfullmakt().getLast(), personnavn);

        VergeData.VergeEllerFullmektig vergeEllerFullmektigDataLast = vergeOgFullmaktDataLast.getVergeEllerFullmektig();
        assertEquals("Testfornavnavn1", vergeEllerFullmektigDataLast.getNavn().getFornavn());
        assertEquals(5, vergeEllerFullmektigDataLast.getTjenesteomraade().size());
        assertEquals("arbeid", vergeEllerFullmektigDataLast.getTjenesteomraade().getFirst().getTjenesteoppgave());
    }

}
