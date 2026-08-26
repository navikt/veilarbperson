package no.nav.veilarbperson.controller.v2;

import no.nav.common.types.identer.Fnr;
import no.nav.veilarbperson.client.regoppslag.RegoppslagResponseDTO;
import no.nav.veilarbperson.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v2/person")
public class PersonV2Controller {

    /**
     * @deprecated Bruk POST /api/v3/hent-person-tilgangsstyrt
     */
    @Deprecated
    @GetMapping
    public PersonV2Data hentPerson(@RequestParam("fnr") Fnr fnr) {
        throw new ResponseStatusException(HttpStatus.GONE, "Endepunktet er utgått. Bruk POST /api/v3/hent-person-tilgangsstyrt");
    }

    /**
     * @deprecated Bruk POST /api/v3/person/hent-malform
     */
    @Deprecated
    @GetMapping("/malform")
    public Malform malform(@RequestParam("fnr") Fnr fnr) {
        throw new ResponseStatusException(HttpStatus.GONE, "Endepunktet er utgått. Bruk POST /api/v3/person/hent-malform");
    }

    /**
     * @deprecated Bruk POST /api/v3/person/hent-vergeOgFullmakt
     */
    @Deprecated
    @GetMapping("/vergeOgFullmakt")
    public VergeData hentVergemaal(@RequestParam("fnr") Fnr fnr) {
        throw new ResponseStatusException(HttpStatus.GONE, "Endepunktet er utgått. Bruk POST /api/v3/person/hent-vergeOgFullmakt");
    }

    /**
     * @deprecated Bruk POST /api/v3/person/hent-tolk
     */
    @Deprecated
    @GetMapping("/tolk")
    public TilrettelagtKommunikasjonData hentSpraakTolk(@RequestParam("fnr") Fnr fnr) {
        throw new ResponseStatusException(HttpStatus.GONE, "Endepunktet er utgått. Bruk POST /api/v3/person/hent-tolk");
    }

    /**
     * @deprecated Bruk POST /api/v3/person/hent-navn
     */
    @Deprecated
    @GetMapping("/navn")
    public PersonNavnV2 hentNavn(@RequestParam("fnr") Fnr fnr) {
        throw new ResponseStatusException(HttpStatus.GONE, "Endepunktet er utgått. Bruk POST /api/v3/person/hent-navn");
    }

    /**
     * @deprecated Bruk POST /api/v3/person/hent-postadresse
     */
    @Deprecated
    @GetMapping("/postadresse")
    public RegoppslagResponseDTO hentPostadresse(@RequestParam("fnr") Fnr fnr) {
        throw new ResponseStatusException(HttpStatus.GONE, "Endepunktet er utgått. Bruk POST /api/v3/person/hent-postadresse");
    }
}
