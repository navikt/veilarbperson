package no.nav.veilarbperson.utils.mappers;

import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.pdl.HentPerson;
import no.nav.veilarbperson.client.pdl.domain.PdlRequest;
import no.nav.veilarbperson.client.pdl.domain.VergemaalEllerFullmaktOmfangType;
import no.nav.veilarbperson.client.pdl.domain.Vergetype;
import no.nav.veilarbperson.config.PdlClientTestConfig;
import no.nav.veilarbperson.domain.PersonNavnV2;
import no.nav.veilarbperson.domain.VergeData;
import no.nav.veilarbperson.utils.TestUtils;
import no.nav.veilarbperson.utils.VergeOgFullmaktDataMapper;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VergeOgFullmaktDataMapperTest extends PdlClientTestConfig {

    private static Fnr FNR = TestUtils.fodselsnummerForDato("1980-01-01");

    public HentPerson.Verge hentVerge(Fnr fnr) {
        configurePdlResponse("pdl-hentVerge-response.json");
        return getPdlClient().hentVerge(new PdlRequest(fnr, null));
    }

    @Test
    public void vergetypeOgOmfangEnumMapperTest() {
        List<HentPerson.VergemaalEllerFremtidsfullmakt> vergemaal = hentVerge(FNR).getVergemaalEllerFremtidsfullmakt();

        VergemaalEllerFullmaktOmfangType omfang1 = ofNullable(vergemaal.get(0).getVergeEllerFullmektig()).map(HentPerson.VergeEllerFullmektig::getOmfang).get();
        VergemaalEllerFullmaktOmfangType omfang2 = ofNullable(vergemaal.get(1).getVergeEllerFullmektig()).map(HentPerson.VergeEllerFullmektig::getOmfang).get();

        assertEquals(Vergetype.MIDLERTIDIG_FOR_VOKSEN, vergemaal.get(0).getType());
        assertEquals(Vergetype.STADFESTET_FREMTIDSFULLMAKT, vergemaal.get(1).getType());

        assertEquals(VergemaalEllerFullmaktOmfangType.OEKONOMISKE_INTERESSER, omfang1);
        assertEquals(VergemaalEllerFullmaktOmfangType.PERSONLIGE_INTERESSER, omfang2);
    }

    @Test
    public void toVergeOgFullmaktDataTest() {
        HentPerson.Verge vergeOgFullmaktFraPdl = hentVerge(FNR);
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

        PersonNavnV2 personnavn = new PersonNavnV2().setFornavn("Testfornavnavn1").setEtternavn("Testetternavn1");
        VergeData.VergemaalEllerFremtidsfullmakt vergeOgFullmaktDataLast = VergeOgFullmaktDataMapper.toVergemaalEllerFremtidsfullmakt(vergeOgFullmaktFraPdl.getVergemaalEllerFremtidsfullmakt().getLast(), personnavn);

        VergeData.VergeEllerFullmektig vergeEllerFullmektigDataLast = vergeOgFullmaktDataLast.getVergeEllerFullmektig();
        assertEquals("Testfornavnavn1", vergeEllerFullmektigDataLast.getNavn().getFornavn());
        assertEquals(5, vergeEllerFullmektigDataLast.getTjenesteomraade().size());
        assertEquals("arbeid", vergeEllerFullmektigDataLast.getTjenesteomraade().getFirst().getTjenesteoppgave());
    }

}
