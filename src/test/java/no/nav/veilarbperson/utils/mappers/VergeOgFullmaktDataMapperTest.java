package no.nav.veilarbperson.utils.mappers;

import no.nav.veilarbperson.client.pdl.HentPdlPerson;
import no.nav.veilarbperson.client.pdl.PdlClientImpl;
import no.nav.veilarbperson.client.pdl.domain.VergeOgFullmaktData;
import no.nav.veilarbperson.client.pdl.domain.VergemaalEllerFullmaktOmfangType;
import no.nav.veilarbperson.client.pdl.domain.Vergetype;
import no.nav.veilarbperson.config.PdlClientTestConfig;
import no.nav.veilarbperson.utils.VergeOgFullmaktDataMapper;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class VergeOgFullmaktDataMapperTest extends PdlClientTestConfig {

    static final String FNR = "0123456789";

    public HentPdlPerson.VergeOgFullmakt hentVergeOgFullmakt(String fnr) {
        PdlClientImpl pdlClient = configurPdlClient("pdl-hentVergeOgFullmakt-response.json");
        return pdlClient.hentVergeOgFullmakt(fnr, "USER_TOKEN");
    }

    @Test
    public void vergetypeOgOmfangEnumMapperTest() {
        List<HentPdlPerson.VergemaalEllerFremtidsfullmakt> vergemaal = hentVergeOgFullmakt(FNR).getVergemaalEllerFremtidsfullmakt();
        VergemaalEllerFullmaktOmfangType omfang1 = ofNullable(vergemaal.get(0).getVergeEllerFullmektig()).map(HentPdlPerson.VergeEllerFullmektig::getOmfang).get();
        VergemaalEllerFullmaktOmfangType omfang2 = ofNullable(vergemaal.get(1).getVergeEllerFullmektig()).map(HentPdlPerson.VergeEllerFullmektig::getOmfang).get();

        assertEquals(Vergetype.MIDLERTIDIG_FOR_VOKSEN, vergemaal.get(0).getType());
        assertEquals(VergemaalEllerFullmaktOmfangType.OEKONOMISKE_INTERESSER, omfang1);

        assertEquals(Vergetype.STADFESTET_FREMTIDSFULLMAKT, vergemaal.get(1).getType());
        assertEquals(VergemaalEllerFullmaktOmfangType.PERSONLIGE_INTERESSER, omfang2);
    }

    @Test
    public void toVergeOgFullmaktDataTest() {
        HentPdlPerson.VergeOgFullmakt vergeOgFullmaktFraPdl = hentVergeOgFullmakt(FNR);
        VergeOgFullmaktData vergeOgFullmaktData = VergeOgFullmaktDataMapper.toVergeOgFullmaktData(vergeOgFullmaktFraPdl);

        List<VergeOgFullmaktData.VergemaalEllerFremtidsfullmakt> vergemaalEllerFremtidsfullmaktData = vergeOgFullmaktData.getVergemaalEllerFremtidsfullmakt();

        assertEquals(2, vergemaalEllerFremtidsfullmaktData.size());
        assertEquals(Vergetype.MIDLERTIDIG_FOR_VOKSEN, vergemaalEllerFremtidsfullmaktData.get(0).getType());
        assertEquals("VergemallEmbete", vergemaalEllerFremtidsfullmaktData.get(0).getEmbete());

        VergeOgFullmaktData.VergeEllerFullmektig vergeEllerFullmektigData = vergemaalEllerFremtidsfullmaktData.get(0).getVergeEllerFullmektig();

        assertEquals("vergeFornavn1", vergeEllerFullmektigData.getNavn().getFornavn());
        assertEquals("VergeMotpartsPersonident1", vergeEllerFullmektigData.getMotpartsPersonident());
        assertEquals(VergemaalEllerFullmaktOmfangType.OEKONOMISKE_INTERESSER, vergeEllerFullmektigData.getOmfang());

        VergeOgFullmaktData.Folkeregistermetadata folkeregistermetadata = vergemaalEllerFremtidsfullmaktData.get(0).getFolkeregistermetadata();
        LocalDateTime localDateTime = LocalDateTime.of(LocalDate.of(2021, 03, 02), LocalTime.of(13, 00, 42));
        assertEquals(localDateTime, folkeregistermetadata.getAjourholdstidspunkt());
        assertEquals(localDateTime, folkeregistermetadata.getGyldighetstidspunkt());

        List<VergeOgFullmaktData.Fullmakt> fullmakts = vergeOgFullmaktData.getFullmakt();

        assertEquals(2, fullmakts.size());
        assertEquals("motpartsPersonident1", fullmakts.get(0).getMotpartsPersonident());
        assertEquals("motpartsRolle1", fullmakts.get(0).getMotpartsRolle());

        assertEquals(LocalDate.of(2021, 01, 15), fullmakts.get(0).getGyldigFraOgMed());
        assertEquals(LocalDate.of(2021, 01, 15), fullmakts.get(0).getGyldigFraOgMed());
    }

}
