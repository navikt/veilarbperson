package no.nav.veilarbperson.utils.mappers;

import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.pdl.HentPerson;
import no.nav.veilarbperson.client.pdl.domain.PdlRequest;
import no.nav.veilarbperson.client.pdl.domain.VergemaalEllerFullmaktOmfangType;
import no.nav.veilarbperson.client.pdl.domain.Vergetype;
import no.nav.veilarbperson.config.PdlClientTestConfig;
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
        VergeData vergeOgFullmaktData = VergeOgFullmaktDataMapper.toVerge(vergeOgFullmaktFraPdl);

        List<VergeData.VergemaalEllerFremtidsfullmakt> vergemaalEllerFremtidsfullmaktData = vergeOgFullmaktData.getVergemaalEllerFremtidsfullmakt();

        assertEquals(2, vergemaalEllerFremtidsfullmaktData.size());
        assertEquals(Vergetype.MIDLERTIDIG_FOR_VOKSEN, vergemaalEllerFremtidsfullmaktData.get(0).getType());
        assertEquals("VergemallEmbete", vergemaalEllerFremtidsfullmaktData.get(0).getEmbete());

        VergeData.VergeEllerFullmektig vergeEllerFullmektigData = vergemaalEllerFremtidsfullmaktData.get(0).getVergeEllerFullmektig();

        assertEquals("vergeFornavn1", vergeEllerFullmektigData.getNavn().getFornavn());
        assertEquals("VergeMotpartsPersonident1", vergeEllerFullmektigData.getMotpartsPersonident());
        assertEquals(VergemaalEllerFullmaktOmfangType.OEKONOMISKE_INTERESSER, vergeEllerFullmektigData.getOmfang());

        VergeData.Folkeregistermetadata folkeregistermetadata = vergemaalEllerFremtidsfullmaktData.get(0).getFolkeregistermetadata();
        LocalDateTime localDateTime = LocalDateTime.of(LocalDate.of(2021, 03, 02), LocalTime.of(13, 00, 42));
        assertEquals(localDateTime, folkeregistermetadata.getAjourholdstidspunkt());
        assertEquals(localDateTime, folkeregistermetadata.getGyldighetstidspunkt());
    }

}
